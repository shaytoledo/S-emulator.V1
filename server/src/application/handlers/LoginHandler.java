package application.handlers;

import application.model.UserInfo;
import application.service.UserRegistry;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

/**
 * POST /api/login   body: {"username":"alice"}  → 200 or 409
 * POST /api/logout  body: {"username":"alice"}  → 200 or 404
 */
public class LoginHandler implements HttpHandler {

    private final UserRegistry users;

    public LoginHandler(UserRegistry users) {
        this.users = users;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        try {
            if (path.endsWith("/login")) {
                handleLogin(ex);
            } else if (path.endsWith("/logout")) {
                handleLogout(ex);
            } else {
                HandlerUtils.sendError(ex, 404, "Not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            HandlerUtils.sendError(ex, 500, e.getMessage());
        }
    }

    private void handleLogin(HttpExchange ex) throws IOException {
        if (!HandlerUtils.requireMethod(ex, "POST")) return;
        String body = HandlerUtils.readBody(ex);
        JsonObject json = HandlerUtils.GSON.fromJson(body, JsonObject.class);
        String username = json.get("username").getAsString().trim();

        if (username.isBlank()) {
            HandlerUtils.sendError(ex, 400, "Username cannot be empty");
            return;
        }

        UserInfo user = users.login(username);
        if (user == null) {
            HandlerUtils.sendError(ex, 409, "Username '" + username + "' is already in use.");
            return;
        }

        HandlerUtils.sendJson(ex, 200, Map.of("ok", true, "credits", user.getCredits()));
    }

    private void handleLogout(HttpExchange ex) throws IOException {
        if (!HandlerUtils.requireMethod(ex, "POST")) return;
        String body = HandlerUtils.readBody(ex);
        JsonObject json = HandlerUtils.GSON.fromJson(body, JsonObject.class);
        String username = json.get("username").getAsString().trim();

        boolean removed = users.logout(username);
        if (!removed) {
            HandlerUtils.sendError(ex, 404, "User not found: " + username);
            return;
        }
        HandlerUtils.sendJson(ex, 200, Map.of("ok", true));
    }
}
