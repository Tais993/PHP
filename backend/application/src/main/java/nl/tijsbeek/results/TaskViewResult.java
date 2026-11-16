package nl.tijsbeek.results;

import nl.tijsbeek.entities.BoardView;
import nl.tijsbeek.entities.Task;

import java.util.List;

public record TaskViewResult(
        BoardView.GroupBy groupedBy,
        List<Group> groups
) {
    public record Group(
            Long id,
            String name,
            int position,
            List<Task> items
    ) {}
}