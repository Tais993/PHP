package nl.tijsbeek.integration.repository;

import nl.tijsbeek.entities.Board;
import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.integration.fixtures.IntegrationTestBase;
import nl.tijsbeek.interfaces.repositories.BoardRepository;
import nl.tijsbeek.interfaces.repositories.BoardViewRepository;
import nl.tijsbeek.testing.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@IntegrationTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class BoardRepositoryIntegrationTest extends IntegrationTestBase {

    private final BoardRepository boardRepository;
    private final BoardViewRepository boardViewRepository;
    private final JdbcTemplate jdbcTemplate;

    BoardRepositoryIntegrationTest(JdbcTemplate jdbcTemplate,
                                   BoardRepository boardRepository,
                                   BoardViewRepository boardViewRepository) {
        super(jdbcTemplate);
        this.boardRepository = boardRepository;
        this.boardViewRepository = boardViewRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void saveBoard_ThenFindById_ReturnsPersistedBoard() {
        // Arrange
        Board board = new Board(null, "BoardOne", "Board one for testing");

        // Act
        Board savedBoard = boardRepository.save(board);
        assertThat(savedBoard.getId()).isNotNull().isPositive();
        Optional<Board> result = boardRepository.findById(savedBoard.getId());

        // Assert
        assertThat(result).hasValueSatisfying(foundBoard -> {
            assertThat(foundBoard.getId()).isEqualTo(savedBoard.getId());
            assertThat(foundBoard.getTitle()).isEqualTo("BoardOne");
            assertThat(foundBoard.getDescription()).isEqualTo("Board one for testing");
            assertThat(foundBoard.getStatuses()).isEmpty();
        });
    }

    @Test
    void findById_MissingBoard_ReturnsEmptyOptional() {
        // Act
        Optional<Board> result = boardRepository.findById(Long.MAX_VALUE);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void saveBoards_ThenFindAll_ReturnsAllPersistedBoards() {
        // Arrange
        Board firstBoard = boardRepository.save(
                new Board(null, "BoardOne", "First board description")
        );
        Board secondBoard = boardRepository.save(
                new Board(null, "BoardTwo", "Second board description")
        );

        // Act
        List<Board> result = boardRepository.findAll();

        // Assert
        assertThat(result)
                .extracting(Board::getTitle, Board::getDescription)
                .containsExactlyInAnyOrder(
                        tuple("Inbox", "Existing tasks"),
                        tuple("BoardOne", "First board description"),
                        tuple("BoardTwo", "Second board description")
                );
        assertThat(result)
                .extracting(Board::getId)
                .contains(firstBoard.getId(), secondBoard.getId())
                .doesNotHaveDuplicates();
    }

    @Test
    void findAll_NoBoards_ReturnsEmptyList() {
        // Arrange
        for (Board board : boardRepository.findAll()) {
            boardRepository.deleteById(board.getId());
        }

        // Act
        List<Board> result = boardRepository.findAll();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void saveExistingBoard_UpdatesFieldsWithoutCreatingAnotherBoard() {
        // Arrange
        Board board = boardRepository.save(
                new Board(null, "Original title", "Original description")
        );
        int boardCountBeforeUpdate = boardRepository.findAll().size();
        board.changeTitle("Updated title");
        board.setDescription("Updated description");

        // Act
        Board updatedBoard = boardRepository.save(board);
        Optional<Board> result = boardRepository.findById(board.getId());

        // Assert
        assertThat(updatedBoard.getId()).isEqualTo(board.getId());
        assertThat(result).hasValueSatisfying(foundBoard -> {
            assertThat(foundBoard.getId()).isEqualTo(board.getId());
            assertThat(foundBoard.getTitle()).isEqualTo("Updated title");
            assertThat(foundBoard.getDescription()).isEqualTo("Updated description");
        });
        assertThat(boardRepository.findAll()).hasSize(boardCountBeforeUpdate);
    }

    @Test
    void saveBoardWithStatuses_ThenFindById_ReturnsPersistedStatuses() {
        // Arrange
        Board board = new Board(null, "BoardOne", "Board with statuses", List.of(
                new BoardStatus(null, null, "To do", 0),
                new BoardStatus(null, null, "Done", 1)
        ));

        // Act
        Board savedBoard = boardRepository.save(board);
        Board foundBoard = boardRepository.findById(savedBoard.getId()).orElseThrow();

        // Assert
        assertThat(savedBoard.getStatuses()).hasSize(2).allSatisfy(status ->
                assertThat(status.getId()).isNotNull().isPositive()
        );
        assertThat(foundBoard.getStatuses())
                .extracting(BoardStatus::getId)
                .containsExactlyInAnyOrderElementsOf(
                        savedBoard.getStatuses().stream().map(BoardStatus::getId).toList()
                )
                .doesNotHaveDuplicates();
        assertThat(foundBoard.getStatuses())
                .extracting(BoardStatus::getName, BoardStatus::getPosition)
                .containsExactlyInAnyOrder(tuple("To do", 0), tuple("Done", 1));
    }

    @Test
    void findById_StatusesSavedOutOfOrder_ReturnsStatusesOrderedByPosition() {
        // Arrange
        Board savedBoard = boardRepository.save(
                new Board(null, "BoardOne", "Board with unordered statuses", List.of(
                        new BoardStatus(null, null, "Done", 20),
                        new BoardStatus(null, null, "To do", 0),
                        new BoardStatus(null, null, "In progress", 10)
                ))
        );

        // Act
        Board foundBoard = boardRepository.findById(savedBoard.getId()).orElseThrow();

        // Assert
        assertThat(foundBoard.getStatuses())
                .extracting(BoardStatus::getName, BoardStatus::getPosition)
                .containsExactly(
                        tuple("To do", 0),
                        tuple("In progress", 10),
                        tuple("Done", 20)
                );
    }

    @Test
    void deleteBoard_ThenFindById_ReturnsEmptyOptional() {
        // Arrange
        Board savedBoard = boardRepository.save(
                new Board(null, "BoardOne", "Board to delete")
        );
        int boardCountBeforeDelete = boardRepository.findAll().size();

        // Act
        boardRepository.deleteById(savedBoard.getId());

        // Assert
        assertThat(boardRepository.findById(savedBoard.getId())).isEmpty();
        assertThat(boardRepository.findAll()).hasSize(boardCountBeforeDelete - 1);
    }

    @Test
    void deleteBoardWithStatuses_RemovesAssociatedStatuses() {
        // Arrange
        Board savedBoard = boardRepository.save(
                new Board(null, "BoardOne", "Board with statuses to delete", List.of(
                        new BoardStatus(null, null, "To do", 0),
                        new BoardStatus(null, null, "Done", 1)
                ))
        );
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM board_status WHERE board_id = ?",
                Long.class, savedBoard.getId()
        )).isEqualTo(2L);

        // Act
        boardRepository.deleteById(savedBoard.getId());

        // Assert
        assertThat(boardRepository.findById(savedBoard.getId())).isEmpty();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM board_status WHERE board_id = ?",
                Long.class, savedBoard.getId()
        )).isZero();
    }

    @Test
    void deleteBoardWithViews_RemovesAssociatedViews() {
        // Arrange
        Board board = boardRepository.save(new Board(null, "BoardOne", "Board to delete"));
        Board otherBoard = boardRepository.save(new Board(null, "OtherBoard", "Board to preserve"));
        BoardView firstView = saveView(board, "First view", 0);
        BoardView secondView = saveView(board, "Second view", 1);
        BoardView otherView = saveView(otherBoard, "Other view", 0);

        // Act
        boardRepository.deleteById(board.getId());

        // Assert
        assertThat(boardRepository.findById(board.getId())).isEmpty();
        assertThat(boardViewRepository.findById(firstView.getId())).isEmpty();
        assertThat(boardViewRepository.findById(secondView.getId())).isEmpty();
        assertThat(boardViewRepository.findAllByBoardId(board.getId())).isEmpty();
        assertThat(boardViewRepository.findAll())
                .extracting(BoardView::getId, BoardView::getBoardId, BoardView::getName, BoardView::getPosition)
                .containsExactly(tuple(otherView.getId(), otherBoard.getId(), "Other view", 0));
        assertThat(boardRepository.findById(otherBoard.getId())).hasValueSatisfying(foundBoard -> {
            assertThat(foundBoard.getTitle()).isEqualTo("OtherBoard");
            assertThat(foundBoard.getDescription()).isEqualTo("Board to preserve");
        });
    }

    @Test
    void saveBoard_WithNullTitle_ThrowsDataIntegrityViolationException() {
        // Arrange
        Board board = new Board(null, null, "Board without a title");
        int boardCountBeforeSave = boardRepository.findAll().size();

        // Act & Assert
        assertThatThrownBy(() -> boardRepository.save(board))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("title");
        assertThat(boardRepository.findAll()).hasSize(boardCountBeforeSave);
    }

    private BoardView saveView(Board board, String name, int position) {
        return boardViewRepository.save(new BoardView(null, board.getId(), name, BoardView.ViewType.BOARD,
                BoardView.GroupBy.STATUS, BoardView.SortBy.TITLE, BoardView.SortDirection.ASC, position));
    }
}
