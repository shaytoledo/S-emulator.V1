package logic.exception;

public class FunctionNotExist extends RuntimeException {
    public FunctionNotExist(String message) {
        super(message);
    }
}
