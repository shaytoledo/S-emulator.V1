package logic.exception;

public class MissingFunctionName extends RuntimeException {
    public MissingFunctionName(String message) {
        super(message);
    }
}
