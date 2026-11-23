package nl.tijsbeek.dto.request;

import java.time.LocalDateTime;

public record CreateTaskRequest(
        String title,
        Long boardId,
        String description,
        LocalDateTime dueDate,
        Long statusId
) {
}
