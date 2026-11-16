package nl.tijsbeek.dto;

import nl.tijsbeek.results.TaskViewResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;

public record TaskViewResponse(
        String groupedBy,
        LinkedHashMap<Long, Group> groups
) {
    public record Group(
            Long id,
            String name,
            int position,
            List<TaskResponse> items
    ) {
        @Contract("_ -> new")
        public static @NotNull Group fromDomain(TaskViewResult.Group group) {
            return new Group(
                    group.id(),
                    group.name(),
                    group.position(),
                    group.tasks().stream()
                            .map(TaskResponse::fromDomain)
                            .toList()
            );
        }
    }

    @Contract("_ -> new")
    public static @NotNull TaskViewResponse fromDomain(TaskViewResult result) {
        LinkedHashMap<Long, Group> groups = new LinkedHashMap<>();

        result.groups().forEach((id, group) ->
                groups.put(id, Group.fromDomain(group))
        );

        return new TaskViewResponse(result.groupedBy().toString(), groups);
    }
}