package nl.tijsbeek.services;

import nl.tijsbeek.entities.Board;
import nl.tijsbeek.interfaces.repositories.BoardRepository;
import nl.tijsbeek.interfaces.services.BoardService;
import nl.tijsbeek.interfaces.services.BoardStatusService;
import nl.tijsbeek.interfaces.services.BoardViewService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static nl.tijsbeek.entities.EntityDefaults.defaultBoardView;
import static nl.tijsbeek.entities.EntityDefaults.defaultKanbanBoardStatuses;

@Service
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final BoardViewService boardViewService;
    private final BoardStatusService boardStatusService;

    public BoardServiceImpl(BoardRepository boardRepository, BoardViewService boardViewService, BoardStatusService boardStatusService) {
        this.boardRepository = boardRepository;
        this.boardViewService = boardViewService;
        this.boardStatusService = boardStatusService;
    }

    @Override
    public Board createBoard(Board board) {
        Board savedBoard = boardRepository.save(board);

        boardViewService.createBoardView(defaultBoardView(savedBoard.getId()));
        boardStatusService.createBoardStatuses(defaultKanbanBoardStatuses(savedBoard.getId()));

        return savedBoard;
    }

    @Override
    public Optional<Board> getBoard(long id) {
        return boardRepository.findById(id);
    }

    @Override
    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    @Override
    public void deleteBoard(long id) {
        boardRepository.deleteById(id);
    }
}
