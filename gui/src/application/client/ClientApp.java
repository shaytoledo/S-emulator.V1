package application.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for the Ex3 client application.
 * Launches the Login screen; after successful login opens the Dashboard.
 */
public class ClientApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/application/client/login/Login.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("S-Emulator");
        primaryStage.setScene(new Scene(root, 440, 420));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
