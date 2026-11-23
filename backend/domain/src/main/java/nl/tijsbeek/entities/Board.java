package nl.tijsbeek.entities;

import java.util.List;
import java.util.Objects;

public class Board {
    public static final int MAX_TITLE_LENGTH = 255;

    private final Long id;
    private String title;
    private String description;
    private final List<BoardStatus> statuses;

    public Board(Long id, String title, String description) {
        this(id, title, description, List.of());
    }

    public Board(Long id, String title, String description, List<BoardStatus> statuses) {
        this.id = id;
        this.title = requireValidTitle(title);
        this.description = Objects.requireNonNull(description, "Board description is required");
        this.statuses = requireValidStatuses(id, statuses);
    }


    public void changeTitle(String newTitle) {
        this.title = requireValidTitle(newTitle);
    }

    private static String requireValidTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Board title is required");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "Board title cannot exceed " + MAX_TITLE_LENGTH + " characters");
        }
        return title;
    }

    private static List<BoardStatus> requireValidStatuses(Long boardId, List<BoardStatus> statuses) {
        List<BoardStatus> copy = List.copyOf(statuses);
        for (BoardStatus status : copy) {
            Long statusBoardId = status.getBoardId();
            if (statusBoardId != null && !statusBoardId.equals(boardId)) {
                throw new IllegalArgumentException("Board status must belong to the board");
            }
        }
        return copy;
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

    public List<BoardStatus> getStatuses() {
        return statuses;
    }

    public void setDescription(String description) {
        this.description = Objects.requireNonNull(description, "Board description is required");
    }
}
