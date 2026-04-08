package application.handlers;

import application.model.UserInfo;
import application.service.UserRegistry;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

/**
 * POST /api/credits/topup  body: {"username":"alice","amount":500}
 */
public class CreditsHandler implements HttpHandler {

    private final UserRegistry users;

    public CreditsHandler(UserRegistry users) {
        this.users = users;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            if (!HandlerUtils.requireMethod(ex, "POST")) return;

            JsonObject json = HandlerUtils.GSON.fromJson(HandlerUtils.readBody(ex), JsonObject.class);
            String username = json.get("username").getAsString();
            long amount = json.get("amount").getAsLong();

            if (amount <= 0) {
                HandlerUtils.sendError(ex, 400, "Amount must be positive");
                return;
            }

            UserInfo user = users.getUser(username);
            if (user == null) {
                HandlerUtils.sendError(ex, 404, "User not found: " + username);
                return;
            }

            user.addCredits(amount);
            HandlerUtils.sendJson(ex, 200, Map.of("ok", true, "credits", user.getCredits()));
        } catch (Exception e) {
            e.printStackTrace();
            HandlerUtils.sendError(ex, 500, e.getMessage());
        }
    }
}
