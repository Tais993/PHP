package nl.tijsbeek.entities;

public class BoardStatus {
    public static final int MAX_NAME_LENGTH = 255;

    private final Long id;
    private final Long boardId;
    private String name;
    private int position;

    public BoardStatus(long boardId, String name, int position) {
        this(null, boardId, name, position);
    }

    public BoardStatus(Long id, Long boardId, String name, int position) {
          this.id = id;
        this.boardId = boardId;
        this.name = requireValidName(name);
        this.position = requireValidPosition(position);
    }

    private static String requireValidName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Board status name is required");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "Board status name cannot exceed " + MAX_NAME_LENGTH + " characters");
        }
        return name;
    }

    private static int requireValidPosition(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("Board status position must be non-negative");
        }
        return position;
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

    public void setName(String name) {
        this.name = requireValidName(name);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = requireValidPosition(position);
    }
}
