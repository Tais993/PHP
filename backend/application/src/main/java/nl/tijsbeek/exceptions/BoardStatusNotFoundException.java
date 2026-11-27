package nl.tijsbeek.exceptions;

public class BoardStatusNotFoundException extends RuntimeException {

    public BoardStatusNotFoundException(long id) {
        super("Status with ID " + id + " was not found");
    }
}