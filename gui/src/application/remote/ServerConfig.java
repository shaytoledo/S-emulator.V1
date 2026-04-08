package application.remote;

public class ServerConfig {
    public String host = "localhost";
    public int port = 8080;
    public long defaultTimeoutMs = 10_000L;
    public long pollIntervalMs = 500L;

    public String baseUrl() {
        return "http://" + host + ":" + port;
    }
}
