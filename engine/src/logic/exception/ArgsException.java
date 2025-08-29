package logic.exception;

public class ArgsException extends Exception {
    public ArgsException(String message) {
        super(message);
    }

    public ArgsException(String message, Throwable cause) {
        super(message, cause);
    }}
