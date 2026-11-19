package nl.tijsbeek.exceptions;

public class InvalidFilterRulesException extends IllegalArgumentException {
    public InvalidFilterRulesException(String message) {
        super(message);
    }
}
