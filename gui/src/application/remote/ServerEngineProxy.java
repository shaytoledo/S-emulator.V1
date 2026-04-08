package application.remote;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import core.engine.Engine;
import core.program.Program;
import core.program.VariableAndLabelMenger;
import dto.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implements the Engine interface by forwarding calls to the remote HTTP server.
 * Used by the GUI when running in "Server" mode.
 */
public class ServerEngineProxy implements Engine {

    private final ServerConfig config;
    private final HttpClient http;
    private final Gson gson = new Gson();
    private String sessionId;

    public ServerEngineProxy(ServerConfig config) {
        this.config = config;
        this.http = HttpClient.newHttpClient();
    }

    /** Create a new session on the server. Must be called before other methods. */
    public void connect() {
        String url = config.baseUrl() + "/api/sessions";
        String body = post(url, "");
        JsonObject obj = gson.fromJson(body, JsonObject.class);
        this.sessionId = obj.get("sessionId").getAsString();
    }

    public String getSessionId() { return sessionId; }

    // -----------------------------------------------------------------------
    // Engine interface methods
    // -----------------------------------------------------------------------

    @Override
    public LoadReport loadProgramFromContent(String xmlContent) {
        throw new UnsupportedOperationException("Not supported in legacy server mode");
    }

    @Override
    public LoadReport loadProgram(Path xmlPath) {
        String url = config.baseUrl() + "/api/sessions/" + sessionId + "/load";
        JsonObject body = new JsonObject();
        body.addProperty("xmlPath", xmlPath.toAbsolutePath().toString());
        String resp = post(url, gson.toJson(body));
        JsonObject obj = gson.fromJson(resp, JsonObject.class);
        boolean ok = obj.has("ok") && obj.get("ok").getAsBoolean();
        return new LoadReport(ok, List.of());
    }

    @Override
    public RunResult run(int level, List<Long> inputs) {
        String url = config.baseUrl() + "/api/sessions/" + sessionId + "/run";
        JsonObject body = new JsonObject();
        body.addProperty("level", level);
        body.addProperty("timeoutMs", config.defaultTimeoutMs);
        body.add("inputs", gson.toJsonTree(inputs));

        String resp = post(url, gson.toJson(body));
        return pollUntilDone(resp);
    }

    @Override
    public List<RunSummary> getHistory() {
        String url = config.baseUrl() + "/api/sessions/" + sessionId + "/history";
        String resp = get(url);
        Type listType = new TypeToken<List<RunSummary>>() {}.getType();
        return gson.fromJson(resp, listType);
    }

    // -----------------------------------------------------------------------
    // Unsupported in server mode — debug / expand operations are local-only
    // -----------------------------------------------------------------------

    @Override
    public ProgramSummary getProgramSummaryForShow() {
        throw new UnsupportedOperationException("Not supported in server mode");
    }

    @Override
    public List<List<InstructionView>> expandProgramToLevelForExtend(int level) {
        throw new UnsupportedOperationException("Not supported in server mode");
    }

    @Override
    public List<InstructionView> expandProgramToLevelForRun(int level) {
        throw new UnsupportedOperationException("Not supported in server mode");
    }

    @Override
    public int getMaxExpandLevel() {
        throw new UnsupportedOperationException("Not supported in server mode");
    }

    @Override
    public VariableAndLabelMenger getVlm() {
        throw new UnsupportedOperationException("Not supported in server mode");
    }

    @Override
    public List<List<String>> getInfoForEachInstruction(int level) {
        throw new UnsupportedOperationException("Not supported in server mode");
    }

    @Override
    public Pair<Map<String, Long>, Integer> startDebug(int currentLevel, List<Long> inputsByOrder) {
        throw new UnsupportedOperationException("Debug not supported in server mode");
    }

    @Override
    public Pair<Map<String, Long>, Integer> oneStepInDebug() {
        throw new UnsupportedOperationException("Debug not supported in server mode");
    }

    @Override
    public void endDebug() {
        throw new UnsupportedOperationException("Debug not supported in server mode");
    }

    @Override
    public Map<String, Long> resumeDebug() {
        throw new UnsupportedOperationException("Debug not supported in server mode");
    }

    @Override
    public Map<String, Long> saveDebugRun() {
        throw new UnsupportedOperationException("Debug not supported in server mode");
    }

    @Override
    public int getCycels() {
        throw new UnsupportedOperationException("Not supported in server mode");
    }

    @Override
    public List<functionView> getAllFunctionViews() {
        throw new UnsupportedOperationException("Not supported in server mode");
    }

    @Override
    public Program getCurrentProgram() {
        throw new UnsupportedOperationException("Not supported in server mode");
    }

    @Override
    public void loadFunc(String name) {
        throw new UnsupportedOperationException("Not supported in server mode");
    }

    @Override
    public VariablesAndLabels getProgramInfo(int level) {
        throw new UnsupportedOperationException("Not supported in server mode");
    }

    @Override
    public void cancelRun() {
        // Best-effort: no cancel endpoint yet
    }

    // -----------------------------------------------------------------------
    // HTTP helpers
    // -----------------------------------------------------------------------

    /** Poll the server until the run is complete, then return the RunResult. */
    private RunResult pollUntilDone(String initialResponse) {
        JsonObject obj = gson.fromJson(initialResponse, JsonObject.class);

        while ("pending".equals(obj.get("status").getAsString())) {
            String runId = obj.get("runId").getAsString();
            try {
                Thread.sleep(config.pollIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Polling interrupted", e);
            }
            String resp = get(config.baseUrl() + "/api/run/" + runId);
            obj = gson.fromJson(resp, JsonObject.class);
        }

        // status == "done"
        return gson.fromJson(obj.get("result"), RunResult.class);
    }

    private String post(String url, String jsonBody) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 500) {
                throw new RuntimeException("Server error " + resp.statusCode() + ": " + resp.body());
            }
            return resp.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("HTTP POST failed: " + url, e);
        }
    }

    private String get(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 500) {
                throw new RuntimeException("Server error " + resp.statusCode() + ": " + resp.body());
            }
            return resp.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("HTTP GET failed: " + url, e);
        }
    }
}
