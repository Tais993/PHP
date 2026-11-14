package nl.tijsbeek.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import static nl.tijsbeek.entities.Task.MAX_TITLE_LENGTH;

@Entity
@Table(name = "task")
public class TaskJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(name = "title", nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status")
    private BoardStatusJPA status;

    public TaskJPA() {
        // Required by JPA
    }

    public TaskJPA(Long id, Long boardId, String title, String description,
                   LocalDateTime createdAt, BoardStatusJPA status) {
        this.id = id;
        this.boardId = boardId;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public long getBoardId() {
        return boardId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public BoardStatusJPA getStatus() {
        return status;
    }
}
