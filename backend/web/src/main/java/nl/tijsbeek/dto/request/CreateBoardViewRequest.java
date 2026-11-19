package nl.tijsbeek.dto.request;

import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.BoardView.GroupBy;
import nl.tijsbeek.entities.BoardView.SortBy;
import nl.tijsbeek.entities.BoardView.SortDirection;
import nl.tijsbeek.entities.BoardView.ViewType;
import org.jspecify.annotations.NonNull;

public record CreateBoardViewRequest(
        Long boardId,
        String name,
        ViewType type,
        GroupBy groupBy,
        SortBy sortBy,
        SortDirection sortDirection,
        int position
) {

    public @NonNull BoardView toDomain() {
        return new BoardView(boardId, name, type, groupBy, sortBy, sortDirection, position);
    }
}
