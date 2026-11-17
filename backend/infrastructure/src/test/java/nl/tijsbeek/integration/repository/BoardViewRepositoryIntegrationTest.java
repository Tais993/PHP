package nl.tijsbeek.integration.repository;

import nl.tijsbeek.entities.Board;
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
class BoardViewRepositoryIntegrationTest extends IntegrationTestBase {

    private final BoardViewRepository boardViewRepository;
    private final BoardRepository boardRepository;

    BoardViewRepositoryIntegrationTest(JdbcTemplate jdbcTemplate,
                                       BoardViewRepository boardViewRepository,
                                       BoardRepository boardRepository) {
        super(jdbcTemplate);
        this.boardViewRepository = boardViewRepository;
        this.boardRepository = boardRepository;
    }

    @Test
    void saveView_ThenFindById_ReturnsPersistedView() {
        // Arrange
        Board board = createBoard("BoardOne");
        BoardView view = new BoardView(null, board.getId(), "Board view", BoardView.ViewType.BOARD,
                BoardView.GroupBy.STATUS, BoardView.SortBy.CREATED_AT, BoardView.SortDirection.DESC, 3);

        // Act
        BoardView savedView = boardViewRepository.save(view);
        Optional<BoardView> result = boardViewRepository.findById(savedView.getId());

        // Assert
        assertThat(savedView.getId()).isNotNull().isPositive();
        assertThat(result).hasValueSatisfying(foundView -> {
            assertThat(foundView.getId()).isEqualTo(savedView.getId());
            assertThat(foundView.getBoardId()).isEqualTo(board.getId());
            assertThat(foundView.getName()).isEqualTo("Board view");
            assertThat(foundView.getType()).isEqualTo(BoardView.ViewType.BOARD);
            assertThat(foundView.getGroupBy()).isEqualTo(BoardView.GroupBy.STATUS);
            assertThat(foundView.getSortBy()).isEqualTo(BoardView.SortBy.CREATED_AT);
            assertThat(foundView.getSortDirection()).isEqualTo(BoardView.SortDirection.DESC);
            assertThat(foundView.getPosition()).isEqualTo(3);
        });
    }

    @Test
    void findById_MissingView_ReturnsEmptyOptional() {
        // Act
        Optional<BoardView> result = boardViewRepository.findById(Long.MAX_VALUE);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void saveExistingView_UpdatesFieldsWithoutChangingIdOrBoard() {
        // Arrange
        Board board = createBoard("BoardOne");
        BoardView savedView = saveView(board, "Original name", 0);
        int viewCountBeforeUpdate = boardViewRepository.findAll().size();
        savedView.setName("Updated name");
        savedView.setSortBy(BoardView.SortBy.CREATED_AT);
        savedView.setSortDirection(BoardView.SortDirection.DESC);
        savedView.setPosition(4);

        // Act
        BoardView updatedView = boardViewRepository.save(savedView);
        BoardView foundView = boardViewRepository.findById(savedView.getId()).orElseThrow();

        // Assert
        assertThat(updatedView.getId()).isEqualTo(savedView.getId());
        assertThat(updatedView.getBoardId()).isEqualTo(board.getId());
        assertThat(foundView.getId()).isEqualTo(savedView.getId());
        assertThat(foundView.getBoardId()).isEqualTo(board.getId());
        assertThat(foundView.getName()).isEqualTo("Updated name");
        assertThat(foundView.getType()).isEqualTo(BoardView.ViewType.BOARD);
        assertThat(foundView.getGroupBy()).isEqualTo(BoardView.GroupBy.STATUS);
        assertThat(foundView.getSortBy()).isEqualTo(BoardView.SortBy.CREATED_AT);
        assertThat(foundView.getSortDirection()).isEqualTo(BoardView.SortDirection.DESC);
        assertThat(foundView.getPosition()).isEqualTo(4);
        assertThat(boardViewRepository.findAll()).hasSize(viewCountBeforeUpdate);
    }

    @Test
    void saveViewsAcrossBoards_ThenFindAll_ReturnsAllPersistedViews() {
        // Arrange
        Board firstBoard = createBoard("BoardOne");
        Board secondBoard = createBoard("BoardTwo");
        BoardView firstView = saveView(firstBoard, "First view", 0);
        BoardView secondView = saveView(firstBoard, "Second view", 1);
        BoardView otherView = saveView(secondBoard, "Other view", 0);

        // Act
        List<BoardView> result = boardViewRepository.findAll();

        // Assert
        assertThat(result)
                .extracting(BoardView::getId, BoardView::getBoardId, BoardView::getName, BoardView::getPosition)
                .containsExactlyInAnyOrder(
                        tuple(firstView.getId(), firstBoard.getId(), "First view", 0),
                        tuple(secondView.getId(), firstBoard.getId(), "Second view", 1),
                        tuple(otherView.getId(), secondBoard.getId(), "Other view", 0)
                );
        assertThat(result).extracting(BoardView::getId).doesNotHaveDuplicates();
    }

    @Test
    void findAll_NoViews_ReturnsEmptyList() {
        // Act
        List<BoardView> result = boardViewRepository.findAll();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findAllByBoardId_ViewsOnDifferentBoards_ReturnsOnlyMatchingViews() {
        // Arrange
        Board firstBoard = createBoard("BoardOne");
        Board secondBoard = createBoard("BoardTwo");
        BoardView firstView = saveView(firstBoard, "First view", 0);
        BoardView secondView = saveView(firstBoard, "Second view", 1);
        BoardView otherView = saveView(secondBoard, "Other view", 0);

        // Act & Assert
        assertThat(boardViewRepository.findAllByBoardId(firstBoard.getId()))
                .extracting(BoardView::getId, BoardView::getBoardId, BoardView::getName, BoardView::getPosition)
                .containsExactly(
                        tuple(firstView.getId(), firstBoard.getId(), "First view", 0),
                        tuple(secondView.getId(), firstBoard.getId(), "Second view", 1)
                );
        assertThat(boardViewRepository.findAllByBoardId(secondBoard.getId()))
                .extracting(BoardView::getId, BoardView::getBoardId, BoardView::getName, BoardView::getPosition)
                .containsExactly(tuple(otherView.getId(), secondBoard.getId(), "Other view", 0));
    }

    @Test
    void findAllByBoardId_ViewsSavedOutOfOrder_ReturnsViewsOrderedByPositionThenId() {
        // Arrange
        Board board = createBoard("BoardOne");
        BoardView lastView = saveView(board, "Last view", 20);
        BoardView firstView = saveView(board, "First view", 0);
        BoardView secondView = saveView(board, "Second view", 0);

        // Act
        List<BoardView> result = boardViewRepository.findAllByBoardId(board.getId());

        // Assert
        assertThat(result)
                .extracting(BoardView::getId, BoardView::getName, BoardView::getPosition)
                .containsExactly(
                        tuple(firstView.getId(), "First view", 0),
                        tuple(secondView.getId(), "Second view", 0),
                        tuple(lastView.getId(), "Last view", 20)
                );
    }

    @Test
    void findAllByBoardId_ExistingBoardWithoutViews_ReturnsEmptyList() {
        // Arrange
        Board emptyBoard = createBoard("EmptyBoard");
        Board otherBoard = createBoard("OtherBoard");
        saveView(otherBoard, "Other view", 0);

        // Act & Assert
        assertThat(boardViewRepository.findAllByBoardId(emptyBoard.getId())).isEmpty();
    }

    @Test
    void findAllByBoardId_MissingBoard_ReturnsEmptyList() {
        // Arrange
        Board board = createBoard("BoardOne");
        saveView(board, "Board view", 0);

        // Act & Assert
        assertThat(boardViewRepository.findAllByBoardId(Long.MAX_VALUE)).isEmpty();
    }

    @Test
    void deleteView_RemovesViewAndPreservesBoardAndOtherViews() {
        // Arrange
        Board board = createBoard("BoardOne");
        BoardView view = saveView(board, "View to delete", 0);
        BoardView remainingView = saveView(board, "Remaining view", 1);

        // Act
        boardViewRepository.deleteById(view.getId());

        // Assert
        assertThat(boardViewRepository.findById(view.getId())).isEmpty();
        assertThat(boardViewRepository.findAll())
                .extracting(BoardView::getId, BoardView::getBoardId, BoardView::getName, BoardView::getPosition)
                .containsExactly(tuple(remainingView.getId(), board.getId(), "Remaining view", 1));
        assertThat(boardRepository.findById(board.getId())).hasValueSatisfying(foundBoard -> {
            assertThat(foundBoard.getTitle()).isEqualTo("BoardOne");
            assertThat(foundBoard.getDescription()).isEqualTo("Description for BoardOne");
        });
    }

    @Test
    void saveView_WithMissingBoard_ThrowsDataIntegrityViolationException() {
        // Arrange
        Board board = createBoard("BoardOne");
        BoardView existingView = saveView(board, "Existing view", 0);
        BoardView invalidView = newView(Long.MAX_VALUE, "Orphan view", 0);

        // Act & Assert
        assertThatThrownBy(() -> boardViewRepository.save(invalidView))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(boardViewRepository.findAll())
                .extracting(BoardView::getId, BoardView::getBoardId, BoardView::getName, BoardView::getPosition)
                .containsExactly(tuple(existingView.getId(), board.getId(), "Existing view", 0));
    }

    @Test
    void saveView_WithNullName_ThrowsDataIntegrityViolationException() {
        // Arrange
        Board board = createBoard("BoardOne");
        BoardView existingView = saveView(board, "Existing view", 0);
        BoardView invalidView = newView(board.getId(), null, 1);

        // Act & Assert
        assertThatThrownBy(() -> boardViewRepository.save(invalidView))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("name");
        assertThat(boardViewRepository.findAll())
                .extracting(BoardView::getId, BoardView::getBoardId, BoardView::getName, BoardView::getPosition)
                .containsExactly(tuple(existingView.getId(), board.getId(), "Existing view", 0));
    }

    @Test
    void saveView_WithBlankName_ThrowsDataIntegrityViolationException() {
        // Arrange
        Board board = createBoard("BoardOne");
        BoardView existingView = saveView(board, "Existing view", 0);
        BoardView invalidView = newView(board.getId(), "   ", 1);

        // Act & Assert
        assertThatThrownBy(() -> boardViewRepository.save(invalidView))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("board_view_name_check");
        assertThat(boardViewRepository.findAll())
                .extracting(BoardView::getId, BoardView::getBoardId, BoardView::getName, BoardView::getPosition)
                .containsExactly(tuple(existingView.getId(), board.getId(), "Existing view", 0));
    }

    @Test
    void saveView_WithNegativePosition_ThrowsDataIntegrityViolationException() {
        // Arrange
        Board board = createBoard("BoardOne");
        BoardView existingView = saveView(board, "Existing view", 0);
        BoardView invalidView = newView(board.getId(), "Invalid position", -1);

        // Act & Assert
        assertThatThrownBy(() -> boardViewRepository.save(invalidView))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("position");
        assertThat(boardViewRepository.findAll())
                .extracting(BoardView::getId, BoardView::getBoardId, BoardView::getName, BoardView::getPosition)
                .containsExactly(tuple(existingView.getId(), board.getId(), "Existing view", 0));
    }

    private Board createBoard(String title) {
        return boardRepository.save(new Board(null, title, "Description for " + title));
    }

    private BoardView saveView(Board board, String name, int position) {
        return boardViewRepository.save(newView(board.getId(), name, position));
    }

    private BoardView newView(long boardId, String name, int position) {
        return new BoardView(null, boardId, name, BoardView.ViewType.BOARD,
                BoardView.GroupBy.STATUS, BoardView.SortBy.TITLE, BoardView.SortDirection.ASC, position);
    }
}
