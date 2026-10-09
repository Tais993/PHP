package nl.tijsbeek.dto;

public record CreateTaskRequest(
        String title,
        String description,
        String status) {
}
