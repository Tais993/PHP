package nl.tijsbeek.services;

import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.FilterRule;
import nl.tijsbeek.exceptions.BoardViewNotFoundException;
import nl.tijsbeek.exceptions.InvalidFilterRulesException;
import nl.tijsbeek.interfaces.repositories.BoardStatusRepository;
import nl.tijsbeek.interfaces.repositories.BoardViewRepository;
import nl.tijsbeek.interfaces.services.BoardViewService;
import org.jetbrains.annotations.Contract;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BoardViewServiceImpl implements BoardViewService {

    private final BoardViewRepository boardViewRepository;
    private final BoardStatusRepository boardStatusRepository;

    @Contract(pure = true)
    public BoardViewServiceImpl(BoardViewRepository boardViewRepository, BoardStatusRepository boardStatusRepository) {
        this.boardViewRepository = boardViewRepository;
        this.boardStatusRepository = boardStatusRepository;
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

    @Override
    public BoardView replaceFilterRules(long viewId, List<FilterRule> rules) {
        BoardView view = boardViewRepository.findById(viewId)
                .orElseThrow(() -> new BoardViewNotFoundException(viewId));

        List<FilterRule> replacement = List.copyOf(rules);

        List<Long> statusIds = replacement.stream()
                .filter(rule -> rule.field() == FilterRule.FilterField.STATUS)
                .flatMap(rule -> rule.values().stream())
                .map(Long::parseLong)
                .distinct()
                .toList();

        for (long statusId : statusIds) {
            if (boardStatusRepository.findByIdAndboardId(statusId, view.getBoardId()).isEmpty()) {
                throw new InvalidFilterRulesException(
                        "Status " + statusId + " does not exist on board " + view.getBoardId()
                );
            }
        }

        view.setFilterRules(replacement);
        return boardViewRepository.save(view);
    }
}
