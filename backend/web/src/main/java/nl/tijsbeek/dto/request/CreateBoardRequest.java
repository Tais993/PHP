package nl.tijsbeek.dto.request;

import nl.tijsbeek.entities.Board;

public record CreateBoardRequest(
        String title,
        String description
) {
    public Board toDomain() {
        return new Board(null, title, description);
    }
}
