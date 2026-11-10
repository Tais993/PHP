package nl.tijsbeek.interfaces.services;

import nl.tijsbeek.entities.Board;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BoardService {

    @Transactional
    Board createBoard(String title, String description);

    @Transactional
    Optional<Board> getBoard(long id);

    @Transactional
    List<Board> getAllBoards();

    @Transactional
    void deleteBoard(long id);
}
