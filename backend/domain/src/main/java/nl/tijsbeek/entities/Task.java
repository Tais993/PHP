package nl.tijsbeek.entities;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    public static final int MAX_TITLE_LENGTH = 255;

    private final Long id;
    private Long boardId;
    private String title;
    private String description;
    private final LocalDateTime createdAt;
    private LocalDateTime dueDate;
    private BoardStatus status;

    public Task(Long boardId, String title, String description, LocalDateTime dueDate, BoardStatus status) {
        this.id = null;
        this.boardId = boardId;
        this.title = title;
        this.description = description;
        this.createdAt = null;
        this.dueDate = dueDate;
        this.status = status;
    }

    public Task(Long id, Long boardId, String title, String description, LocalDateTime createdAt, LocalDateTime dueDate, BoardStatus status) {
        this.id = id;
        this.boardId = boardId;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
        this.status = status;
    }


    public void changeTitle(@NotNull String newTitle) {
        Objects.requireNonNull(newTitle);

        if (newTitle.isBlank()) {
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

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
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

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public BoardStatus getStatus() {
        return status;
    }

    public void setStatus(BoardStatus status) {
        this.status = status;
    }
}
