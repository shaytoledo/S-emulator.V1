package logic.exception;

public class LoadProgramException extends RuntimeException {
  public LoadProgramException(String message) {
    super(message);
  }

  public LoadProgramException(String message, Throwable cause) {
    super(message, cause);
  }
}
