package logic.exception;

public class LabelException extends Exception {
    public LabelException(String message) {
        super(message);
    }

    public LabelException(String message, Throwable cause) {
        super(message, cause);
    }
}
