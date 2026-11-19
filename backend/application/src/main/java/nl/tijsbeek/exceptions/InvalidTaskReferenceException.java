package nl.tijsbeek.exceptions;

public class InvalidTaskReferenceException extends RuntimeException {

    public InvalidTaskReferenceException(String message) {
        super(message);
    }
}
