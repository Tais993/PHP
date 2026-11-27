package nl.tijsbeek.dto.response;

import nl.tijsbeek.entities.Task;
import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        Long boardId,
        String title,
        String description,
        LocalDateTime createdAt,
        LocalDateTime dueDate,
        StatusResponse status) {

    public static @NonNull TaskResponse fromDomain(@NonNull Task task) {
        return new TaskResponse(
                task.getId(),
                task.getBoardId(),
                task.getTitle(),
                task.getDescription(),
                task.getCreatedAt(),
                task.getDueDate(),
                StatusResponse.fromDomain(task.getStatus())
        );
    }
}
