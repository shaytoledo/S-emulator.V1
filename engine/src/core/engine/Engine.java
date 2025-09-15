package core.engine;

import core.program.VariableAndLabelMenger;
import dto.*;

import java.nio.file.Path;
import java.util.List;


// The engine API, determines which methods the engine has
public interface Engine {

    LoadReport loadProgram(Path xmlPath);
    ProgramSummary getProgramSummaryForShow();
    List<List<InstructionView>> expandProgramToLevelForExtend(int level);
    List<InstructionView> expandProgramToLevelForRun(int level);
    RunResult run(int level, List<Long> inputs);
    List<RunSummary> getHistory();
    int getMaxExpandLevel();
    VariableAndLabelMenger getVlm();
    List<List<String>> getInfoForEachInstruction(int level);
}

