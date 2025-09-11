package application.right;

import application.main.MainLayoutController;
import dto.ProgramSummary;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class RightToolbarController {

    MainLayoutController mainLayoutController;

    @FXML private Label CyclesLabel;
    @FXML private Label InputsLabel;
    @FXML private Label VariablesLabel;
    @FXML private Label debuggerLabel;
    @FXML private Label executionLabel;
    @FXML private HBox regulerExecution;
    @FXML private Button resumeDebugButton;
    @FXML private VBox right;
    @FXML private Button startButton;
    @FXML private Button startDebugButton;
    @FXML private Button stepOverDebugButton;
    @FXML private Button stopDebugButton;
    @FXML private TableColumn<String, String> valueInput;
    @FXML private TableColumn<?, ?> valueState;
    @FXML private TableColumn<?, ?> variableInput;
    @FXML private TableColumn<?, ?> variableState;
    @FXML private TableView<String > variableTable;
    @FXML private TableView<?> inputTable;


    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void clearAll() {
    }
    @FXML
    void historyOrStatisticsListener(ActionEvent event) {

    }

    @FXML
    void reRunListener(ActionEvent event) {

    }

    @FXML
    void resumeDebugListener(ActionEvent event) {

    }

    @FXML
    void showHistoryListener(ActionEvent event) {

    }

    @FXML
    void startDebugListener(ActionEvent event) {

    }

    @FXML
    void startListener(ActionEvent event) {

    }

    @FXML
    void stepOverDebugListener(ActionEvent event) {

    }

    @FXML
    void stopDebugListener(ActionEvent event) {

    }

//    public void loadInputTable() {
//        ProgramSummary summary = mainLayoutController.engine.getProgramSummaryForShow();
//        List<String> inputs = summary.getInputs();
//        variableInput.setCellValueFactory(cell ->
//                new ReadOnlyStringWrapper(cell.getValue()));
//
//        valueInput.setCellValueFactory(cell ->
//                new ReadOnlyStringWrapper(""));
//
//        inputTable.setItems(FXCollections.observableArrayList(inputs));
//    }

    public void showProgram() {
    }
}
