package nl.tijsbeek.dto;

import nl.tijsbeek.results.TaskViewResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record TaskViewResponse(String groupedBy,
                               List<TaskViewResponse.Group> groups
) {
    public record Group(
            Long id,
            String name,
            int position,
            List<TaskResponse> items
    ) {

        @Contract("_ -> new")
        public static @NotNull Group fromDomain(nl.tijsbeek.results.TaskViewResult.Group group) {
            return new Group(group.id(), group.name(), group.position(), group.items()
                    .stream()
                    .map(TaskResponse::fromDomain)
                    .toList());
        }
    }


    @Contract("_ -> new")
    public static @NotNull TaskViewResponse fromDomain(TaskViewResult result) {
        return new TaskViewResponse(result.groupedBy().toString(), result.groups()
                .stream()
                .map(TaskViewResponse.Group::fromDomain)
                .toList());
    }
}