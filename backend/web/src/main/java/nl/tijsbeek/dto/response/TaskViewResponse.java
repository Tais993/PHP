package nl.tijsbeek.dto.response;

import nl.tijsbeek.results.TaskViewResult;
import org.jspecify.annotations.NonNull;

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
        public static @NonNull Group fromDomain(TaskViewResult.Group group) {
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

    public static @NonNull TaskViewResponse fromDomain(TaskViewResult result) {
        LinkedHashMap<Long, Group> groups = new LinkedHashMap<>();

        result.groups().forEach((id, group) ->
                groups.put(id, Group.fromDomain(group))
        );

        return new TaskViewResponse(result.groupedBy().toString(), groups);
    }
}