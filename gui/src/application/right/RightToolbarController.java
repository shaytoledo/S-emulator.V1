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
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class RightToolbarController {

    MainLayoutController mainLayoutController;

    // Won't be used
    @FXML private Label CyclesLabel;
    @FXML private Label InputsLabel;
    @FXML private Label VariablesLabel;
    @FXML private Label debuggerLabel;
    @FXML private Label History;
    @FXML private Label executionLabel;
    @FXML private VBox right;
    @FXML private HBox regulerExecution;


    // Need to be implemented
    @FXML public Button resumeDebugButton;
    @FXML public Button startButton;
    @FXML public Button startDebugButton;
    @FXML public Button stepOverDebugButton;
    @FXML public Button stopDebugButton;
    @FXML public Button show;
    @FXML public Button reRun;

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

    private void startDebugButtons() {
        mainLayoutController.getTop().Expand.setDisable(true);
        mainLayoutController.getTop().Collapse.setDisable(true);
        mainLayoutController.getTop().HighlightSelection.setDisable(true);
        mainLayoutController.getTop().ProgramOrFunctionSelector.setDisable(true);
        resumeDebugButton.setDisable(false);
        stepOverDebugButton.setDisable(false);
        stopDebugButton.setDisable(false);
        show.setDisable(true);
        reRun.setDisable(true);
        inputTable.setEditable(false);
        startButton.setDisable(true);
    }

    private void endDebugButtons() {
        mainLayoutController.getTop().HighlightSelection.setDisable(false);
        mainLayoutController.getTop().ProgramOrFunctionSelector.setDisable(false);
        mainLayoutController.getTop().Expand.setDisable(false);
        mainLayoutController.getTop().Collapse.setDisable(false);
        resumeDebugButton.setDisable(true);
        stepOverDebugButton.setDisable(true);
        stopDebugButton.setDisable(true);
        startButton.setDisable(false);
        if (historyTable.getItems().size() > 0) {
            show.setDisable(false);
            reRun.setDisable(false);
        } else {
            show.setDisable(true);
            reRun.setDisable(true);
        }

    }

    private void startButtons() {
        resumeDebugButton.setDisable(true);
        stepOverDebugButton.setDisable(true);
        stopDebugButton.setDisable(true);
        show.setDisable(false);
        reRun.setDisable(false);
    }




    @FXML
    void showHistoryListener(ActionEvent event) {
        RunSummary selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        applyInputsToExistingInputTable(selected.inputs());
    }

    @FXML
    void reRunListener(ActionEvent event) {
        RunSummary selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        applyInputsToExistingInputTable(selected.inputs());
        run();

    }



    @FXML
    void resumeDebugListener(ActionEvent event) {
        Map<String, Long> variableState  = mainLayoutController.engine.resumeDebug();

        // cycles counter update
        CyclesCounter.setText(String.valueOf(mainLayoutController.engine.getCycels()));

        fillVariableStateTable(variableState);
        inputTable.setEditable(true);
        mainLayoutController.engine.endDebug();
        mainLayoutController.getTop().Expand.setDisable(false);
        mainLayoutController.getTop().Collapse.setDisable(false);
        // unbold all the table lines
        mainLayoutController.getLeft().clearHighlights();
        endDebugButtons();
    }

    @FXML
    void startDebugListener(ActionEvent event) {
        startDebugButtons();
        mainLayoutController.getTop().HighlightSelection.getItems().clear();

        Pair<Map<String, Long>,Integer> variableState = mainLayoutController.engine.startDebug(
                mainLayoutController.getCurrentLevel(),
                getCurrVariableState()
        );
        if (variableState.getKey() == null) {
            endOfDebug();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("(:");
            alert.setHeaderText(null);
            alert.setContentText("The Program finished immediately.");
            alert.showAndWait();
        } else {
            // bold 0 index line in table
            Set<Integer> index = new HashSet<>();
            index.add(0);
            mainLayoutController.getLeft().boldRows(index);
           // fillVariableStateTable(variableState.getKey());
            CyclesCounter.setText("0");
        }
    }

    @FXML
    void stepOverDebugListener(ActionEvent event) {
        Pair<Map<String, Long>,Integer> variableState = mainLayoutController.engine.oneStepInDebug();

        if (variableState.getValue() == -1 ) {
            endOfDebug();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("(:");
            alert.setHeaderText(null);
            alert.setContentText("The Program finished.");
            alert.showAndWait();

        } else {
            // unbold all the table lines
            mainLayoutController.getLeft().clearHighlights();
            // bold index line in table
            Set<Integer> index = new HashSet<>();
            index.add(variableState.getValue());
            mainLayoutController.getLeft().boldRows(index);
            fillVariableStateTable(variableState.getKey());

            // cycles counter update
            CyclesCounter.setText(String.valueOf(mainLayoutController.engine.getCycels()));
        }
    }

    private void endOfDebug() {
        endDebugButtons();
        inputTable.setEditable(true);
        variableTable.getItems().clear();

        // zero debugIndexCounter because the program executer is null now until the next execution
        mainLayoutController.engine.endDebug();
        mainLayoutController.getTop().Expand.setDisable(false);
        mainLayoutController.getTop().Collapse.setDisable(false);
        // unbold all the table lines
        mainLayoutController.getLeft().clearHighlights();
        CyclesCounter.setText("");
    }

    @FXML
    void stopDebugListener(ActionEvent event) {
        endOfDebug();
    }

    // Key for storing the last displayed state in the table's properties
    private static final String DISPLAYED_STATE_KEY = "displayedStateMap";

    // Fills the variable table with the given state and highlights changed values (debug-only usage)
    private void fillVariableStateTable(Map<String, Long> newStateInput) {
        // Ensure UI update on FX thread
        if (!javafx.application.Platform.isFxApplicationThread()) {
            javafx.application.Platform.runLater(() -> fillVariableStateTable(newStateInput));
            return;
        }

        // --- Build final local copies so they are safe to capture in lambdas ---
        @SuppressWarnings("unchecked")
        Map<String, Long> oldRaw = (Map<String, Long>) variableTable.getProperties().get(DISPLAYED_STATE_KEY);
        final Map<String, Long> oldState = (oldRaw == null)
                ? Collections.emptyMap()
                : new HashMap<>(oldRaw); // defensive copy

        final Map<String, Long> newState = (newStateInput == null)
                ? Collections.emptyMap()
                : new HashMap<>(newStateInput); // defensive copy

        // --- Build and sort keys: y first, then x1..xn, z1..zn, then alphabetical ---
        List<String> keys = new ArrayList<>(newState.keySet());
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

        // --- Name column (plain text) with highlight if changed ---
        variableState.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue()));
        variableState.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);

                // Determine row variable name
                String varName = (getTableRow() != null) ? (String) getTableRow().getItem() : null;
                if (varName == null) {
                    setStyle("");
                    return;
                }

                Long was = oldState.get(varName);
                Long now = newState.get(varName);
                if (was != null && !Objects.equals(was, now)) {
                    setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: red;");
                } else {
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // --- Value column (with highlight if changed) ---
        valueState.setCellValueFactory(cd -> {
            Long v = newState.get(cd.getValue());
            return new ReadOnlyStringWrapper(v == null ? "" : String.valueOf(v));
        });
        valueState.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);

                String varName = (getTableRow() != null) ? (String) getTableRow().getItem() : null;
                if (varName == null) {
                    setStyle("");
                    return;
                }

                Long was = oldState.get(varName);
                Long now = newState.get(varName);
                if (was != null && !Objects.equals(was, now)) {
                    setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: red;");

                } else {
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // --- Update rows and refresh ---
        variableTable.getItems().setAll(keys);
        variableTable.refresh();

        // --- Persist the "currently displayed" state for the next comparison ---
        variableTable.getProperties().put(DISPLAYED_STATE_KEY, new HashMap<>(newState));
    }

    // Helper method: extract numeric suffix after a given prefix (x, z, etc.)
    private static int numAfterPrefix(String s, char prefix) {
        if (s == null || s.length() < 2 || Character.toLowerCase(s.charAt(0)) != Character.toLowerCase(prefix)) {
            return Integer.MAX_VALUE; // place non-matching names at the end of their group
        }
        try {
            return Integer.parseInt(s.substring(1));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE; // names without a proper number go last within the group
        }
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
        run();
        startButtons();
    }

    private void run () {
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
}