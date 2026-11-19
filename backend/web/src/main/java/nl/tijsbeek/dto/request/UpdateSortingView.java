package nl.tijsbeek.dto.request;

import nl.tijsbeek.entities.BoardView;

public record UpdateSortingView(BoardView.SortBy sortBy, BoardView.SortDirection sortDirection) {
}
