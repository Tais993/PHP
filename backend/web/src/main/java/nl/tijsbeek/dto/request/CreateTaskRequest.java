package nl.tijsbeek.dto.request;

import nl.tijsbeek.interfaces.services.TaskService.CreateTask;

import java.time.LocalDateTime;

public record CreateTaskRequest(
        String title,
        Long boardId,
        String description,
        LocalDateTime dueDate,
        Long statusId
) {
    public CreateTask toDomain() {
        return new CreateTask(title, boardId, description, statusId);
    }
}
