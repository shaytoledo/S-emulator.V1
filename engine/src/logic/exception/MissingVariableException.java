package logic.exception;

public class MissingVariableException extends Exception {
    public MissingVariableException(String message) {
        super(message);
    }

    public MissingVariableException(String message, Throwable cause) {
        super(message, cause);
    }
}
