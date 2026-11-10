package nl.tijsbeek.entities;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Board {
    public static final int MAX_TITLE_LENGTH = 255;

    private final Long id;
    private String title;
    private String description;

    @Contract(pure = true)
    public Board(Long id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
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

    public String getTitle() {
        return title;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
