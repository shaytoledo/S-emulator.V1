package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;


public class ProgramSceneController extends Application {



    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/application/main/main_scene.fxml")
        );
        stage.setTitle("S-emulator");
        stage.setScene(new Scene(root, 800, 570));
        stage.show();
    }
//    @Override
//    public void start(Stage stage) throws Exception {
//
////        Parent root = FXMLLoader.load(getClass().getResource("/main/main_scene.fxml"));
//        FXMLLoader loader = new FXMLLoader(new File("gui/src/application/main/main_scene.fxml").toURI().toURL());
//        Parent root = loader.load();
//
//        stage.setTitle("S-emulator");
//        stage.setScene(new Scene(root, 800, 570));
//        stage.show();
//    }

    public static void main(String[] args) {
        launch(ProgramSceneController.class);
    }
}
