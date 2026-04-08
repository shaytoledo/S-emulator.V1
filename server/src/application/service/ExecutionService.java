package application.service;

import application.model.Architecture;
import application.model.DebugSession;
import application.model.ProgramEntry;
import application.model.UserInfo;
import core.engine.EngineImpl;
import dto.InstructionView;
import dto.LoadReport;
import dto.Pair;
import dto.RunResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class ExecutionService {

    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final ConcurrentHashMap<String, Future<RunResult>> pendingRuns = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RunResult> completedRuns = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> runMeta = new ConcurrentHashMap<>(); // runId → metadata

    private final ConcurrentHashMap<String, DebugSession> debugSessions = new ConcurrentHashMap<>();

    // -----------------------------------------------------------------------
    // Run (non-debug)
    // -----------------------------------------------------------------------

    /**
     * Validate that all instructions in the program at the given level are
     * supported by the architecture, and return unsupported instruction names.
     * Uses the actual Instruction.getName() which returns InstructionData names
     * (e.g. "INCREASE", "JNZ", "ASSIGNMENT") — not human-readable command text.
     */
    public List<String> getUnsupportedCommands(ProgramEntry entry, int level, Architecture arch) {
        List<String> unsupported = new ArrayList<>();
        try {
            entry.program.instructionViewsAfterExtendRunShow(level); // expand to level first
            List<logic.instruction.Instruction> instructions = entry.program.getInstructions();
            if (instructions == null) return unsupported;
            for (logic.instruction.Instruction instr : instructions) {
                String name = instr.getName();
                if (name != null && !arch.supports(name)) {
                    unsupported.add(name);
                }
            }
        } catch (Exception ignored) {}
        return unsupported;
    }

    /**
     * Submit a run. Charges architecture cost up-front, then cycles per step.
     * Returns runId for polling, or throws if credits insufficient.
     */
    public String submitRun(UserInfo user, ProgramEntry entry, boolean isMainProgram,
                            Architecture arch, int level, List<Long> inputs,
                            UserRegistry userRegistry) {
        String runId = UUID.randomUUID().toString();

        Future<RunResult> future = pool.submit(() -> {
            EngineImpl engine = buildEngine(entry, userRegistry);
            RunResult result = engine.run(level, inputs);

            // Record in program stats
            long cyclesUsed = result == null ? 0 : result.totalCycles();
            long totalCost = arch.cost + cyclesUsed;
            entry.recordRun(totalCost);

            // Deduct cycle credits (arch cost already deducted before submit)
            user.tryDeductCredits(cyclesUsed);

            // Add history entry
            user.incrementRunCount();
            user.addHistoryEntry(isMainProgram, entry.getName(), arch.name(),
                    level, result == null ? 0 : result.y(), cyclesUsed);

            return result;
        });

        pendingRuns.put(runId, future);
        return runId;
    }

    public enum PollStatus { PENDING, DONE, ERROR }

    public record PollResult(PollStatus status, RunResult result, String error) {}

    public PollResult poll(String runId) {
        RunResult done = completedRuns.get(runId);
        if (done != null) return new PollResult(PollStatus.DONE, done, null);

        Future<RunResult> future = pendingRuns.get(runId);
        if (future == null) return new PollResult(PollStatus.ERROR, null, "Run not found: " + runId);

        if (future.isDone()) {
            try {
                RunResult result = future.get();
                completedRuns.put(runId, result);
                pendingRuns.remove(runId);
                return new PollResult(PollStatus.DONE, result, null);
            } catch (Exception e) {
                return new PollResult(PollStatus.ERROR, null, e.getMessage());
            }
        }

        return new PollResult(PollStatus.PENDING, null, null);
    }

    // -----------------------------------------------------------------------
    // Debug
    // -----------------------------------------------------------------------

    public record StartDebugResult(String debugId, Map<String, Long> variables, int currentIndex,
                                   long credits, boolean finished) {}

    public StartDebugResult startDebug(UserInfo user, ProgramEntry entry, boolean isMainProgram,
                                       Architecture arch, int level, List<Long> inputs,
                                       UserRegistry userRegistry) {
        EngineImpl engine = buildEngine(entry, userRegistry);

        Pair<Map<String, Long>, Integer> state = engine.startDebug(level, inputs);
        Map<String, Long> vars = state.getKey();
        int idx = state.getValue();

        String debugId = UUID.randomUUID().toString();
        DebugSession session = new DebugSession(debugId, user.name, entry.getName(), isMainProgram,
                arch, level, inputs, engine, user.getCredits());
        session.lastVariableState = vars;
        session.currentIndex = idx;
        session.finished = (idx == -1);
        debugSessions.put(debugId, session);

        return new StartDebugResult(debugId, vars, idx, user.getCredits(), idx == -1);
    }

    public record StepResult(Map<String, Long> variables, int currentIndex, long credits,
                              boolean finished, boolean outOfCredits) {}

    public StepResult stepDebug(String debugId, UserInfo user) {
        DebugSession session = debugSessions.get(debugId);
        if (session == null) throw new IllegalArgumentException("Debug session not found: " + debugId);

        Pair<Map<String, Long>, Integer> result = session.engine.oneStepInDebug();
        Map<String, Long> vars = result.getKey();
        int idx = result.getValue();

        // Deduct credits equal to cycles consumed by this step (delta from last known count)
        int nowCycles = session.engine.getCycels();
        int deltaCycles = Math.max(0, nowCycles - session.lastCycleCount);
        session.lastCycleCount = nowCycles;
        long newCredits = deltaCycles > 0 ? user.tryDeductCredits(deltaCycles) : user.getCredits();
        if (newCredits == -1) {
            // out of credits — stop debug
            session.engine.endDebug();
            debugSessions.remove(debugId);
            return new StepResult(vars, idx, 0, true, true);
        }

        session.lastVariableState = vars;
        session.currentIndex = idx;
        session.creditsRemaining = newCredits;

        boolean finished = (idx == -1);
        if (finished) {
            // save history
            long cyclesTotal = session.engine.getCycels();
            user.incrementRunCount();
            user.addHistoryEntry(session.isMainProgram, session.programName,
                    session.architecture.name(), session.level,
                    vars == null ? 0 : vars.getOrDefault("y", 0L), cyclesTotal);
            session.engine.endDebug();
            debugSessions.remove(debugId);
        }

        return new StepResult(vars, idx, newCredits, finished, false);
    }

    public StepResult resumeDebug(String debugId, UserInfo user) {
        DebugSession session = debugSessions.get(debugId);
        if (session == null) throw new IllegalArgumentException("Debug session not found: " + debugId);

        Map<String, Long> finalState = session.engine.resumeDebug();
        long cyclesTotal = session.engine.getCycels();

        // Deduct only remaining cycles (not already deducted during step calls)
        long remainingCycles = Math.max(0, cyclesTotal - session.lastCycleCount);
        long remaining = remainingCycles > 0 ? user.tryDeductCredits(remainingCycles) : user.getCredits();
        if (remaining == -1) remaining = 0;

        user.incrementRunCount();
        user.addHistoryEntry(session.isMainProgram, session.programName,
                session.architecture.name(), session.level,
                finalState == null ? 0 : finalState.getOrDefault("y", 0L), cyclesTotal);

        session.engine.endDebug();
        debugSessions.remove(debugId);

        return new StepResult(finalState, -1, remaining, true, false);
    }

    public void stopDebug(String debugId) {
        DebugSession session = debugSessions.remove(debugId);
        if (session != null) {
            session.engine.endDebug();
        }
    }

    public DebugSession getDebugSession(String debugId) {
        return debugSessions.get(debugId);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private EngineImpl buildEngine(ProgramEntry entry, UserRegistry userRegistry) {
        EngineImpl engine = new EngineImpl();
        // Re-parse from stored XML content → fresh, isolated engine per execution
        LoadReport report = engine.loadProgramFromContent(entry.xmlContent);
        if (!report.ok()) {
            throw new IllegalStateException("Failed to build engine for program: " + entry.getName());
        }
        // If the program is a helper function, switch the engine to that function context
        if (!entry.isMainProgram) {
            engine.loadFunc(entry.getName());
        }
        return engine;
    }
}
