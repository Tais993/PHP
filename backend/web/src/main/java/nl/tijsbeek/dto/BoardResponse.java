package nl.tijsbeek.dto;

import nl.tijsbeek.entities.Board;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public record BoardResponse(
        Long id,
        String title,
        String description) {


    @Contract("_ -> new")
    public static @NonNull BoardResponse fromDomain(@NonNull Board board) {
        return new BoardResponse(
                board.getId(),
                board.getTitle(),
                board.getDescription()
        );
    }
}
