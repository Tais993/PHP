package nl.tijsbeek.dto.response;

import nl.tijsbeek.entities.BoardView;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record BoardViewResponse(
        Long id,
        Long boardId,
        String name,
        String type,
        String groupBy,
        String sortBy,
        String sortDirection,
        int position,
        List<FilterRuleResponse> filterRules
) {

    @Contract("_ -> new")
    public static @NotNull BoardViewResponse fromDomain(@NonNull BoardView boardView) {
        return new BoardViewResponse(
                boardView.getId(),
                boardView.getBoardId(),
                boardView.getName(),
                boardView.getType().toString(),
                boardView.getGroupBy().toString(),
                boardView.getSortBy().toString(),
                boardView.getSortDirection().toString(),
                boardView.getPosition(),
                boardView.getFilterRules().stream()
                        .map(FilterRuleResponse::fromDomain)
                        .toList()
        );
    }
}
