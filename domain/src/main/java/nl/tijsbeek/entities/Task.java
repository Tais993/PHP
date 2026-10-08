package nl.tijsbeek.entities;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class Task {
    public static final int MAX_TITLE_LENGTH = 255;

    private final Long id;
    private String title;
    private String description;
    private final LocalDateTime createdAt;
    private String status;

    public Task(Long id, String title, String description, LocalDateTime createdAt, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.status = status;
    }


    public void changeTitle(@NotNull String newTitle) {
        if (newTitle == null || newTitle.isBlank()) {
            throw new IllegalArgumentException(
                    "Task title cannot be empty"
            );
        }

        if (newTitle.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "Task title cannot exceed 255 characters"
            );
        }

        this.title = newTitle;
    }


    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
