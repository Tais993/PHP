package nl.tijsbeek.services;

import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.interfaces.repositories.BoardStatusRepository;
import nl.tijsbeek.interfaces.services.BoardStatusService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BoardStatusServiceImpl implements BoardStatusService {

    private final BoardStatusRepository boardStatusRepository;

    public BoardStatusServiceImpl(BoardStatusRepository boardStatusRepository) {
        this.boardStatusRepository = boardStatusRepository;
    }

    @Override
    public BoardStatus createBoardStatus(BoardStatus boardStatus) {
        return boardStatusRepository.save(boardStatus);
    }

    @Override
    public List<BoardStatus> createBoardStatuses(List<BoardStatus> boardStatuses) {
        return boardStatusRepository.saveAll(boardStatuses);
    }

    @Override
    public Optional<BoardStatus> getBoardStatus(long boardStatusId) {
        return Optional.empty();
    }

    @Override
    public List<BoardStatus> getAllBoardStatuses() {
        return List.of();
    }

    @Override
    public List<BoardStatus> getAllByBoardId(long boardId) {
        return List.of();
    }

    @Override
    public void deleteBoardStatus(long boardStatusId) {

    }
}
