package nl.tijsbeek.dto;

public record CreateTaskRequest(
        String title,
        Long boardId,
        String description,
        String status) {
}
