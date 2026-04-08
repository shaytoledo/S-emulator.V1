package application.handlers;

import application.model.RunHistoryEntry;
import application.model.UserInfo;
import application.service.UserRegistry;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * GET /api/users              → list all logged-in users with stats
 * GET /api/users/{name}/history → run history for a user
 */
public class UsersHandler implements HttpHandler {

    private final UserRegistry users;

    public UsersHandler(UserRegistry users) {
        this.users = users;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            String rest = HandlerUtils.pathAfter(path, "/api/users");

            if (rest.isEmpty() || rest.equals("/")) {
                handleListUsers(ex);
            } else if (rest.startsWith("/") && rest.endsWith("/history")) {
                String username = rest.substring(1, rest.length() - "/history".length());
                handleUserHistory(ex, username);
            } else {
                HandlerUtils.sendError(ex, 404, "Not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            HandlerUtils.sendError(ex, 500, e.getMessage());
        }
    }

    private void handleListUsers(HttpExchange ex) throws IOException {
        if (!HandlerUtils.requireMethod(ex, "GET")) return;

        List<Map<String, Object>> result = new ArrayList<>();
        for (UserInfo u : users.getAllUsers()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("username", u.name);
            row.put("credits", u.getCredits());
            row.put("creditsUsed", u.getCreditsUsed());
            row.put("programsUploaded", u.getProgramsUploaded());
            row.put("functionsContributed", u.getFunctionsContributed());
            row.put("runCount", u.getRunCount());
            result.add(row);
        }
        HandlerUtils.sendJson(ex, 200, result);
    }

    private void handleUserHistory(HttpExchange ex, String username) throws IOException {
        if (!HandlerUtils.requireMethod(ex, "GET")) return;

        UserInfo user = users.getUser(username);
        if (user == null) {
            HandlerUtils.sendError(ex, 404, "User not found: " + username);
            return;
        }

        List<RunHistoryEntry> history = user.getHistory();
        HandlerUtils.sendJson(ex, 200, history);
    }
}
