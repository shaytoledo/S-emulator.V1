package application.handlers;

import application.model.ProgramEntry;
import application.model.UserInfo;
import application.service.ProgramRegistry;
import application.service.UserRegistry;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.LoadReport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * POST /api/programs   body: raw XML, header X-Username  → upload and register a program
 * GET  /api/programs                                      → list all programs
 * GET  /api/functions                                     → list all helper functions
 */
public class ProgramsHandler implements HttpHandler {

    private final ProgramRegistry programs;
    private final UserRegistry users;

    public ProgramsHandler(ProgramRegistry programs, UserRegistry users) {
        this.programs = programs;
        this.users = users;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();

            if (path.equals("/api/programs") || path.equals("/api/programs/")) {
                String method = ex.getRequestMethod();
                if ("GET".equalsIgnoreCase(method)) {
                    handleListPrograms(ex);
                } else if ("POST".equalsIgnoreCase(method)) {
                    handleUpload(ex);
                } else {
                    HandlerUtils.sendError(ex, 405, "Method not allowed");
                }
            } else if (path.equals("/api/functions") || path.equals("/api/functions/")) {
                handleListFunctions(ex);
            } else {
                HandlerUtils.sendError(ex, 404, "Not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            HandlerUtils.sendError(ex, 500, e.getMessage());
        }
    }

    private void handleUpload(HttpExchange ex) throws IOException {
        String username = ex.getRequestHeaders().getFirst("X-Username");
        if (username == null || username.isBlank()) {
            HandlerUtils.sendError(ex, 400, "Missing X-Username header");
            return;
        }

        UserInfo user = users.getUser(username);
        if (user == null) {
            HandlerUtils.sendError(ex, 403, "User not logged in: " + username);
            return;
        }

        String xmlContent = HandlerUtils.readBody(ex);
        if (xmlContent.isBlank()) {
            HandlerUtils.sendError(ex, 400, "Empty XML content");
            return;
        }

        // Count functions before and after to track contribution
        int funcsBefore = programs.getAllFunctions().size();
        LoadReport report = programs.register(xmlContent, username);
        int funcsAfter = programs.getAllFunctions().size();

        if (!report.ok()) {
            List<String> errorMessages = report.errors().stream()
                    .map(e -> e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName())
                    .collect(Collectors.toList());
            HandlerUtils.sendJson(ex, 422, Map.of("ok", false, "errors", errorMessages));
            return;
        }

        user.incrementProgramsUploaded();
        user.addFunctionsContributed(funcsAfter - funcsBefore);

        HandlerUtils.sendJson(ex, 200, Map.of("ok", true));
    }

    private void handleListPrograms(HttpExchange ex) throws IOException {
        if (!HandlerUtils.requireMethod(ex, "GET")) return;

        List<Map<String, Object>> result = new ArrayList<>();
        for (ProgramEntry entry : programs.getAllPrograms()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", entry.getName());
            row.put("uploaderName", entry.uploaderName);
            row.put("instructionCount", entry.getInstructionCount());
            row.put("maxLevel", entry.getMaxLevel());
            row.put("timesRun", entry.getTimesRun());
            row.put("avgCredits", entry.getAvgCreditsUsed());
            row.put("architecture", entry.minArchitecture);
            result.add(row);
        }
        HandlerUtils.sendJson(ex, 200, result);
    }

    private void handleListFunctions(HttpExchange ex) throws IOException {
        if (!HandlerUtils.requireMethod(ex, "GET")) return;

        List<Map<String, Object>> result = new ArrayList<>();
        for (ProgramEntry entry : programs.getAllFunctions()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", entry.getName());
            row.put("parentProgramName", entry.parentProgramName != null ? entry.parentProgramName : "");
            row.put("uploaderName", entry.uploaderName);
            row.put("userString", entry.userString != null ? entry.userString : "");
            row.put("instructionCount", entry.getInstructionCount());
            row.put("maxLevel", entry.getMaxLevel());
            row.put("timesRun", entry.getTimesRun());
            row.put("avgCredits", entry.getAvgCreditsUsed());
            row.put("architecture", entry.minArchitecture);
            result.add(row);
        }
        HandlerUtils.sendJson(ex, 200, result);
    }
}
