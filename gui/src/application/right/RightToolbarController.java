package application.right;

import application.main.MainLayoutController;
import core.program.VariableAndLabelMenger;
import dto.ProgramSummary;
import dto.RunResult;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;

public class RightToolbarController {

    MainLayoutController mainLayoutController;

    @FXML private Label CyclesLabel;
    @FXML private TextField CyclesCounter;
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
    @FXML private TableColumn<String, String> valueState;
    @FXML private TableColumn<String, String> variableInput;
    @FXML private TableColumn<String, String> variableState;
    @FXML private TableView<String> variableTable;
    @FXML private TableView<String> inputTable;

    private final Map<String, Long> inputsMap = new LinkedHashMap<>();


    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void clearAll() {
        inputsMap.clear();
        inputTable.getItems().clear();
        variableTable.getItems().clear();
        CyclesCounter.setText("");
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
    void stepOverDebugListener(ActionEvent event) {

    }

    @FXML
    void stopDebugListener(ActionEvent event) {

    }

    @FXML
    void startListener(ActionEvent event) {
        List<Long> inputsByOrder = getCurrVariableState();
        RunResult res = mainLayoutController.engine.run(
                mainLayoutController.getCurrentLevel(),
                inputsByOrder
        );

        CyclesCounter.setText(String.valueOf(res.totalCycles()));
        Map<String, Long> resultVars = new LinkedHashMap<>(res.variables());
        resultVars.putIfAbsent("y", res.y());

        List<String> keys = new ArrayList<>(resultVars.keySet());
        keys.sort((a, b) -> {
            boolean ax = a != null && a.toLowerCase().startsWith("x");
            boolean bx = b != null && b.toLowerCase().startsWith("x");
            if (ax && bx) {
                int ai = Integer.parseInt(a.substring(1));
                int bi = Integer.parseInt(b.substring(1));
                return Integer.compare(ai, bi);
            } else if (ax) return -1;
            else if (bx) return 1;
            return a.compareToIgnoreCase(b);
        });

        variableState.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue()));

        valueState.setCellValueFactory(cd -> {
            Long v = resultVars.get(cd.getValue());
            return new ReadOnlyStringWrapper(v == null ? "" : String.valueOf(v));
        });

        valueState.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
            }
        });

        variableTable.getItems().setAll(keys);


    }

    public void showProgram() {
        fillInputTable();
    }

    private void fillInputTable() {

        /// all the variable and label in the expended program, now need to know where is each label and variable
        /// X Y Z
        VariableAndLabelMenger vlm = mainLayoutController.engine.getVlm();
        List<String> allVariables = vlm.getAll();
        List<String> xVariables = allVariables.stream()
                .filter(var -> var != null && var.toLowerCase().startsWith("x"))
                .toList();

        inputTable.getItems().setAll(xVariables);

        variableInput.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue()));

        valueInput.setCellValueFactory(cd -> {
            Long val = inputsMap.get(cd.getValue());
            return new ReadOnlyStringWrapper(val == null ? "" : String.valueOf(val));
        });

        valueInput.setCellFactory(TextFieldTableCell.forTableColumn());

        valueInput.setOnEditCommit(ev -> {
            String varName = ev.getRowValue();
            String text = ev.getNewValue() == null ? "" : ev.getNewValue().trim();

            if (text.isEmpty()) {
                inputsMap.put(varName, 0L);
            } else {
                try {
                    inputsMap.put(varName, Long.parseLong(text));
                } catch (NumberFormatException ignore) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Input");
                    alert.setHeaderText(null);
                    alert.setContentText("You can only enter Long values.");
                    alert.showAndWait();
                }
            }
            inputTable.refresh();
            //System.out.println("inputsMap = " + inputsMap);
        });
    }

    private List<Long> getCurrVariableState() {
        List<Long> inputsByOrder = inputsMap.entrySet()  // get all entries from the map (key=value)
                .stream()
                // keep only variables that start with "x" or "X"
                .filter(entry -> entry.getKey() != null && entry.getKey().toLowerCase().startsWith("x"))
                // sort them by the number after the "x"
                .sorted(Comparator.comparingInt(entry -> {
                    String key = entry.getKey().substring(1); // remove the 'x'
                    return Integer.parseInt(key);             // convert the rest to a number
                }))
                // take only the values
                .map(Map.Entry::getValue)
                // collect everything back into a list
                .toList(); // if Java < 16 use .collect(Collectors.toList())
        return inputsByOrder;
    }
}