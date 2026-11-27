package nl.tijsbeek.services;

import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.exceptions.BoardStatusNotFoundException;
import nl.tijsbeek.interfaces.repositories.BoardStatusRepository;
import nl.tijsbeek.interfaces.services.BoardStatusService;
import nl.tijsbeek.interfaces.services.FieldUpdate;
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
    public BoardStatus updateBoardStatus(long boardStatusId, FieldUpdate<String> name, FieldUpdate<Integer> position) {
        BoardStatus boardStatus = boardStatusRepository.findById(boardStatusId)
                .orElseThrow(() -> new BoardStatusNotFoundException(boardStatusId));

        if (name.supplied()) {
            boardStatus.setName(name.value());
        }

        if (position.supplied()) {
            boardStatus.setPosition(position.value());
        }

        return boardStatusRepository.save(boardStatus);
    }

    @Override
    public void deleteBoardStatus(long boardStatusId) {

    }
}
