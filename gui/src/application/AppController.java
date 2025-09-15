package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppController extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/application/main/MainLayout.fxml")
        );
        stage.setTitle("S-emulator");
        stage.setScene(new Scene(root, 920, 660));
        stage.show();
    }

    public static void main(String[] args) {
        launch(AppController.class);
    }
}
