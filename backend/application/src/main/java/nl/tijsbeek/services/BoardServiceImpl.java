package nl.tijsbeek.services;

import nl.tijsbeek.entities.Board;
import nl.tijsbeek.interfaces.repositories.BoardRepository;
import nl.tijsbeek.interfaces.services.BoardService;
import org.jetbrains.annotations.Contract;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;

    @Contract(pure = true)
    public BoardServiceImpl(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Override
    public Board createBoard(String title, String description) {
        Board board = new Board(null, title, description);

        return boardRepository.save(board);
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
