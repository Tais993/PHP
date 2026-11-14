package nl.tijsbeek.entities;

public class BoardStatus {

    private final Long id;
    private final String name;
    private final int position;

    public BoardStatus(Long id, String name, int position) {
        this.id = id;
        this.name = name;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }
}
