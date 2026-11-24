package nl.tijsbeek.integration.repository;

import nl.tijsbeek.entities.Board;
import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.entities.Task;
import nl.tijsbeek.integration.fixtures.IntegrationTestBase;
import nl.tijsbeek.interfaces.repositories.BoardRepository;
import nl.tijsbeek.interfaces.repositories.BoardStatusRepository;
import nl.tijsbeek.interfaces.repositories.TaskRepository;
import nl.tijsbeek.testing.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@IntegrationTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class BoardStatusRepositoryIntegrationTest extends IntegrationTestBase {

    private final BoardStatusRepository boardStatusRepository;
    private final BoardRepository boardRepository;
    private final TaskRepository taskRepository;
    private final JdbcTemplate jdbcTemplate;

    BoardStatusRepositoryIntegrationTest(JdbcTemplate jdbcTemplate,
                                         BoardStatusRepository boardStatusRepository,
                                         BoardRepository boardRepository,
                                         TaskRepository taskRepository) {
        super(jdbcTemplate);
        this.boardStatusRepository = boardStatusRepository;
        this.boardRepository = boardRepository;
        this.taskRepository = taskRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void findById_MissingStatus_ReturnsEmptyOptional() {
        assertThat(boardStatusRepository.findById(Long.MAX_VALUE)).isEmpty();
    }

    @Test
    void saveStatusesAcrossBoards_ThenFindAll_ReturnsAllPersistedStatuses() {
        // Arrange
        Board firstBoard = createBoard("BoardOne");
        Board secondBoard = createBoard("BoardTwo");
        BoardStatus firstStatus = saveStatus(firstBoard, "To do", 0);
        BoardStatus secondStatus = saveStatus(firstBoard, "Done", 1);
        BoardStatus otherStatus = saveStatus(secondBoard, "In progress", 2);

        // Act
        List<BoardStatus> result = boardStatusRepository.findAll();

        // Assert
        assertThat(result)
                .extracting(BoardStatus::getId, BoardStatus::getBoardId, BoardStatus::getName, BoardStatus::getPosition)
                .containsExactlyInAnyOrder(
                        tuple(firstStatus.getId(), firstBoard.getId(), "To do", 0),
                        tuple(secondStatus.getId(), firstBoard.getId(), "Done", 1),
                        tuple(otherStatus.getId(), secondBoard.getId(), "In progress", 2)
                );
        assertThat(result).extracting(BoardStatus::getId).doesNotHaveDuplicates();
    }

    @Test
    void findAll_NoStatuses_ReturnsEmptyList() {
        assertThat(boardStatusRepository.findAll()).isEmpty();
    }

    @Test
    void findAllByBoardId_StatusesOnDifferentBoards_ReturnsOnlyMatchingStatuses() {
        // Arrange
        Board firstBoard = createBoard("BoardOne");
        Board secondBoard = createBoard("BoardTwo");
        BoardStatus firstStatus = saveStatus(firstBoard, "To do", 0);
        BoardStatus secondStatus = saveStatus(firstBoard, "Done", 1);
        BoardStatus otherStatus = saveStatus(secondBoard, "To do", 2);

        // Act & Assert
        assertThat(boardStatusRepository.findAllByBoardId(firstBoard.getId()))
                .extracting(BoardStatus::getId, BoardStatus::getBoardId, BoardStatus::getName, BoardStatus::getPosition)
                .containsExactlyInAnyOrder(
                        tuple(firstStatus.getId(), firstBoard.getId(), "To do", 0),
                        tuple(secondStatus.getId(), firstBoard.getId(), "Done", 1)
                );
        assertThat(boardStatusRepository.findAllByBoardId(secondBoard.getId()))
                .extracting(BoardStatus::getId, BoardStatus::getBoardId, BoardStatus::getName, BoardStatus::getPosition)
                .containsExactly(tuple(otherStatus.getId(), secondBoard.getId(), "To do", 2));
    }

    @Test
    void findAllByBoardId_ExistingBoardWithoutStatuses_ReturnsEmptyList() {
        // Arrange
        Board emptyBoard = createBoard("EmptyBoard");
        Board otherBoard = createBoard("OtherBoard");
        saveStatus(otherBoard, "To do", 0);

        // Act & Assert
        assertThat(boardStatusRepository.findAllByBoardId(emptyBoard.getId())).isEmpty();
    }

    @Test
    void saveStatus_ThenFindById_ReturnsPersistedStatusWithCorrectBoard() {
        // Arrange
        Board board = createBoard("BoardOne");
        BoardStatus status = new BoardStatus(null, board.getId(), "To do", 2);

        // Act
        BoardStatus savedStatus = boardStatusRepository.save(status);
        BoardStatus foundStatus = boardStatusRepository.findById(savedStatus.getId()).orElseThrow();

        // Assert
        assertThat(savedStatus.getId()).isNotNull().isPositive();
        assertThat(savedStatus.getBoardId()).isEqualTo(board.getId());
        assertThat(savedStatus.getName()).isEqualTo("To do");
        assertThat(savedStatus.getPosition()).isEqualTo(2);
        assertThat(foundStatus.getId()).isEqualTo(savedStatus.getId());
        assertThat(foundStatus.getBoardId()).isEqualTo(board.getId());
        assertThat(foundStatus.getName()).isEqualTo("To do");
        assertThat(foundStatus.getPosition()).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT board_id FROM board_status WHERE id = ?", Long.class, savedStatus.getId()
        )).isEqualTo(board.getId());
    }

    @Test
    void saveExistingStatus_UpdatesFieldsWithoutChangingIdOrBoard() {
        // Arrange
        Board board = createBoard("BoardOne");
        BoardStatus savedStatus = saveStatus(board, "Original name", 0);
        int statusCountBeforeUpdate = boardStatusRepository.findAll().size();
        BoardStatus changedStatus = new BoardStatus(savedStatus.getId(), board.getId(), "Updated name", 3);

        // Act
        BoardStatus updatedStatus = boardStatusRepository.save(changedStatus);
        BoardStatus foundStatus = boardStatusRepository.findById(savedStatus.getId()).orElseThrow();

        // Assert
        assertThat(updatedStatus.getId()).isEqualTo(savedStatus.getId());
        assertThat(updatedStatus.getBoardId()).isEqualTo(board.getId());
        assertThat(foundStatus.getId()).isEqualTo(savedStatus.getId());
        assertThat(foundStatus.getBoardId()).isEqualTo(board.getId());
        assertThat(foundStatus.getName()).isEqualTo("Updated name");
        assertThat(foundStatus.getPosition()).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT board_id FROM board_status WHERE id = ?", Long.class, savedStatus.getId()
        )).isEqualTo(board.getId());
        assertThat(boardStatusRepository.findAll()).hasSize(statusCountBeforeUpdate);
    }

    @Test
    void saveStatus_WithNullName_ThrowsDataIntegrityViolationException() {
        // Arrange
        Board board = createBoard("BoardOne");
        BoardStatus existingStatus = saveStatus(board, "To do", 0);
        BoardStatus invalidStatus = new BoardStatus(null, board.getId(), null, 1);

        // Act & Assert
        assertThatThrownBy(() -> boardStatusRepository.save(invalidStatus))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("name");
        assertThat(boardStatusRepository.findAll())
                .extracting(BoardStatus::getId, BoardStatus::getBoardId, BoardStatus::getName, BoardStatus::getPosition)
                .containsExactly(tuple(existingStatus.getId(), board.getId(), "To do", 0));
    }

    @Test
    void saveStatus_WithNegativePosition_ThrowsDataIntegrityViolationException() {
        // Arrange
        Board board = createBoard("BoardOne");
        BoardStatus existingStatus = saveStatus(board, "To do", 0);
        BoardStatus invalidStatus = new BoardStatus(null, board.getId(), "Done", -1);

        // Act & Assert
        assertThatThrownBy(() -> boardStatusRepository.save(invalidStatus))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("position");
        assertThat(boardStatusRepository.findAll())
                .extracting(BoardStatus::getId, BoardStatus::getBoardId, BoardStatus::getName, BoardStatus::getPosition)
                .containsExactly(tuple(existingStatus.getId(), board.getId(), "To do", 0));
    }

    @Test
    void saveStatus_WithDuplicateNameOnSameBoard_ThrowsDataIntegrityViolationException() {
        // Arrange
        Board board = createBoard("BoardOne");
        BoardStatus existingStatus = saveStatus(board, "To do", 0);
        BoardStatus duplicateStatus = new BoardStatus(null, board.getId(), "To do", 1);

        // Act & Assert
        assertThatThrownBy(() -> boardStatusRepository.save(duplicateStatus))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(boardStatusRepository.findAll())
                .extracting(BoardStatus::getId, BoardStatus::getBoardId, BoardStatus::getName, BoardStatus::getPosition)
                .containsExactly(tuple(existingStatus.getId(), board.getId(), "To do", 0));
    }

    @Test
    void saveStatuses_WithSameNameOnDifferentBoards_PersistsBothStatuses() {
        // Arrange
        Board firstBoard = createBoard("BoardOne");
        Board secondBoard = createBoard("BoardTwo");
        BoardStatus firstStatus = saveStatus(firstBoard, "To do", 0);

        // Act
        BoardStatus secondStatus = saveStatus(secondBoard, "To do", 0);

        // Assert
        assertThat(secondStatus.getId()).isNotNull().isPositive().isNotEqualTo(firstStatus.getId());
        assertThat(boardStatusRepository.findAll())
                .extracting(BoardStatus::getId, BoardStatus::getBoardId, BoardStatus::getName, BoardStatus::getPosition)
                .containsExactlyInAnyOrder(
                        tuple(firstStatus.getId(), firstBoard.getId(), "To do", 0),
                        tuple(secondStatus.getId(), secondBoard.getId(), "To do", 0)
                );
    }

    @Test
    void saveStatus_WithMissingBoard_ThrowsDataIntegrityViolationException() {
        // Arrange
        BoardStatus status = new BoardStatus(null, Long.MAX_VALUE, "To do", 0);

        // Act & Assert
        assertThatThrownBy(() -> boardStatusRepository.save(status))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(boardStatusRepository.findAll()).isEmpty();
    }

    @Test
    void deleteStatus_RemovesStatusAndPreservesBoardAndOtherStatuses() {
        // Arrange
        Board board = createBoard("BoardOne");
        BoardStatus status = saveStatus(board, "To do", 0);
        BoardStatus remainingStatus = saveStatus(board, "Done", 1);

        // Act
        boardStatusRepository.deleteById(status.getId());

        // Assert
        assertThat(boardStatusRepository.findById(status.getId())).isEmpty();
        assertThat(boardStatusRepository.findAll())
                .extracting(BoardStatus::getId, BoardStatus::getBoardId, BoardStatus::getName, BoardStatus::getPosition)
                .containsExactly(tuple(remainingStatus.getId(), board.getId(), "Done", 1));
        assertThat(boardRepository.findById(board.getId())).hasValueSatisfying(foundBoard -> {
            assertThat(foundBoard.getTitle()).isEqualTo("BoardOne");
            assertThat(foundBoard.getDescription()).isEqualTo("Description for BoardOne");
            assertThat(foundBoard.getStatuses()).extracting(BoardStatus::getId)
                    .containsExactly(remainingStatus.getId());
        });
    }

    @Test
    void deleteStatus_ReferencedByTask_RejectsDeletionAndPreservesTaskAndStatus() {
        // Arrange
        Board board = createBoard("BoardOne");
        BoardStatus status = saveStatus(board, "To do", 0);
        Task task = taskRepository.save(new Task(
                board.getId(), "TaskOne", "Task with a status", null, status
        ));
        Task originalTask = taskRepository.findById(task.getId()).orElseThrow();

        // Act & Assert
        assertThatThrownBy(() -> boardStatusRepository.deleteById(status.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(boardStatusRepository.findById(status.getId())).hasValueSatisfying(foundStatus -> {
            assertThat(foundStatus.getBoardId()).isEqualTo(board.getId());
            assertThat(foundStatus.getName()).isEqualTo("To do");
            assertThat(foundStatus.getPosition()).isZero();
        });
        assertThat(taskRepository.findById(task.getId())).hasValueSatisfying(foundTask -> {
            assertThat(foundTask.getBoardId()).isEqualTo(board.getId());
            assertThat(foundTask.getTitle()).isEqualTo("TaskOne");
            assertThat(foundTask.getDescription()).isEqualTo("Task with a status");
            assertThat(originalTask.getCreatedAt()).isNotNull();
            assertThat(foundTask.getCreatedAt()).isEqualTo(originalTask.getCreatedAt());
            assertThat(foundTask.getStatus().getId()).isEqualTo(status.getId());
            assertThat(foundTask.getStatus().getBoardId()).isEqualTo(board.getId());
        });
        assertThat(boardStatusRepository.findAll()).hasSize(1);
        assertThat(taskRepository.findAll()).hasSize(1);
    }

    private Board createBoard(String title) {
        return boardRepository.save(new Board(null, title, "Description for " + title));
    }

    private BoardStatus saveStatus(Board board, String name, int position) {
        return boardStatusRepository.save(new BoardStatus(null, board.getId(), name, position));
    }
}
