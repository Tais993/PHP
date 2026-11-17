package nl.tijsbeek.exceptions;

public class BoardNotFoundException extends RuntimeException {

    public BoardNotFoundException(long id) {
        super("Board with ID " + id + " was not found");
    }
}
