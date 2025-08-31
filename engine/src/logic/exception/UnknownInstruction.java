package logic.exception;

public class UnknownInstruction extends RuntimeException {
    public UnknownInstruction(String message) {
        super(message);
    }
}