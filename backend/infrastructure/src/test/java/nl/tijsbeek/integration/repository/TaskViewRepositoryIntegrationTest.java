package nl.tijsbeek.integration.repository;

import nl.tijsbeek.entities.Board;
import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.Task;
import nl.tijsbeek.integration.fixtures.IntegrationTestBase;
import nl.tijsbeek.interfaces.repositories.BoardRepository;
import nl.tijsbeek.interfaces.repositories.BoardViewRepository;
import nl.tijsbeek.interfaces.repositories.TaskRepository;
import nl.tijsbeek.testing.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@IntegrationTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TaskViewRepositoryIntegrationTest extends IntegrationTestBase {

    private static final LocalDateTime FIRST_CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);
    private static final LocalDateTime SECOND_CREATED_AT = FIRST_CREATED_AT.plusDays(1);

    private final JdbcTemplate jdbcTemplate;
    private final BoardRepository boardRepository;
    private final BoardViewRepository boardViewRepository;
    private final TaskRepository taskRepository;

    TaskViewRepositoryIntegrationTest(JdbcTemplate jdbcTemplate,
                                      BoardRepository boardRepository,
                                      BoardViewRepository boardViewRepository,
                                      TaskRepository taskRepository) {
        super(jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate;
        this.boardRepository = boardRepository;
        this.boardViewRepository = boardViewRepository;
        this.taskRepository = taskRepository;
    }

    @ParameterizedTest
    @MethodSource("sortingOptions")
    void findAllForView_ReturnsOnlyMatchingTasksInConfiguredOrder(
            BoardView.SortBy sortBy,
            BoardView.SortDirection sortDirection,
            List<String> expectedTitles
    ) {
        // Arrange
        Board board = createBoard("BoardOne", List.of(
                new BoardStatus(null, null, "Later status", 20),
                new BoardStatus(null, null, "First status", 0)
        ));
        BoardStatus firstStatus = board.getStatuses().stream()
                .filter(status -> status.getName().equals("First status")).findFirst().orElseThrow();
        BoardStatus laterStatus = board.getStatuses().stream()
                .filter(status -> status.getName().equals("Later status")).findFirst().orElseThrow();
        saveTask(board, "B", firstStatus, SECOND_CREATED_AT);
        saveTask(board, "D", firstStatus, FIRST_CREATED_AT);
        saveTask(board, "A", laterStatus, SECOND_CREATED_AT);
        saveTask(board, "C", laterStatus, FIRST_CREATED_AT);
        saveTask(board, "E", null, FIRST_CREATED_AT);
        Board otherBoard = createBoard("OtherBoard", List.of());
        saveTask(otherBoard, "Other task", null, FIRST_CREATED_AT);
        BoardView view = saveView(board, sortBy, sortDirection);

        // Act
        List<Task> result = taskRepository.findAllForView(view.getId());

        // Assert
        assertThat(result).extracting(Task::getTitle).containsExactlyElementsOf(expectedTitles);
        assertThat(result).allSatisfy(task -> {
            assertThat(task.getBoardId()).isEqualTo(board.getId());
            assertThat(task.getDescription()).isEqualTo("Description for " + task.getTitle());
            assertThat(task.getCreatedAt()).isNotNull();
        });
        assertThat(result).filteredOn(task -> task.getTitle().equals("B"))
                .singleElement().satisfies(task -> {
                    assertThat(task.getCreatedAt()).isEqualTo(SECOND_CREATED_AT);
                    assertThat(task.getStatus().getId()).isEqualTo(firstStatus.getId());
                    assertThat(task.getStatus().getBoardId()).isEqualTo(board.getId());
                    assertThat(task.getStatus().getName()).isEqualTo("First status");
                    assertThat(task.getStatus().getPosition()).isZero();
                });
        assertThat(result).filteredOn(task -> task.getTitle().equals("E"))
                .singleElement().satisfies(task -> assertThat(task.getStatus()).isNull());
    }

    @Test
    void findAllForView_EqualSortValues_ReturnsTasksOrderedById() {
        // Arrange
        Board board = createBoard("BoardOne", List.of());
        Task firstTask = saveTask(board, "Same title", null, FIRST_CREATED_AT);
        Task secondTask = saveTask(board, "Same title", null, FIRST_CREATED_AT);
        BoardView view = saveView(board, BoardView.SortBy.TITLE, BoardView.SortDirection.DESC);

        // Act
        List<Task> result = taskRepository.findAllForView(view.getId());

        // Assert
        assertThat(result).extracting(Task::getId).containsExactly(firstTask.getId(), secondTask.getId());
    }

    @Test
    void findAllForView_StatusesAtSamePosition_ReturnsTasksOrderedByStatusId() {
        // Arrange
        Board board = createBoard("BoardOne", List.of(
                new BoardStatus(null, null, "First status", 0),
                new BoardStatus(null, null, "Second status", 0)
        ));
        Board persistedBoard = boardRepository.findById(board.getId()).orElseThrow();
        BoardStatus firstStatus = persistedBoard.getStatuses().get(0);
        BoardStatus secondStatus = persistedBoard.getStatuses().get(1);
        Task secondTask = saveTask(board, "A", secondStatus, FIRST_CREATED_AT);
        Task firstTask = saveTask(board, "Z", firstStatus, FIRST_CREATED_AT);
        BoardView view = saveView(board, BoardView.SortBy.TITLE, BoardView.SortDirection.ASC);

        // Act
        List<Task> result = taskRepository.findAllForView(view.getId());

        // Assert
        assertThat(result).extracting(task -> task.getStatus().getId())
                .containsExactly(firstStatus.getId(), secondStatus.getId());
        assertThat(result).extracting(Task::getId).containsExactly(firstTask.getId(), secondTask.getId());
    }

    @Test
    void findAllForView_BoardWithoutTasks_ReturnsEmptyList() {
        // Arrange
        Board board = createBoard("EmptyBoard", List.of());
        Board otherBoard = createBoard("OtherBoard", List.of());
        saveTask(otherBoard, "Other task", null, FIRST_CREATED_AT);
        BoardView view = saveView(board, BoardView.SortBy.TITLE, BoardView.SortDirection.ASC);

        // Act
        List<Task> result = taskRepository.findAllForView(view.getId());

        // Assert
        assertThat(result).isEmpty();
    }

    private static Stream<Arguments> sortingOptions() {
        return Stream.of(
                Arguments.of(BoardView.SortBy.TITLE, BoardView.SortDirection.ASC, List.of("B", "D", "A", "C", "E")),
                Arguments.of(BoardView.SortBy.TITLE, BoardView.SortDirection.DESC, List.of("D", "B", "C", "A", "E")),
                Arguments.of(BoardView.SortBy.CREATED_AT, BoardView.SortDirection.ASC, List.of("D", "B", "C", "A", "E")),
                Arguments.of(BoardView.SortBy.CREATED_AT, BoardView.SortDirection.DESC, List.of("B", "D", "A", "C", "E"))
        );
    }

    private Board createBoard(String title, List<BoardStatus> statuses) {
        return boardRepository.save(new Board(null, title, "Description for " + title, statuses));
    }

    private BoardView saveView(Board board, BoardView.SortBy sortBy, BoardView.SortDirection sortDirection) {
        return boardViewRepository.save(new BoardView(null, board.getId(), "Board view", BoardView.ViewType.BOARD,
                BoardView.GroupBy.STATUS, sortBy, sortDirection, 0));
    }

    private Task saveTask(Board board, String title, BoardStatus status, LocalDateTime createdAt) {
        Task task = taskRepository.save(new Task(null, board.getId(), title, "Description for " + title, null, status));
        jdbcTemplate.update("UPDATE task SET created_at = ? WHERE id = ?", Timestamp.valueOf(createdAt), task.getId());
        return task;
    }
}
