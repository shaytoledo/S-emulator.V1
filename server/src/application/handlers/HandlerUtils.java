package application.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HandlerUtils {

    public static final Gson GSON = new Gson();

    public static String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static void sendJson(HttpExchange ex, int status, Object body) throws IOException {
        String json = GSON.toJson(body);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        addCorsHeaders(ex);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendError(HttpExchange ex, int status, String message) throws IOException {
        sendJson(ex, status, new ErrorBody(message));
    }

    public static boolean requireMethod(HttpExchange ex, String method) throws IOException {
        if (!method.equalsIgnoreCase(ex.getRequestMethod())) {
            sendError(ex, 405, "Method not allowed: " + ex.getRequestMethod());
            return false;
        }
        return true;
    }

    /** Extract the path segment after a known prefix */
    public static String pathAfter(String path, String prefix) {
        if (path.startsWith(prefix)) {
            return path.substring(prefix.length());
        }
        return path;
    }

    private static void addCorsHeaders(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, X-Username");
    }

    record ErrorBody(String error) {}
}
