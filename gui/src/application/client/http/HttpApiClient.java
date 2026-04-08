package application.client.http;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Typed HTTP client for the S-Emulator server (Ex3).
 * All methods are blocking and throw RuntimeException on network errors.
 */
public class HttpApiClient {

    private final String baseUrl;
    private final HttpClient http;
    private final Gson gson = new Gson();

    public HttpApiClient(String host, int port) {
        this.baseUrl = "http://" + host + ":" + port;
        this.http = HttpClient.newHttpClient();
    }

    // -----------------------------------------------------------------------
    // Auth
    // -----------------------------------------------------------------------

    /** @return true if login succeeded; false if username taken */
    public boolean login(String username) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        HttpResponse<String> resp = post("/api/login", gson.toJson(body), null);
        return resp.statusCode() == 200;
    }

    public String loginError(String username) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        HttpResponse<String> resp = post("/api/login", gson.toJson(body), null);
        if (resp.statusCode() == 200) return null;
        return extractError(resp.body());
    }

    public void logout(String username) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        post("/api/logout", gson.toJson(body), null);
    }

    // -----------------------------------------------------------------------
    // Users
    // -----------------------------------------------------------------------

    public List<Map<String, Object>> getUsers() {
        String body = get("/api/users");
        Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
        return gson.fromJson(body, listType);
    }

    public List<Map<String, Object>> getUserHistory(String username) {
        String body = get("/api/users/" + username + "/history");
        Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
        return gson.fromJson(body, listType);
    }

    // -----------------------------------------------------------------------
    // Programs
    // -----------------------------------------------------------------------

    /**
     * Upload XML file content to the server.
     * @return null on success, or an error message string.
     */
    public String uploadProgram(String username, String xmlContent) {
        HttpResponse<String> resp = post("/api/programs", xmlContent,
                Map.of("Content-Type", "application/xml", "X-Username", username));
        if (resp.statusCode() == 200) return null;
        JsonObject obj = gson.fromJson(resp.body(), JsonObject.class);
        if (obj.has("errors")) {
            List<String> errors = gson.fromJson(obj.get("errors"),
                    new TypeToken<List<String>>() {}.getType());
            return String.join("\n", errors);
        }
        return extractError(resp.body());
    }

    public List<Map<String, Object>> getPrograms() {
        String body = get("/api/programs");
        Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
        return gson.fromJson(body, listType);
    }

    public List<Map<String, Object>> getFunctions() {
        String body = get("/api/functions");
        Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
        return gson.fromJson(body, listType);
    }

    // -----------------------------------------------------------------------
    // Program info (instruction list for execution screen)
    // -----------------------------------------------------------------------

    public Map<String, Object> getProgramInfo(String programName, boolean isMain, int level, String architecture) {
        String url = "/api/execute/program-info?programName=" + programName
                + "&isMainProgram=" + isMain
                + "&level=" + level
                + "&architecture=" + architecture;
        String body = get(url);
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(body, mapType);
    }

    // -----------------------------------------------------------------------
    // Execution
    // -----------------------------------------------------------------------

    public record RunStartResult(String runId) {}

    /**
     * Start a run. Architecture cost is deducted server-side.
     * @return runId to poll, or throws on error.
     */
    public String startRun(String username, String programName, boolean isMain,
                           String architecture, int level, List<Long> inputs) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("programName", programName);
        body.addProperty("isMainProgram", isMain);
        body.addProperty("architecture", architecture);
        body.addProperty("level", level);
        JsonArray arr = new JsonArray();
        if (inputs != null) inputs.forEach(arr::add);
        body.add("inputs", arr);

        HttpResponse<String> resp = post("/api/execute/run", gson.toJson(body), null);
        if (resp.statusCode() == 402) {
            throw new InsufficientCreditsException(extractError(resp.body()));
        }
        if (resp.statusCode() >= 400) {
            throw new RuntimeException("Run failed: " + extractError(resp.body()));
        }
        JsonObject obj = gson.fromJson(resp.body(), JsonObject.class);
        return obj.get("runId").getAsString();
    }

    public Map<String, Object> pollRun(String runId) {
        String body = get("/api/execute/poll/" + runId);
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(body, mapType);
    }

    // -----------------------------------------------------------------------
    // Debug
    // -----------------------------------------------------------------------

    public Map<String, Object> startDebug(String username, String programName, boolean isMain,
                                           String architecture, int level, List<Long> inputs) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("programName", programName);
        body.addProperty("isMainProgram", isMain);
        body.addProperty("architecture", architecture);
        body.addProperty("level", level);
        JsonArray arr = new JsonArray();
        if (inputs != null) inputs.forEach(arr::add);
        body.add("inputs", arr);

        HttpResponse<String> resp = post("/api/execute/debug/start", gson.toJson(body), null);
        if (resp.statusCode() == 402) throw new InsufficientCreditsException(extractError(resp.body()));
        if (resp.statusCode() >= 400) throw new RuntimeException("Debug start failed: " + extractError(resp.body()));

        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(resp.body(), mapType);
    }

    public Map<String, Object> debugStep(String debugId) {
        HttpResponse<String> resp = post("/api/execute/debug/" + debugId + "/step", "", null);
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(resp.body(), mapType);
    }

    public Map<String, Object> debugResume(String debugId) {
        HttpResponse<String> resp = post("/api/execute/debug/" + debugId + "/resume", "", null);
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(resp.body(), mapType);
    }

    public void debugStop(String debugId) {
        post("/api/execute/debug/" + debugId + "/stop", "", null);
    }

    // -----------------------------------------------------------------------
    // Credits
    // -----------------------------------------------------------------------

    public long topUpCredits(String username, long amount) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("amount", amount);
        HttpResponse<String> resp = post("/api/credits/topup", gson.toJson(body), null);
        JsonObject obj = gson.fromJson(resp.body(), JsonObject.class);
        return obj.has("credits") ? obj.get("credits").getAsLong() : 0;
    }

    // -----------------------------------------------------------------------
    // HTTP helpers
    // -----------------------------------------------------------------------

    private String get(String path) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .GET()
                    .build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("GET " + path + " failed", e);
        }
    }

    private HttpResponse<String> post(String path, String body, Map<String, String> extraHeaders) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
            if (extraHeaders != null) {
                extraHeaders.forEach(builder::header);
            }
            return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("POST " + path + " failed", e);
        }
    }

    private String extractError(String responseBody) {
        try {
            JsonObject obj = gson.fromJson(responseBody, JsonObject.class);
            if (obj.has("error")) return obj.get("error").getAsString();
        } catch (Exception ignored) {}
        return responseBody;
    }

    public static class InsufficientCreditsException extends RuntimeException {
        public InsufficientCreditsException(String msg) { super(msg); }
    }
}
