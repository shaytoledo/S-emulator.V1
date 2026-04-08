package application.handlers;

import application.model.Architecture;
import application.model.DebugSession;
import application.model.ProgramEntry;
import application.model.UserInfo;
import application.service.ExecutionService;
import application.service.ProgramRegistry;
import application.service.UserRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.InstructionView;
import logic.instruction.Instruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * POST /api/execute/run                  → start a run, returns runId
 * GET  /api/execute/poll/{runId}         → poll run result
 * POST /api/execute/debug/start          → start debug session
 * POST /api/execute/debug/{id}/step      → step over
 * POST /api/execute/debug/{id}/resume    → resume to end
 * POST /api/execute/debug/{id}/stop      → stop/cancel debug
 * GET  /api/execute/program-info         → instruction views for a program at level
 */
public class ExecuteHandler implements HttpHandler {

    private final ExecutionService execution;
    private final ProgramRegistry programs;
    private final UserRegistry users;

    public ExecuteHandler(ExecutionService execution, ProgramRegistry programs, UserRegistry users) {
        this.execution = execution;
        this.programs = programs;
        this.users = users;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            String rest = HandlerUtils.pathAfter(path, "/api/execute/");

            if (rest.equals("run")) {
                handleRun(ex);
            } else if (rest.startsWith("poll/")) {
                handlePoll(ex, rest.substring("poll/".length()));
            } else if (rest.equals("debug/start")) {
                handleDebugStart(ex);
            } else if (rest.startsWith("debug/") && rest.endsWith("/step")) {
                String debugId = rest.substring("debug/".length(), rest.length() - "/step".length());
                handleDebugStep(ex, debugId);
            } else if (rest.startsWith("debug/") && rest.endsWith("/resume")) {
                String debugId = rest.substring("debug/".length(), rest.length() - "/resume".length());
                handleDebugResume(ex, debugId);
            } else if (rest.startsWith("debug/") && rest.endsWith("/stop")) {
                String debugId = rest.substring("debug/".length(), rest.length() - "/stop".length());
                handleDebugStop(ex, debugId);
            } else if (rest.equals("program-info")) {
                handleProgramInfo(ex);
            } else {
                HandlerUtils.sendError(ex, 404, "Unknown execute path: " + rest);
            }
        } catch (Exception e) {
            e.printStackTrace();
            HandlerUtils.sendError(ex, 500, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    // POST /api/execute/run
    private void handleRun(HttpExchange ex) throws IOException {
        if (!HandlerUtils.requireMethod(ex, "POST")) return;

        JsonObject json = HandlerUtils.GSON.fromJson(HandlerUtils.readBody(ex), JsonObject.class);
        String username   = json.get("username").getAsString();
        String programName = json.get("programName").getAsString();
        boolean isMain    = !json.has("isMainProgram") || json.get("isMainProgram").getAsBoolean();
        Architecture arch = Architecture.fromString(json.get("architecture").getAsString());
        int level         = json.has("level") ? json.get("level").getAsInt() : 0;
        List<Long> inputs = parseInputs(json);

        UserInfo user = requireUser(ex, username);
        if (user == null) return;

        ProgramEntry entry = isMain ? programs.getProgram(programName) : programs.getFunction(programName);
        if (entry == null) {
            HandlerUtils.sendError(ex, 404, "Program/function not found: " + programName);
            return;
        }

        // Check for unsupported instructions
        List<String> unsupported = execution.getUnsupportedCommands(entry, level, arch);
        if (!unsupported.isEmpty()) {
            HandlerUtils.sendError(ex, 422,
                    "Program contains commands not supported by architecture " + arch.name()
                    + ": " + unsupported);
            return;
        }

        // Check credits: architecture cost must be coverable
        long archCost = arch.cost;
        if (user.getCredits() < archCost) {
            HandlerUtils.sendError(ex, 402,
                    "Insufficient credits. Need at least " + archCost + " for architecture " + arch.name()
                    + " but have " + user.getCredits());
            return;
        }

        // Deduct architecture cost up front
        user.tryDeductCredits(archCost);

        String runId = execution.submitRun(user, entry, isMain, arch, level, inputs, users);
        HandlerUtils.sendJson(ex, 202, Map.of("runId", runId, "status", "pending"));
    }

    // GET /api/execute/poll/{runId}
    private void handlePoll(HttpExchange ex, String runId) throws IOException {
        if (!HandlerUtils.requireMethod(ex, "GET")) return;

        ExecutionService.PollResult result = execution.poll(runId);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("runId", runId);
        switch (result.status()) {
            case DONE    -> { resp.put("status", "done");   resp.put("result", result.result()); }
            case PENDING -> resp.put("status", "pending");
            case ERROR   -> { resp.put("status", "error");  resp.put("error", result.error()); }
        }
        HandlerUtils.sendJson(ex, 200, resp);
    }

    // POST /api/execute/debug/start
    private void handleDebugStart(HttpExchange ex) throws IOException {
        if (!HandlerUtils.requireMethod(ex, "POST")) return;

        JsonObject json = HandlerUtils.GSON.fromJson(HandlerUtils.readBody(ex), JsonObject.class);
        String username   = json.get("username").getAsString();
        String programName = json.get("programName").getAsString();
        boolean isMain    = !json.has("isMainProgram") || json.get("isMainProgram").getAsBoolean();
        Architecture arch = Architecture.fromString(json.get("architecture").getAsString());
        int level         = json.has("level") ? json.get("level").getAsInt() : 0;
        List<Long> inputs = parseInputs(json);

        UserInfo user = requireUser(ex, username);
        if (user == null) return;

        ProgramEntry entry = isMain ? programs.getProgram(programName) : programs.getFunction(programName);
        if (entry == null) {
            HandlerUtils.sendError(ex, 404, "Program/function not found: " + programName);
            return;
        }

        List<String> unsupported = execution.getUnsupportedCommands(entry, level, arch);
        if (!unsupported.isEmpty()) {
            HandlerUtils.sendError(ex, 422,
                    "Program contains commands not supported by architecture " + arch.name()
                    + ": " + unsupported);
            return;
        }

        if (user.getCredits() < arch.cost) {
            HandlerUtils.sendError(ex, 402,
                    "Insufficient credits for architecture " + arch.name());
            return;
        }
        user.tryDeductCredits(arch.cost);

        ExecutionService.StartDebugResult r = execution.startDebug(user, entry, isMain, arch, level, inputs, users);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("debugId", r.debugId());
        resp.put("variables", r.variables());
        resp.put("currentIndex", r.currentIndex());
        resp.put("credits", r.credits());
        resp.put("finished", r.finished());
        HandlerUtils.sendJson(ex, 200, resp);
    }

    // POST /api/execute/debug/{id}/step
    private void handleDebugStep(HttpExchange ex, String debugId) throws IOException {
        if (!HandlerUtils.requireMethod(ex, "POST")) return;

        DebugSession session = execution.getDebugSession(debugId);
        if (session == null) {
            HandlerUtils.sendError(ex, 404, "Debug session not found: " + debugId);
            return;
        }

        UserInfo user = users.getUser(session.username);
        if (user == null) {
            HandlerUtils.sendError(ex, 404, "User not found: " + session.username);
            return;
        }

        ExecutionService.StepResult result = execution.stepDebug(debugId, user);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("variables", result.variables());
        resp.put("currentIndex", result.currentIndex());
        resp.put("credits", result.credits());
        resp.put("finished", result.finished());
        resp.put("outOfCredits", result.outOfCredits());
        HandlerUtils.sendJson(ex, 200, resp);
    }

    // POST /api/execute/debug/{id}/resume
    private void handleDebugResume(HttpExchange ex, String debugId) throws IOException {
        if (!HandlerUtils.requireMethod(ex, "POST")) return;

        DebugSession session = execution.getDebugSession(debugId);
        if (session == null) {
            HandlerUtils.sendError(ex, 404, "Debug session not found: " + debugId);
            return;
        }
        UserInfo user = users.getUser(session.username);
        if (user == null) {
            HandlerUtils.sendError(ex, 404, "User not found: " + session.username);
            return;
        }

        ExecutionService.StepResult result = execution.resumeDebug(debugId, user);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("variables", result.variables());
        resp.put("credits", result.credits());
        resp.put("finished", true);
        HandlerUtils.sendJson(ex, 200, resp);
    }

    // POST /api/execute/debug/{id}/stop
    private void handleDebugStop(HttpExchange ex, String debugId) throws IOException {
        if (!HandlerUtils.requireMethod(ex, "POST")) return;
        execution.stopDebug(debugId);
        HandlerUtils.sendJson(ex, 200, Map.of("ok", true));
    }

    // GET /api/execute/program-info?programName=...&isMainProgram=true&level=0
    private void handleProgramInfo(HttpExchange ex) throws IOException {
        if (!HandlerUtils.requireMethod(ex, "GET")) return;

        String query = ex.getRequestURI().getQuery();
        Map<String, String> params = parseQuery(query);
        String programName = params.get("programName");
        boolean isMain = !"false".equalsIgnoreCase(params.getOrDefault("isMainProgram", "true"));
        int level = Integer.parseInt(params.getOrDefault("level", "0"));
        String archStr = params.getOrDefault("architecture", "I");

        ProgramEntry entry = isMain ? programs.getProgram(programName) : programs.getFunction(programName);
        if (entry == null) {
            HandlerUtils.sendError(ex, 404, "Program/function not found: " + programName);
            return;
        }

        Architecture arch = Architecture.fromString(archStr);

        try {
            List<InstructionView> views = entry.program.instructionViewsAfterExtendRunShow(level);
            // After expansion, getInstructions() returns the expanded flat list
            List<logic.instruction.Instruction> instrs = entry.program.getInstructions();

            List<Map<String, Object>> rows = new ArrayList<>();
            if (views != null) {
                for (int i = 0; i < views.size(); i++) {
                    InstructionView v = views.get(i);
                    // Get InstructionData name ("INCREASE", "JNZ", etc.) from the instruction object
                    String instrName = (instrs != null && i < instrs.size())
                            ? instrs.get(i).getName() : "";

                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("number", v.number());
                    row.put("type", v.type());
                    row.put("label", v.label());
                    row.put("command", v.command());
                    row.put("cycles", v.cycles());
                    row.put("instrName", instrName);
                    row.put("supported", instrName.isBlank() || arch.supports(instrName));
                    rows.add(row);
                }
            }

            // Architecture support summary (count instructions supported per tier)
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("instructions", rows);
            resp.put("maxLevel", entry.getMaxLevel());
            resp.put("totalInstructions", rows.size());
            resp.put("inputVariables", entry.program.getXVariablesPeek());

            for (Architecture a : Architecture.values()) {
                long count = rows.stream()
                        .filter(r -> {
                            String name = (String) r.get("instrName");
                            return name != null && !name.isBlank() && a.supports(name);
                        }).count();
                resp.put("count" + a.name(), count);
            }

            HandlerUtils.sendJson(ex, 200, resp);
        } catch (Exception e) {
            HandlerUtils.sendError(ex, 500, "Error expanding program: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private UserInfo requireUser(HttpExchange ex, String username) throws IOException {
        UserInfo user = users.getUser(username);
        if (user == null) {
            HandlerUtils.sendError(ex, 403, "User not logged in: " + username);
        }
        return user;
    }

    private List<Long> parseInputs(JsonObject json) {
        List<Long> inputs = new ArrayList<>();
        if (json.has("inputs")) {
            JsonArray arr = json.getAsJsonArray("inputs");
            arr.forEach(e -> inputs.add(e.getAsLong()));
        }
        return inputs;
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> result = new LinkedHashMap<>();
        if (query == null) return result;
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                result.put(pair.substring(0, eq), pair.substring(eq + 1));
            }
        }
        return result;
    }
}
