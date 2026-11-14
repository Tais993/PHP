package nl.tijsbeek.mapper;

import nl.tijsbeek.entities.Board;
import nl.tijsbeek.entities.BoardJPA;
import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.entities.BoardStatusJPA;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public class BoardMapper {
    private BoardMapper() {}

    @Contract("_ -> new")
    public static @NonNull Board toDomain(@NonNull BoardJPA entity) {
        return new Board(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatuses().stream()
                        .map(status -> new BoardStatus(
                                status.getId(),
                                status.getName(),
                                status.getPosition()
                        ))
                        .toList()
        );
    }

    @Contract("_ -> new")
    public static @NonNull BoardJPA toJpa(@NonNull Board board) {
        BoardJPA entity = new BoardJPA();

        if (board.getId() != null && board.getId() > 0) {
            entity.setId(board.getId());
        }

        entity.setTitle(board.getTitle());
        entity.setDescription(board.getDescription());

        board.getStatuses()
                .stream()
                .map(BoardMapper::statusToJpa)
                .forEach(entity::addStatus);

        return entity;
    }

    private static BoardStatusJPA statusToJpa(BoardStatus status) {
        return new BoardStatusJPA(
                status.getId(),
                null,
                status.getName(),
                status.getPosition()
        );
    }
}
