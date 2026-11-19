package nl.tijsbeek.integration.repository;

import nl.tijsbeek.entities.Board;
import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.FilterRule;
import nl.tijsbeek.entities.FilterRule.FilterField;
import nl.tijsbeek.entities.FilterRule.FilterOperator;
import nl.tijsbeek.entities.Task;
import nl.tijsbeek.integration.fixtures.IntegrationTestBase;
import nl.tijsbeek.interfaces.repositories.BoardRepository;
import nl.tijsbeek.interfaces.repositories.BoardViewRepository;
import nl.tijsbeek.interfaces.repositories.TaskRepository;
import nl.tijsbeek.testing.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
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

    @ParameterizedTest
    @EnumSource(value = FilterOperator.class, names = {"IN", "NOT_IN"})
    void findAllForView_WithStatusFilter_ReturnsMatchingTasksInConfiguredOrder(FilterOperator operator) {
        // Arrange
        Board board = createBoard("BoardOne", List.of(
                new BoardStatus(null, null, "To Do", 0),
                new BoardStatus(null, null, "In Progress", 1),
                new BoardStatus(null, null, "Done", 2)
        ));
        List<BoardStatus> statuses = board.getStatuses();
        Task todo = saveTask(board, "Z", statuses.get(0), FIRST_CREATED_AT);
        Task inProgress = saveTask(board, "A", statuses.get(1), FIRST_CREATED_AT);
        Task done = saveTask(board, "Done", statuses.get(2), FIRST_CREATED_AT);
        saveTask(board, "Unassigned", null, FIRST_CREATED_AT);
        FilterRule rule = new FilterRule(FilterField.STATUS, operator,
                List.of(statuses.get(0).getId().toString(), statuses.get(1).getId().toString()));
        BoardView view = saveFilteredView(board, rule);
        List<Long> expectedIds = operator == FilterOperator.IN
                ? List.of(todo.getId(), inProgress.getId()) : List.of(done.getId());

        // Act
        List<Task> result = taskRepository.findAllForView(view.getId());

        // Assert
        assertThat(result).extracting(Task::getId).containsExactlyElementsOf(expectedIds);
    }

    @ParameterizedTest
    @MethodSource("titleFilters")
    void findAllForView_WithTitleFilter_ReturnsMatchingBoardTasks(
            FilterOperator operator,
            String value,
            List<String> expectedTitles
    ) {
        // Arrange
        Board board = createBoard("BoardOne", List.of());
        saveTask(board, "Bug fix in auth", null, FIRST_CREATED_AT);
        saveTask(board, "Feature user profile", null, FIRST_CREATED_AT);
        saveTask(board, "Refactor Auth service", null, FIRST_CREATED_AT);
        saveTask(board, "Track 100%_! complete", null, FIRST_CREATED_AT);
        saveTask(board, "Track 100xy! complete", null, FIRST_CREATED_AT);
        saveTask(board, "Check ' OR TRUE --", null, FIRST_CREATED_AT);
        Board otherBoard = createBoard("OtherBoard", List.of());
        saveTask(otherBoard, "Bug fix in auth", null, FIRST_CREATED_AT);
        BoardView view = saveFilteredView(board, new FilterRule(FilterField.TITLE, operator, List.of(value)));

        // Act
        List<Task> result = taskRepository.findAllForView(view.getId());

        // Assert
        assertThat(result).extracting(Task::getTitle).containsExactlyElementsOf(expectedTitles);
    }

    @ParameterizedTest
    @MethodSource("dueDateFilters")
    void findAllForView_WithDueDateFilter_ReturnsMatchingTasks(
            FilterOperator operator,
            List<String> values,
            List<String> expectedTitles
    ) {
        // Arrange
        Board board = createBoard("BoardOne", List.of());
        saveTaskWithDueDate(board, "A past", FIRST_CREATED_AT.minusDays(1));
        saveTaskWithDueDate(board, "B boundary", FIRST_CREATED_AT);
        saveTaskWithDueDate(board, "C future", FIRST_CREATED_AT.plusDays(1));
        saveTaskWithDueDate(board, "D no due date", null);
        BoardView view = saveFilteredView(board, new FilterRule(FilterField.DUE_DATE, operator, values));

        // Act
        List<Task> result = taskRepository.findAllForView(view.getId());

        // Assert
        assertThat(result).extracting(Task::getTitle).containsExactlyElementsOf(expectedTitles);
    }

    @Test
    void findAllForView_WithMultipleFilters_ReturnsTasksMatchingAllRules() {
        // Arrange
        Board board = createBoard("BoardOne", List.of(
                new BoardStatus(null, null, "To Do", 0),
                new BoardStatus(null, null, "Done", 1)
        ));
        BoardStatus todo = board.getStatuses().get(0);
        BoardStatus done = board.getStatuses().get(1);
        saveTask(board, "Bug fix in auth", todo, FIRST_CREATED_AT);
        saveTask(board, "Feature user profile", todo, FIRST_CREATED_AT);
        saveTask(board, "Refactor auth service", done, FIRST_CREATED_AT);
        BoardView view = saveFilteredView(board,
                new FilterRule(FilterField.STATUS, FilterOperator.IN, List.of(todo.getId().toString())),
                new FilterRule(FilterField.TITLE, FilterOperator.CONTAINS, List.of("auth")));

        // Act
        List<Task> result = taskRepository.findAllForView(view.getId());

        // Assert
        assertThat(result).extracting(Task::getTitle).containsExactly("Bug fix in auth");
    }

    @Test
    void findAllForView_WithNullFilterRules_ReturnsAllBoardTasksInOrder() {
        // Arrange
        Board board = createBoard("BoardOne", List.of());
        saveTask(board, "B", null, FIRST_CREATED_AT);
        saveTask(board, "A", null, FIRST_CREATED_AT);
        BoardView view = boardViewRepository.save(new BoardView(null, board.getId(), "Board view",
                BoardView.ViewType.BOARD, null, BoardView.SortBy.TITLE, BoardView.SortDirection.ASC, 0, null));

        // Act
        List<Task> result = taskRepository.findAllForView(view.getId());

        // Assert
        assertThat(result).extracting(Task::getTitle).containsExactly("A", "B");
    }

    private static Stream<Arguments> titleFilters() {
        return Stream.of(
                Arguments.of(FilterOperator.CONTAINS, "AUTH", List.of("Bug fix in auth", "Refactor Auth service")),
                Arguments.of(FilterOperator.CONTAINS, "%_!", List.of("Track 100%_! complete")),
                Arguments.of(FilterOperator.CONTAINS, "' OR TRUE --", List.of("Check ' OR TRUE --")),
                Arguments.of(FilterOperator.EQUALS, "Bug fix in auth", List.of("Bug fix in auth")),
                Arguments.of(FilterOperator.EQUALS, "bug fix in auth", List.of())
        );
    }

    private static Stream<Arguments> dueDateFilters() {
        List<String> boundary = List.of(FIRST_CREATED_AT.toString());
        return Stream.of(
                Arguments.of(FilterOperator.LESS_THAN_OR_EQUAL, boundary, List.of("A past", "B boundary")),
                Arguments.of(FilterOperator.GREATER_THAN_OR_EQUAL, boundary, List.of("B boundary", "C future")),
                Arguments.of(FilterOperator.IS_NULL, List.of(), List.of("D no due date")),
                Arguments.of(FilterOperator.IS_NOT_NULL, List.of(), List.of("A past", "B boundary", "C future"))
        );
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

    private BoardView saveFilteredView(Board board, FilterRule... rules) {
        return boardViewRepository.save(new BoardView(null, board.getId(), "Filtered view", BoardView.ViewType.BOARD,
                BoardView.GroupBy.STATUS, BoardView.SortBy.TITLE, BoardView.SortDirection.ASC, 0, List.of(rules)));
    }

    private void saveTaskWithDueDate(Board board, String title, LocalDateTime dueDate) {
        taskRepository.save(new Task(board.getId(), title, "Description for " + title, dueDate, null));
    }

    private Task saveTask(Board board, String title, BoardStatus status, LocalDateTime createdAt) {
        Task task = taskRepository.save(new Task(board.getId(), title, "Description for " + title, null, status));
        jdbcTemplate.update("UPDATE task SET created_at = ? WHERE id = ?", Timestamp.valueOf(createdAt), task.getId());
        return task;
    }
}
