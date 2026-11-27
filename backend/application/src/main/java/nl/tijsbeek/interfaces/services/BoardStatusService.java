package nl.tijsbeek.interfaces.services;

import nl.tijsbeek.entities.BoardStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BoardStatusService {

    @Transactional
    BoardStatus createBoardStatus(BoardStatus boardStatus);

    @Transactional
    List<BoardStatus> createBoardStatuses(List<BoardStatus> boardStatuses);

    @Transactional
    Optional<BoardStatus> getBoardStatus(long boardStatusId);

    @Transactional
    List<BoardStatus> getAllBoardStatuses();

    @Transactional
    List<BoardStatus> getAllByBoardId(long boardId);

    @Transactional
    void deleteBoardStatus(long boardStatusId);
}
