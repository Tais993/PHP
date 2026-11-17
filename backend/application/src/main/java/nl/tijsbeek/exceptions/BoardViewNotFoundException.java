package nl.tijsbeek.exceptions;

public class BoardViewNotFoundException extends RuntimeException {

    public BoardViewNotFoundException(long id) {
        super("Board view with ID " + id + " was not found");
    }
}
