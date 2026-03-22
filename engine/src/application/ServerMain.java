package application;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ServerMain {

    public static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // TODO: register servlet contexts here
        // server.createContext("/api/programs", new ProgramsHandler());
        // server.createContext("/api/run",      new RunHandler());
        // server.createContext("/api/users",    new UsersHandler());

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();

        System.out.println("S-Emulator Server started on port " + PORT);
        System.out.println("Press Ctrl+C to stop.");
    }
}
