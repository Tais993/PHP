package nl.tijsbeek.dto;

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
        String status) {


    @Contract("_ -> new")
    public static @NonNull TaskResponse fromDomain(@NonNull Task task) {
        return new TaskResponse(
                task.getId(),
                task.getBoardId(),
                task.getTitle(),
                task.getDescription(),
                task.getCreatedAt(),
                task.getStatus()
        );
    }
}
