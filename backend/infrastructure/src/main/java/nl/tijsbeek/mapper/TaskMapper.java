package nl.tijsbeek.mapper;

import nl.tijsbeek.entities.BoardStatusJPA;
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
                entity.getBoardId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getDueDate(),
                entity.getStatus() == null ? null : BoardStatusMapper.toDomain(entity.getStatus())
        );
    }

    @Contract("_, _ -> new")
    public static @NonNull TaskJPA toJpa(@NonNull Task task, BoardStatusJPA status) {
        return new TaskJPA(
                task.getId(),
                task.getBoardId(),
                task.getTitle(),
                task.getDescription(),
                task.getCreatedAt(),
                task.getDueDate(),
                status
        );
    }
}
