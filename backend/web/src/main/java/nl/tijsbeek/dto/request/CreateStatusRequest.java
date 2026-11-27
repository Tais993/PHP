package nl.tijsbeek.dto.request;

import nl.tijsbeek.entities.BoardStatus;

public record CreateStatusRequest(
        long boardId,
        String name,
        int position
) {

    public BoardStatus toDomain() {
        return new BoardStatus(boardId, name, position);
    }
}
