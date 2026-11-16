package nl.tijsbeek.services;

import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.interfaces.repositories.BoardViewRepository;
import nl.tijsbeek.interfaces.services.BoardViewService;
import org.jetbrains.annotations.Contract;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BoardViewServiceImpl implements BoardViewService {

    private final BoardViewRepository boardViewRepository;

    @Contract(pure = true)
    public BoardViewServiceImpl(BoardViewRepository boardViewRepository) {
        this.boardViewRepository = boardViewRepository;
    }


    @Override
    public List<BoardView> getBoardViews(long boardId) {
        return boardViewRepository.findAllByBoardId(boardId);
    }

    @Override
    public void deleteBoardView(long boardViewId) {
        boardViewRepository.deleteById(boardViewId);
    }

    @Override
    public Optional<BoardView> getBoardView(long boardViewId) {
        return boardViewRepository.findById(boardViewId);
    }

    @Override
    public BoardView createBoardView(BoardView boardView) {
        return boardViewRepository.save(boardView);
    }
}
