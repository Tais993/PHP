package nl.tijsbeek.interfaces.repositories;

import nl.tijsbeek.entities.Board;
import nl.tijsbeek.entities.Task;

import java.util.List;
import java.util.Optional;

public interface BoardRepository {

    Optional<Board> findById(long id);

    List<Board> findAll();

    Board save(Board board);

    void deleteById(long id);
}