package nl.tijsbeek.entities;

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
        this(null, boardId, title, description, null, dueDate, status);
    }

    public Task(Long id, Long boardId, String title, String description, LocalDateTime createdAt, LocalDateTime dueDate, BoardStatus status) {
        this.id = id;
        this.boardId = Objects.requireNonNull(boardId, "Task board ID is required");
        this.title = requireValidTitle(title);
        this.description = Objects.requireNonNull(description, "Task description is required");
        this.createdAt = createdAt;
        this.dueDate = dueDate;
        this.status = requireValidStatus(boardId, status);
    }


    public void changeTitle(String newTitle) {
        this.title = requireValidTitle(newTitle);
    }

    private static String requireValidTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title is required");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "Task title cannot exceed " + MAX_TITLE_LENGTH + " characters");
        }
        return title;
    }

    private static BoardStatus requireValidStatus(Long boardId, BoardStatus status) {
        if (status != null && !Objects.equals(boardId, status.getBoardId())) {
            throw new IllegalArgumentException("Task status must belong to the task's board");
        }
        return status;
    }


    public Long getId() {
        return id;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        Objects.requireNonNull(boardId, "Task board ID is required");
        requireValidStatus(boardId, status);
        this.boardId = boardId;
    }

    public String getTitle() {
        return title;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Objects.requireNonNull(description, "Task description is required");
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
        this.status = requireValidStatus(boardId, status);
    }
}
