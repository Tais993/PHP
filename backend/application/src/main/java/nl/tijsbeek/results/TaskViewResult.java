package nl.tijsbeek.results;

import nl.tijsbeek.entities.BoardStatus;
import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.Task;
import org.jspecify.annotations.NonNull;
import org.springframework.lang.Contract;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public record TaskViewResult(
        BoardView.GroupBy groupedBy,
        LinkedHashMap<Long, Group> groups
) {
    public record Group(
            Long id,
            String name,
            int position,
            ArrayList<Task>  tasks
    ) {

        @Contract("_ -> new")
        public static @NonNull Group fromStatus(@NonNull BoardStatus status) {
            return new Group(status.getId(), status.getName(), status.getPosition(), new ArrayList<>());
        }
    }
}