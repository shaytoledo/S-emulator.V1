package application.main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainLayoutController implements Initializable {

 //   @FXML private GridPane rootGrid;

    @FXML
    private GridPane bottomGrid;

    @FXML
    private GridPane rootGrid;

    @Override
    public void initialize(URL url, ResourceBundle rb) {


//        try {
//            System.out.println("Loading child FXMLs...");
//            System.out.println("rootGrid is " + rootGrid);
//
//            Node top = FXMLLoader.load(getClass().getResource("/application/main/main_scene.fxml"));
//            rootGrid.add(top, 0, 0);
//            GridPane.setHgrow(top, Priority.ALWAYS);
//
//            // LEFT
//            Node left = FXMLLoader.load(getClass().getResource("/application/left/left_bar.fxml"));
//            bottomGrid.add(left, 0, 0);
//
//            // RIGHT
//            Node right = FXMLLoader.load(getClass().getResource("/application/right/right_bar.fxml"));
//            bottomGrid.add(right, 1, 0);
//
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to load child FXMLs", e);
//        }
    }

    private Object loadIntoGrid(String fxmlPath, int col, int row, int colSpan, int rowSpan) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Node node = loader.load();

        // Add the node into the GridPane at the given position and span
        rootGrid.add(node, col, row, colSpan, rowSpan);

        // Return the controller of the loaded FXML
        return loader.getController();
    }

//    public Object getLeftController()  { return leftController; }
//    public Object getRightController() { return rightController; }
//    public Object getTopController()   { return topController; }


}
