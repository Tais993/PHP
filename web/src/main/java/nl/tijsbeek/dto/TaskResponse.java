package nl.tijsbeek.dto;

import nl.tijsbeek.entities.Task;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        LocalDateTime createdAt,
        String status) {


    public static TaskResponse fromDomain(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getCreatedAt(),
                task.getStatus()
        );
    }
}
