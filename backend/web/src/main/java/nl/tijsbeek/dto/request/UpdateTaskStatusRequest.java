package nl.tijsbeek.dto.request;

import nl.tijsbeek.interfaces.services.TaskService.ChangeTaskStatus;

public record UpdateTaskStatusRequest(
        Long statusId
) {
    public ChangeTaskStatus toDomain(long taskId) {
        return new ChangeTaskStatus(taskId, statusId);
    }
}
