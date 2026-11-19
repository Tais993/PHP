package nl.tijsbeek.dto.response;

import nl.tijsbeek.entities.Board;
import org.jspecify.annotations.NonNull;

public record BoardResponse(
        Long id,
        String title,
        String description) {


    public static @NonNull BoardResponse fromDomain(@NonNull Board board) {
        return new BoardResponse(
                board.getId(),
                board.getTitle(),
                board.getDescription()
        );
    }
}
