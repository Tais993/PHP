package nl.tijsbeek.mapper;

import nl.tijsbeek.entities.BoardJPA;
import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.BoardViewJPA;
import org.jspecify.annotations.NonNull;
import org.springframework.lang.Contract;

public class BoardViewMapper {
    private BoardViewMapper() {}

    @Contract("_ -> new")
    public static @NonNull BoardView toDomain(@NonNull BoardViewJPA entity) {
        return new BoardView(
                entity.getId(),
                entity.getBoard().getId(),
                entity.getName(),
                entity.getType(),
                entity.getGroupBy(),
                entity.getSortBy(),
                entity.getSortDirection(),
                entity.getPosition(),
                entity.getFilterRules()
        );
    }

    @Contract("_, _ -> new")
    public static @NonNull BoardViewJPA toJpa(@NonNull BoardView view, @NonNull BoardJPA board) {
        return new BoardViewJPA(
                view.getId(),
                board,
                view.getName(),
                view.getType(),
                view.getGroupBy(),
                view.getSortBy(),
                view.getSortDirection(),
                view.getPosition(),
                view.getFilterRules()
        );
    }
}
