package nl.tijsbeek.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "board_status")
public class BoardStatusJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardJPA board;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "position", nullable = false)
    private int position;

    public BoardStatusJPA() {
        // Required by JPA
    }

    public BoardStatusJPA(Long id, BoardJPA board, String name, int position) {
        this.id = id;
        this.board = board;
        this.name = name;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BoardJPA getBoard() {
        return board;
    }

    public void setBoard(BoardJPA board) {
        this.board = board;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
