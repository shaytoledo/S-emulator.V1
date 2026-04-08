package application;

import application.handlers.*;
import application.service.ExecutionService;
import application.service.ProgramRegistry;
import application.service.UserRegistry;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ServerMain {

    public static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        // Shared services
        UserRegistry users = new UserRegistry();
        ProgramRegistry programs = new ProgramRegistry();
        ExecutionService execution = new ExecutionService();

        // HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Auth routes
        server.createContext("/api/login",   new LoginHandler(users));
        server.createContext("/api/logout",  new LoginHandler(users));

        // User info routes
        server.createContext("/api/users",   new UsersHandler(users));

        // Program/function upload and listing
        server.createContext("/api/programs", new ProgramsHandler(programs, users));
        server.createContext("/api/functions", new ProgramsHandler(programs, users));

        // Execution routes (run, poll, debug)
        server.createContext("/api/execute/", new ExecuteHandler(execution, programs, users));

        // Credits
        server.createContext("/api/credits/", new CreditsHandler(users));

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("S-Emulator Server (Ex3) started on port " + PORT);
        System.out.println("Endpoints:");
        System.out.println("  POST /api/login         - login with username");
        System.out.println("  POST /api/logout        - logout");
        System.out.println("  GET  /api/users         - list users");
        System.out.println("  GET  /api/users/{name}/history - user history");
        System.out.println("  POST /api/programs      - upload XML program");
        System.out.println("  GET  /api/programs      - list all programs");
        System.out.println("  GET  /api/functions     - list all functions");
        System.out.println("  POST /api/execute/run   - start a run");
        System.out.println("  GET  /api/execute/poll/{id} - poll run result");
        System.out.println("  POST /api/execute/debug/start - start debug");
        System.out.println("  POST /api/execute/debug/{id}/step   - step");
        System.out.println("  POST /api/execute/debug/{id}/resume - resume");
        System.out.println("  POST /api/execute/debug/{id}/stop   - stop");
        System.out.println("  GET  /api/execute/program-info - get instruction list");
        System.out.println("  POST /api/credits/topup - add credits");
        System.out.println("\nPress Ctrl+C to stop.");
    }
}
