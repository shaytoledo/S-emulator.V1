package core.engine;

import core.program.VariableAndLabelMenger;
import dto.*;
import javafx.util.Pair;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;


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
    Pair<Map<String, Long>,Integer> startDebug(int currentLevel, List<Long> inputsByOrder);
    Pair<Map<String, Long>,Integer> oneStepInDebug();
    void endDebug();
    Map<String, Long> resumeDebug();
    int getCycels();
    }

