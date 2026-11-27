package nl.tijsbeek.entities;

public class BoardStatus {

    private final Long id;
    private final Long boardId;
    private final String name;
    private final int position;

    public BoardStatus(Long boardId, String name, int position) {
        this(null, boardId, name, position);
    }

    public BoardStatus(Long id, Long boardId, String name, int position) {
        this.id = id;
        this.boardId = boardId;
        this.name = name;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public Long getBoardId() {
        return boardId;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }
}
