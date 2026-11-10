package nl.tijsbeek.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static nl.tijsbeek.entities.Task.MAX_TITLE_LENGTH;

@Entity
@Table(name = "board")
public class BoardJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    public BoardJPA() {
        // Required by JPA
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}