package nl.tijsbeek.interfaces.services;

import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.FilterRule;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BoardViewService {


    @Transactional
    List<BoardView> getBoardViews(final long boardId);

    @Transactional
    void deleteBoardView(final long boardViewId);

    @Transactional
    Optional<BoardView> getBoardView(final long boardViewId);

    @Transactional
    BoardView createBoardView(final BoardView boardView);

    @Transactional
    BoardView updateSortingOfBoardView(long viewId, BoardView.SortBy sortBy, BoardView.SortDirection sortDirection);

    @Transactional
    BoardView replaceFilterRules(long viewId, List<FilterRule> rules);
}
