package nl.tijsbeek.mapper;

import nl.tijsbeek.entities.Board;
import nl.tijsbeek.entities.BoardJPA;
import org.jspecify.annotations.NonNull;
import org.springframework.lang.Contract;

public class BoardMapper {
    private BoardMapper() {}

    @Contract("_ -> new")
    public static @NonNull Board toDomain(@NonNull BoardJPA entity) {
        return new Board(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatuses().stream()
                        .map(BoardStatusMapper::toDomain)
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
                .map(BoardStatusMapper::toJpa)
                .forEach(entity::addStatus);

        return entity;
    }
}
