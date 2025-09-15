package application.right;

import application.main.MainLayoutController;
import core.program.VariableAndLabelMenger;
import dto.RunResult;
import dto.RunSummary;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.stream.Collectors;

public class RightToolbarController {

    MainLayoutController mainLayoutController;

    // Won't be used
    @FXML private Label CyclesLabel;
    @FXML private Label InputsLabel;
    @FXML private Label VariablesLabel;
    @FXML private Label debuggerLabel;
    @FXML private Label executionLabel;
    @FXML private VBox right;
    @FXML private HBox regulerExecution;


    // Need to be implemented
    @FXML private Button resumeDebugButton;
    @FXML private Button startButton;
    @FXML private Button startDebugButton;
    @FXML private Button stepOverDebugButton;
    @FXML private Button stopDebugButton;

    @FXML private TextField CyclesCounter;
    @FXML private TableColumn<String, String> valueInput;
    @FXML private TableColumn<String, String> valueState;
    @FXML private TableColumn<String, String> variableInput;
    @FXML private TableColumn<String, String> variableState;
    @FXML private TableView<String> variableTable;
    @FXML private TableView<String> inputTable;
    @FXML private TableView<RunSummary> historyTable;
    @FXML private TableColumn<RunSummary, String> Index;
    @FXML private TableColumn<RunSummary, String> level;
    @FXML private TableColumn<RunSummary, String> inputs;
    @FXML private TableColumn<RunSummary, String> output;
    @FXML private TableColumn<RunSummary, String> cycles;


    private final Map<String, Long> inputsMap = new LinkedHashMap<>();


    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void clearAll() {
        inputsMap.clear();
        inputTable.getItems().clear();
        variableTable.getItems().clear();
        CyclesCounter.setText("");
        historyTable.getItems().clear();
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
    private void historyRowClicked(MouseEvent event) {
        RunSummary selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        applyInputsToExistingInputTable(selected.inputs());
    }

    // Copies the given input values into the existing inputTable rows, in order.
    private void applyInputsToExistingInputTable(List<Long> values) {
        if (values == null) values = Collections.emptyList();

        // Current rows in the input table (these are variable names, e.g., "x1", "x2", ...)
        List<String> rows = inputTable.getItems();
        int n = rows == null ? 0 : rows.size();

        for (int i = 0; i < n; i++) {
            String varName = rows.get(i);
            long v = (i < values.size() && values.get(i) != null) ? values.get(i) : 0L;
            // Store into your backing map so the next run() uses these numbers
            inputsMap.put(varName, v);
        }

        // Refresh the table so the "value" column shows the updated numbers
        inputTable.refresh();
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
            String la = a == null ? "" : a.toLowerCase();
            String lb = b == null ? "" : b.toLowerCase();

            if (la.equals("y") && !lb.equals("y")) return -1;
            if (lb.equals("y") && !la.equals("y")) return 1;

            boolean ax = la.startsWith("x");
            boolean bx = lb.startsWith("x");
            if (ax && bx) return Integer.compare(numAfterPrefix(la, 'x'), numAfterPrefix(lb, 'x'));
            if (ax) return -1;
            if (bx) return 1;

            boolean az = la.startsWith("z");
            boolean bz = lb.startsWith("z");
            if (az && bz) return Integer.compare(numAfterPrefix(la, 'z'), numAfterPrefix(lb, 'z'));
            if (az) return -1;
            if (bz) return 1;

            return la.compareTo(lb);
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
        fillHistoryTable();


    }

    public void showProgram() {
        fillInputTable();
    }

    public void fillHistoryTable() {
        // Fetch history from the engine
        List<RunSummary> history = mainLayoutController.engine.getHistory();

        if (history == null) {
            history = Collections.emptyList();
        }

        // Define column value factories (one-time setup is enough; harmless if called again)
        // Index (runNumber)
        Index.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(String.valueOf(cd.getValue().runNumber())));

        // Level
        level.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(String.valueOf(cd.getValue().level())));

        // Inputs (format list as comma-separated string)
        inputs.setCellValueFactory(cd -> {
            List<Long> in = cd.getValue().inputs();
            String text = (in == null || in.isEmpty())
                    ? ""
                    : in.stream().map(String::valueOf).collect(Collectors.joining(", "));
            return new ReadOnlyStringWrapper(text);
        });

        // Outputs: assuming 'y' is the single output
        output.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(String.valueOf(cd.getValue().y())));

        // Cycles
        cycles.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(String.valueOf(cd.getValue().cycles())));

        // Optional: simple cell to ensure plain text without styling issues
        TableCellFactoryPlainText(Index);
        TableCellFactoryPlainText(level);
        TableCellFactoryPlainText(inputs);
        TableCellFactoryPlainText(output);
        TableCellFactoryPlainText(cycles);

        // Set items
        historyTable.getItems().setAll(history);
    }

    // Small helper to enforce plain text cells (optional)
    private static void TableCellFactoryPlainText(TableColumn<RunSummary, String> col) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
            }
        });
    }


    private void fillInputTable() {

        /// all the variable and label in the expended program, now need to know where each label and variable
        /// X Y Z is
        VariableAndLabelMenger vlm = mainLayoutController.engine.getVlm();
        List<String> allVariables = vlm.getAll();
        List<String> xVariables = allVariables.stream()
                .filter(var -> var != null && var.toLowerCase().startsWith("x"))
                .toList();


        for (String var : xVariables) {
            inputsMap.putIfAbsent(var, 0L);
        }

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
                .map(entry -> entry.getValue() == null ? 0L : entry.getValue())
                .toList();                // collect everything back into a list
        return inputsByOrder;
    }

    private static int numAfterPrefix(String s, char prefix) {
        if (s == null || s.length() < 2 || Character.toLowerCase(s.charAt(0)) != Character.toLowerCase(prefix)) {
            return Integer.MAX_VALUE; // שלא יקדים שמות בלי מספר תקין
        }
        try {
            return Integer.parseInt(s.substring(1));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE; // x, z בלי מספר – יופיעו בסוף קבוצתם
        }
    }

}