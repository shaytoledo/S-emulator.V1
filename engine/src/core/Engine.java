package core;

import dto.*;
import java.nio.file.Path;
import java.util.List;


// The engine API, determines which methods the engine has
public interface Engine {

    LoadReport loadProgram(Path xmlPath);
    ProgramSummary getProgramSummary();
    void expandToLevel(int level);
    RunResult run(int level, List<Long> inputs, List<String> varsNames);
    List<RunSummary> getHistory();
}

