package nl.tijsbeek.interfaces.repositories;

import nl.tijsbeek.entities.BoardStatus;

import java.util.List;
import java.util.Optional;

public interface BoardStatusRepository {

    Optional<BoardStatus> findById(long id);

    Optional<BoardStatus> findByIdAndboardId(long statusId, long boardId);

    List<BoardStatus> findAll();

    List<BoardStatus> findAllByBoardId(long boardId);

    BoardStatus save(BoardStatus boardStatus);

    void deleteById(long id);
}