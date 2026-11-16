package nl.tijsbeek.mapper;

import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.entities.BoardStatusJPA;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public class BoardStatusMapper {

    @Contract("_ -> new")
    public static @NonNull BoardStatusJPA toJpa(@NonNull BoardStatus status) {
        return new BoardStatusJPA(
                status.getId(),
                null,
                status.getName(),
                status.getPosition()
        );
    }

    @Contract("_ -> new")
    public static @NonNull BoardStatus toDomain(@NonNull BoardStatusJPA entity) {
        return new BoardStatus(entity.getId(), entity.getName(), entity.getPosition());
    }
}
