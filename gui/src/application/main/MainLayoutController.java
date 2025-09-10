package application.main;

import application.left.LeftToolbarController;
import application.right.RightToolbarController;
import application.top.TopToolbarController;
import core.engine.Engine;
import core.engine.EngineImpl;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class MainLayoutController {

    @FXML private GridPane bottomGrid;
    @FXML private GridPane rootGrid;

    @FXML private ScrollPane top;
    @FXML private VBox left;
    @FXML private VBox right;


    @FXML private TopToolbarController topController;     // from fx:id="top"
    @FXML private LeftToolbarController leftController;   // from fx:id="left"
    @FXML private RightToolbarController rightController; // from fx:id="right"

    public Engine engine = new EngineImpl();

    /*************************************************************************/

    public LeftToolbarController getLeft()  { return leftController;  }
    public RightToolbarController getRight(){ return rightController; }
    public TopToolbarController getTop() {return topController;}

    @FXML
    private void initialize() {
        if (topController != null || leftController != null || rightController != null) {
            topController.setMainLayoutController(this);
            leftController.setMainLayoutController(this);
            rightController.setMainLayoutController(this);
            //System.out.println("CHECK1");
        }
    }


    public void trytry() {
        System.out.println("CHECK2");
    }
}