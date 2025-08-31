package logic.exception;

import java.io.FileNotFoundException;

public class ProgramFileNotFoundException extends FileNotFoundException {
    public ProgramFileNotFoundException(String msg) {
        super(msg);
    }
}

