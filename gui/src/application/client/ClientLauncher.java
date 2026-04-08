package application.client;

/**
 * Launcher wrapper to avoid JavaFX module issues when running from a JAR.
 */
public class ClientLauncher {
    public static void main(String[] args) {
        ClientApp.main(args);
    }
}
