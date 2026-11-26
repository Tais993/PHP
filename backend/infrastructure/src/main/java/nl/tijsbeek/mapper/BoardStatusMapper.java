package nl.tijsbeek.mapper;

import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.entities.BoardStatusJPA;
import org.jspecify.annotations.NonNull;
import org.springframework.lang.Contract;

public class BoardStatusMapper {
    private BoardStatusMapper() {}

    @Contract("_ -> new")
    public static @NonNull BoardStatusJPA toJpa(@NonNull BoardStatus status) {
        return new BoardStatusJPA(
                status.getId(),
                null,
                status.getName(),
                status.getPosition()
        );
    }

    public static @NonNull BoardStatus toDomain(@NonNull BoardStatusJPA entity) {
        return new BoardStatus(entity.getId(), entity.getBoard().getId(), entity.getName(), entity.getPosition());
    }
}
