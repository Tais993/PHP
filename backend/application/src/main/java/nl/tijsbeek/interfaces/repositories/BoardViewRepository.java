package nl.tijsbeek.interfaces.repositories;

import nl.tijsbeek.entities.BoardView;

import java.util.List;
import java.util.Optional;

public interface BoardViewRepository {

    Optional<BoardView> findById(long id);

    List<BoardView> findAll();

    List<BoardView> findAllByBoardId(long boardId);

    BoardView save(BoardView view);

    void deleteById(long id);
}
