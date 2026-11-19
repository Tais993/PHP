package nl.tijsbeek.dto.response;

import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.BoardView.GroupBy;
import nl.tijsbeek.entities.BoardView.SortBy;
import nl.tijsbeek.entities.BoardView.SortDirection;
import nl.tijsbeek.entities.BoardView.ViewType;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record BoardViewResponse(
        Long id,
        Long boardId,
        String name,
        ViewType type,
        GroupBy groupBy,
        SortBy sortBy,
        SortDirection sortDirection,
        int position,
        List<FilterRuleResponse> filterRules
) {

    public static @NonNull BoardViewResponse fromDomain(@NonNull BoardView boardView) {
        return new BoardViewResponse(
                boardView.getId(),
                boardView.getBoardId(),
                boardView.getName(),
                boardView.getType(),
                boardView.getGroupBy(),
                boardView.getSortBy(),
                boardView.getSortDirection(),
                boardView.getPosition(),
                boardView.getFilterRules().stream()
                        .map(FilterRuleResponse::fromDomain)
                        .toList()
        );
    }
}
