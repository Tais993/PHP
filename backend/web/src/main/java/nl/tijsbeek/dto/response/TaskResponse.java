package nl.tijsbeek.dto.response;

import nl.tijsbeek.entities.Task;
import org.jetbrains.annotations.Contract;
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

    public record StatusResponse(Long id, String name, int position) {}


    @Contract("_ -> new")
    public static @NonNull TaskResponse fromDomain(@NonNull Task task) {
        return new TaskResponse(
                task.getId(),
                task.getBoardId(),
                task.getTitle(),
                task.getDescription(),
                task.getCreatedAt(),
                task.getDueDate(),
                task.getStatus() == null ? null : new StatusResponse(
                        task.getStatus().getId(),
                        task.getStatus().getName(),
                        task.getStatus().getPosition()
                )
        );
    }
}
