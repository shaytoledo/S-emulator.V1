package application.model;

import core.engine.EngineImpl;

import java.util.List;
import java.util.Map;

public class DebugSession {
    public final String debugId;
    public final String username;
    public final String programName;
    public final boolean isMainProgram;
    public final Architecture architecture;
    public final int level;
    public final List<Long> inputs;
    public final EngineImpl engine;

    public volatile boolean finished = false;
    public volatile Map<String, Long> lastVariableState;
    public volatile int currentIndex = 0;
    public volatile long creditsRemaining;
    public volatile int lastCycleCount = 0; // tracks cumulative cycles to compute per-step delta

    public DebugSession(String debugId, String username, String programName, boolean isMainProgram,
                        Architecture architecture, int level, List<Long> inputs,
                        EngineImpl engine, long creditsRemaining) {
        this.debugId = debugId;
        this.username = username;
        this.programName = programName;
        this.isMainProgram = isMainProgram;
        this.architecture = architecture;
        this.level = level;
        this.inputs = inputs;
        this.engine = engine;
        this.creditsRemaining = creditsRemaining;
    }
}
