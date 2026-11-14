package nl.tijsbeek.integration.repository;

import nl.tijsbeek.entities.Board;
import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.entities.Task;
import nl.tijsbeek.integration.fixtures.IntegrationTestBase;
import nl.tijsbeek.interfaces.repositories.BoardRepository;
import nl.tijsbeek.interfaces.repositories.TaskRepository;
import nl.tijsbeek.testing.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
@IntegrationTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TaskRepositoryIntegrationTest extends IntegrationTestBase {

    private final TaskRepository taskRepository;
    private final BoardRepository boardRepository;

    TaskRepositoryIntegrationTest(JdbcTemplate jdbcTemplate,
                                  TaskRepository taskRepository,
                                  BoardRepository boardRepository) {
        super(jdbcTemplate);
        this.taskRepository = taskRepository;
        this.boardRepository = boardRepository;
    }

    @Test
    void saveTaskWithoutStatus_ThenFindById_ReturnsPersistedTask() {
        // Arrange
        Board board = createBoard("BoardOne");
        Task task = new Task(null, board.getId(), "TaskOne", "Task one for testing", null, null);

        // Act
        Task savedTask = taskRepository.save(task);
        Task foundTask = taskRepository.findById(savedTask.getId()).orElseThrow();

        // Assert
        assertThat(savedTask.getId()).isNotNull().isPositive();
        assertThat(foundTask.getId()).isEqualTo(savedTask.getId());
        assertThat(foundTask.getBoardId()).isEqualTo(board.getId());
        assertThat(foundTask.getTitle()).isEqualTo("TaskOne");
        assertThat(foundTask.getDescription()).isEqualTo("Task one for testing");
        assertThat(foundTask.getCreatedAt()).isNotNull();
        assertThat(foundTask.getStatus()).isNull();
    }

    @Test
    void saveTaskWithExistingStatus_ThenFindById_ReturnsPersistedStatus() {
        // Arrange
        Board board = createBoard("BoardOne");
        BoardStatus status = board.getStatuses().get(0);

        // Act
        Task savedTask = saveTask(board, "TaskOne", status);
        Task foundTask = taskRepository.findById(savedTask.getId()).orElseThrow();

        // Assert
        assertThat(foundTask.getStatus()).isNotNull();
        assertThat(foundTask.getStatus().getId()).isEqualTo(status.getId());
        assertThat(foundTask.getStatus().getName()).isEqualTo("To do");
        assertThat(foundTask.getStatus().getPosition()).isZero();
        assertThat(boardRepository.findById(board.getId()).orElseThrow().getStatuses())
                .extracting(BoardStatus::getId)
                .containsExactlyInAnyOrderElementsOf(
                        board.getStatuses().stream().map(BoardStatus::getId).toList()
                );
    }

    @Test
    void findById_MissingTask_ReturnsEmptyOptional() {
        assertThat(taskRepository.findById(Long.MAX_VALUE)).isEmpty();
    }

    @Test
    void saveTasksAcrossBoards_ThenFindAll_ReturnsAllPersistedTasks() {
        // Arrange
        Board firstBoard = createBoard("BoardOne");
        Board secondBoard = createBoard("BoardTwo");
        Task firstTask = saveTask(firstBoard, "TaskOne", null);
        Task secondTask = saveTask(secondBoard, "TaskTwo", secondBoard.getStatuses().get(1));

        // Act
        List<Task> result = taskRepository.findAll();

        // Assert
        assertThat(result)
                .extracting(Task::getId, Task::getBoardId, Task::getTitle, Task::getDescription)
                .containsExactlyInAnyOrder(
                        tuple(firstTask.getId(), firstBoard.getId(), "TaskOne", "Description for TaskOne"),
                        tuple(secondTask.getId(), secondBoard.getId(), "TaskTwo", "Description for TaskTwo")
                );
        assertThat(result).allSatisfy(task -> assertThat(task.getCreatedAt()).isNotNull());
        assertThat(result).filteredOn(task -> task.getId().equals(secondTask.getId()))
                .singleElement().satisfies(task ->
                        assertThat(task.getStatus().getId()).isEqualTo(secondBoard.getStatuses().get(1).getId())
                );
    }

    @Test
    void findAll_NoTasks_ReturnsEmptyList() {
        assertThat(taskRepository.findAll()).isEmpty();
    }

    @Test
    void findAllByBoardId_TasksOnDifferentBoards_ReturnsOnlyMatchingTasks() {
        // Arrange
        Board firstBoard = createBoard("BoardOne");
        Board secondBoard = createBoard("BoardTwo");
        Task firstTask = saveTask(firstBoard, "TaskOne", firstBoard.getStatuses().get(0));
        Task secondTask = saveTask(firstBoard, "TaskTwo", null);
        Task otherTask = saveTask(secondBoard, "OtherTask", null);

        // Act & Assert
        assertThat(taskRepository.findAllByBoardId(firstBoard.getId()))
                .extracting(Task::getId, Task::getBoardId, Task::getTitle)
                .containsExactlyInAnyOrder(
                        tuple(firstTask.getId(), firstBoard.getId(), "TaskOne"),
                        tuple(secondTask.getId(), firstBoard.getId(), "TaskTwo")
                );
        assertThat(taskRepository.findAllByBoardId(secondBoard.getId()))
                .extracting(Task::getId)
                .containsExactly(otherTask.getId());
    }

    @Test
    void findAllByBoardId_ExistingBoardWithoutTasks_ReturnsEmptyList() {
        // Arrange
        Board emptyBoard = createBoard("EmptyBoard");
        Board otherBoard = createBoard("OtherBoard");
        saveTask(otherBoard, "OtherTask", null);

        // Act & Assert
        assertThat(taskRepository.findAllByBoardId(emptyBoard.getId())).isEmpty();
    }

    @Test
    void findAllByBoardId_MissingBoard_ReturnsEmptyList() {
        // Arrange
        Board board = createBoard("BoardOne");
        saveTask(board, "TaskOne", null);

        // Act & Assert
        assertThat(taskRepository.findAllByBoardId(Long.MAX_VALUE)).isEmpty();
    }

    @Test
    void saveExistingTask_UpdatesFieldsWithoutChangingIdOrCreationTime() {
        // Arrange
        Board board = createBoard("BoardOne");
        Task savedTask = saveTask(board, "Original title", board.getStatuses().get(0));
        Task originalTask = taskRepository.findById(savedTask.getId()).orElseThrow();
        int taskCountBeforeUpdate = taskRepository.findAll().size();
        originalTask.changeTitle("Updated title");
        originalTask.setDescription("Updated description");

        // Act
        Task updatedTask = taskRepository.save(originalTask);
        Task foundTask = taskRepository.findById(savedTask.getId()).orElseThrow();

        // Assert
        assertThat(updatedTask.getId()).isEqualTo(savedTask.getId());
        assertThat(foundTask.getId()).isEqualTo(savedTask.getId());
        assertThat(foundTask.getBoardId()).isEqualTo(board.getId());
        assertThat(foundTask.getTitle()).isEqualTo("Updated title");
        assertThat(foundTask.getDescription()).isEqualTo("Updated description");
        assertThat(originalTask.getCreatedAt()).isNotNull();
        assertThat(foundTask.getCreatedAt()).isEqualTo(originalTask.getCreatedAt());
        assertThat(foundTask.getStatus().getId()).isEqualTo(board.getStatuses().get(0).getId());
        assertThat(taskRepository.findAll()).hasSize(taskCountBeforeUpdate);
    }

    @Test
    void saveExistingTask_WithDifferentStatus_PersistsNewStatus() {
        // Arrange
        Board board = createBoard("BoardOne");
        Task task = saveTask(board, "TaskOne", board.getStatuses().get(0));
        BoardStatus newStatus = board.getStatuses().get(1);
        task.setStatus(newStatus);

        // Act
        taskRepository.save(task);
        Task foundTask = taskRepository.findById(task.getId()).orElseThrow();

        // Assert
        assertThat(foundTask.getStatus().getId()).isEqualTo(newStatus.getId());
        assertThat(foundTask.getStatus().getName()).isEqualTo("Done");
        assertThat(foundTask.getStatus().getPosition()).isEqualTo(1);
        assertThat(taskRepository.findAll()).hasSize(1);
    }

    @Test
    void saveExistingTask_WithNullStatus_ClearsStatus() {
        // Arrange
        Board board = createBoard("BoardOne");
        Task task = saveTask(board, "TaskOne", board.getStatuses().get(0));
        assertThat(taskRepository.findById(task.getId()).orElseThrow().getStatus()).isNotNull();
        task.setStatus(null);

        // Act
        taskRepository.save(task);

        // Assert
        assertThat(taskRepository.findById(task.getId())).hasValueSatisfying(foundTask ->
                assertThat(foundTask.getStatus()).isNull()
        );
        assertThat(taskRepository.findAll()).hasSize(1);
    }

    @Test
    void deleteTask_RemovesTaskAndPreservesBoardAndStatus() {
        // Arrange
        Board board = createBoard("BoardOne");
        BoardStatus status = board.getStatuses().get(0);
        Task task = saveTask(board, "TaskOne", status);

        // Act
        taskRepository.deleteById(task.getId());

        // Assert
        assertThat(taskRepository.findById(task.getId())).isEmpty();
        assertThat(taskRepository.findAll()).isEmpty();
        assertThat(boardRepository.findById(board.getId())).hasValueSatisfying(foundBoard ->
                assertThat(foundBoard.getStatuses())
                        .extracting(BoardStatus::getId)
                        .containsExactlyInAnyOrderElementsOf(
                                board.getStatuses().stream().map(BoardStatus::getId).toList()
                        )
        );
    }

    @Test
    void saveTask_WithNullTitle_ThrowsDataIntegrityViolationException() {
        // Arrange
        Board board = createBoard("BoardOne");
        Task task = new Task(null, board.getId(), null, "Task without a title", null, null);

        // Act & Assert
        assertThatThrownBy(() -> taskRepository.save(task))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("title");
        assertThat(taskRepository.findAll()).isEmpty();
    }

    @Test
    void saveTask_WithNullDescription_ThrowsDataIntegrityViolationException() {
        // Arrange
        Board board = createBoard("BoardOne");
        Task task = new Task(null, board.getId(), "TaskOne", null, null, null);

        // Act & Assert
        assertThatThrownBy(() -> taskRepository.save(task))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("description");
        assertThat(taskRepository.findAll()).isEmpty();
    }

    @Test
    void saveTask_WithNullBoardId_ThrowsDataIntegrityViolationException() {
        // Arrange
        Task task = new Task(null, null, "TaskOne", "Task without a board", null, null);

        // Act & Assert
        assertThatThrownBy(() -> taskRepository.save(task))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(taskRepository.findAll()).isEmpty();
    }

    @Test
    void saveTask_WithMissingBoard_ThrowsDataIntegrityViolationException() {
        // Arrange
        Task task = new Task(null, Long.MAX_VALUE, "TaskOne", "Task with missing board", null, null);

        // Act & Assert
        assertThatThrownBy(() -> taskRepository.save(task))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(taskRepository.findAll()).isEmpty();
    }

    @Test
    void saveTask_WithMissingStatus_ThrowsDataIntegrityViolationException() {
        // Arrange
        Board board = createBoard("BoardOne");
        BoardStatus missingStatus = new BoardStatus(Long.MAX_VALUE, "Missing status", 0);
        Task task = new Task(null, board.getId(), "TaskOne", "Task with missing status", null, missingStatus);

        // Act & Assert
        assertThatThrownBy(() -> taskRepository.save(task))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(taskRepository.findAll()).isEmpty();
    }

    @Test
    void saveNewTask_WithStatusFromAnotherBoard_RejectsTask() {
        // Arrange
        Board firstBoard = createBoard("BoardOne");
        Board secondBoard = createBoard("BoardTwo");
        Task task = new Task(null, firstBoard.getId(), "TaskOne", "Wrong board status", null,
                secondBoard.getStatuses().get(0));

        // Act & Assert
        assertThatThrownBy(() -> taskRepository.save(task))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("task_board_status_fk");
        assertThat(taskRepository.findAll()).isEmpty();
    }

    @Test
    void saveExistingTask_WithStatusFromAnotherBoard_RejectsChangeAndPreservesOriginalStatus() {
        // Arrange
        Board firstBoard = createBoard("BoardOne");
        Board secondBoard = createBoard("BoardTwo");
        BoardStatus originalStatus = firstBoard.getStatuses().get(0);
        Task savedTask = saveTask(firstBoard, "TaskOne", originalStatus);
        Task originalTask = taskRepository.findById(savedTask.getId()).orElseThrow();
        savedTask.setStatus(secondBoard.getStatuses().get(0));

        // Act & Assert
        assertThatThrownBy(() -> taskRepository.save(savedTask))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("task_board_status_fk");
        assertThat(taskRepository.findById(savedTask.getId())).hasValueSatisfying(foundTask -> {
            assertThat(foundTask.getBoardId()).isEqualTo(firstBoard.getId());
            assertThat(foundTask.getStatus().getId()).isEqualTo(originalStatus.getId());
            assertThat(foundTask.getStatus().getName()).isEqualTo(originalStatus.getName());
            assertThat(foundTask.getStatus().getPosition()).isEqualTo(originalStatus.getPosition());
            assertThat(foundTask.getCreatedAt()).isEqualTo(originalTask.getCreatedAt());
        });
        assertThat(taskRepository.findAll()).hasSize(1);
    }

    private Board createBoard(String title) {
        return boardRepository.save(new Board(null, title, "Description for " + title, List.of(
                new BoardStatus(null, "To do", 0),
                new BoardStatus(null, "Done", 1)
        )));
    }

    private Task saveTask(Board board, String title, BoardStatus status) {
        return taskRepository.save(new Task(null, board.getId(), title, "Description for " + title, null, status));
    }
}
