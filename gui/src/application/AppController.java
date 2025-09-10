package application;

import application.main.MainLayoutController;
import controler.MainControler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppController extends Application {

    static MainControler controler;
    MainLayoutController layoutController;

//    private AppController() {
//        controler.setProgramSceneController(this);
//    }
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/application/main/main_scene.fxml")
        );
        stage.setTitle("S-emulator");
        stage.setScene(new Scene(root, 800, 660));
        stage.show();
    }

    public static void main(String[] args) {
        launch(AppController.class);
    }
}
