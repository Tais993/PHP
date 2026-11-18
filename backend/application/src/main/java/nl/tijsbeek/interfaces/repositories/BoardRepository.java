package nl.tijsbeek.interfaces.repositories;

import nl.tijsbeek.entities.Board;

import java.util.List;
import java.util.Optional;

public interface BoardRepository {

    boolean existsById(long id);

    Optional<Board> findById(long id);

    List<Board> findAll();

    Board save(Board board);

    void deleteById(long id);
}