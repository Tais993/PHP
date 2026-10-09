package nl.tijsbeek.mapper;

import nl.tijsbeek.entities.Task;
import nl.tijsbeek.entities.TaskJPA;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public class TaskMapper {
    private TaskMapper() {}

    @Contract("_ -> new")
    public static @NonNull Task toDomain(@NonNull TaskJPA entity) {
        return new Task(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getStatus()
        );
    }

    public static @NonNull TaskJPA toJpa(@NonNull Task task) {
        TaskJPA entity = new TaskJPA();

        if (task.getId() != null && task.getId() > 0) {
            entity.setId(task.getId());
        }

        entity.setTitle(task.getTitle());
        entity.setDescription(task.getDescription());
        entity.setStatus(task.getStatus());

        return entity;
    }
}