package application.client.execution;

import application.client.http.HttpApiClient;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Client-side Execution / Debug screen.
 * All engine calls go via HTTP to the server.
 */
public class ExecutionController {

    // ---- Header ----
    @FXML private Label programNameLabel;
    @FXML private Label creditsLabel;
    @FXML private Label usernameLabel;
    @FXML private ComboBox<String> architectureCombo;
    @FXML private Spinner<Integer> levelSpinner;
    @FXML private Label levelMaxLabel;
    @FXML private Label statusLabel;

    // ---- Instructions table ----
    @FXML private TableView<Map<String, Object>> instructionsTable;
    @FXML private TableColumn<Map<String, Object>, String> colLineNum;
    @FXML private TableColumn<Map<String, Object>, String> colLabel;
    @FXML private TableColumn<Map<String, Object>, String> colCommand;
    @FXML private TableColumn<Map<String, Object>, String> colCycles;
    @FXML private TableColumn<Map<String, Object>, String> colArch;

    // ---- Architecture summary ----
    @FXML private Label archCountI;
    @FXML private Label archCountII;
    @FXML private Label archCountIII;
    @FXML private Label archCountIV;

    // ---- Inputs ----
    @FXML private TableView<String[]> inputTable;
    @FXML private TableColumn<String[], String> colInputVar;
    @FXML private TableColumn<String[], String> colInputVal;

    // ---- Run controls ----
    @FXML private Button backButton;
    @FXML private Button runButton;
    @FXML private Button debugStartButton;
    @FXML private Button stepButton;
    @FXML private Button resumeButton;
    @FXML private Button stopDebugButton;
    @FXML private Label cyclesLabel;

    // ---- Variable state ----
    @FXML private TableView<String[]> variableTable;
    @FXML private TableColumn<String[], String> colVarName;
    @FXML private TableColumn<String[], String> colVarValue;

    // ---- Run history ----
    @FXML private TableView<Map<String, Object>> historyTable;
    @FXML private TableColumn<Map<String, Object>, String> colHistNum;
    @FXML private TableColumn<Map<String, Object>, String> colHistArch2;
    @FXML private TableColumn<Map<String, Object>, String> colHistLevel;
    @FXML private TableColumn<Map<String, Object>, String> colHistY;
    @FXML private TableColumn<Map<String, Object>, String> colHistCycles;

    private String username;
    private HttpApiClient client;
    private String programName;
    private boolean isMainProgram;

    private String currentDebugId = null;
    private final List<String[]> inputRows = new ArrayList<>(); // [varName, value]
    private final Map<String, Long> inputsMap = new LinkedHashMap<>();
    private int maxLevel = 0;

    private final ExecutorService bg = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "exec-bg");
        t.setDaemon(true);
        return t;
    });

    public void init(String username, HttpApiClient client, String programName, boolean isMainProgram) {
        this.username = username;
        this.client = client;
        this.programName = programName;
        this.isMainProgram = isMainProgram;

        programNameLabel.setText((isMainProgram ? "Program: " : "Function: ") + programName);
        usernameLabel.setText(username);

        architectureCombo.setItems(FXCollections.observableArrayList("I", "II", "III", "IV"));
        architectureCombo.getSelectionModel().select("I");
        architectureCombo.setOnAction(ev -> loadProgramInfo());
        levelSpinner.valueProperty().addListener((obs, o, n) -> loadProgramInfo());

        setupTableColumns();
        setDebugMode(false);
        loadProgramInfo();
        refreshHistory();
    }

    // -----------------------------------------------------------------------
    // Program info (instruction list)
    // -----------------------------------------------------------------------

    private void loadProgramInfo() {
        String arch = architectureCombo.getSelectionModel().getSelectedItem();
        if (arch == null) arch = "I";
        int level = levelSpinner != null && levelSpinner.getValue() != null ? levelSpinner.getValue() : 0;

        String finalArch = arch;
        int finalLevel = level;

        bg.submit(() -> {
            try {
                Map<String, Object> info = client.getProgramInfo(programName, isMainProgram, finalLevel, finalArch);
                Platform.runLater(() -> applyProgramInfo(info, finalArch));
            } catch (Exception e) {
                Platform.runLater(() -> setStatus("Error loading program info: " + e.getMessage()));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void applyProgramInfo(Map<String, Object> info, String selectedArch) {
        maxLevel = (int) ((Number) info.getOrDefault("maxLevel", 0)).doubleValue();
        levelMaxLabel.setText("/ " + maxLevel);

        // Update level spinner range
        if (levelSpinner != null) {
            SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                    (SpinnerValueFactory.IntegerSpinnerValueFactory) levelSpinner.getValueFactory();
            if (factory != null) {
                int cur = factory.getValue();
                factory.setMax(maxLevel);
                factory.setValue(Math.min(cur, maxLevel));
            }
        }

        // Architecture count summary
        archCountI.setText("I: " + (int)((Number)info.getOrDefault("countI", 0)).doubleValue());
        archCountII.setText("II: " + (int)((Number)info.getOrDefault("countII", 0)).doubleValue());
        archCountIII.setText("III: " + (int)((Number)info.getOrDefault("countIII", 0)).doubleValue());
        archCountIV.setText("IV: " + (int)((Number)info.getOrDefault("countIV", 0)).doubleValue());

        // Highlight arch labels that don't cover all instructions
        int total = (int)((Number)info.getOrDefault("totalInstructions", 0)).doubleValue();
        styleArchLabel(archCountI,   info, "countI",   total, selectedArch.equals("I"));
        styleArchLabel(archCountII,  info, "countII",  total, selectedArch.equals("II"));
        styleArchLabel(archCountIII, info, "countIII", total, selectedArch.equals("III"));
        styleArchLabel(archCountIV,  info, "countIV",  total, selectedArch.equals("IV"));

        // Instructions table
        List<Map<String, Object>> instructions =
                (List<Map<String, Object>>) info.getOrDefault("instructions", List.of());
        instructionsTable.getItems().setAll(instructions);

        // Build input rows from the server-provided inputVariables list
        @SuppressWarnings("unchecked")
        List<String> serverInputVars = (List<String>) info.get("inputVariables");
        inputRows.clear();
        if (serverInputVars != null && !serverInputVars.isEmpty()) {
            for (String v : serverInputVars) inputRows.add(new String[]{v, "0"});
        }
        if (inputTable != null) inputTable.getItems().setAll(inputRows);
    }

    private void styleArchLabel(Label label, Map<String, Object> info, String key, int total, boolean isSelected) {
        int count = (int)((Number)info.getOrDefault(key, 0)).doubleValue();
        boolean incomplete = count < total;
        String base = incomplete ? "-fx-text-fill: #ef4444;" : "-fx-text-fill: #22c55e;";
        String bg = isSelected ? " -fx-background-color: #dbeafe; -fx-background-radius: 4;" : " -fx-background-color: #ecf0f1; -fx-background-radius: 4;";
        String bold = isSelected ? " -fx-font-weight: bold;" : "";
        label.setStyle("-fx-padding: 2 8; -fx-font-size: 11;" + base + bg + bold);
    }

    // -----------------------------------------------------------------------
    // Column setup
    // -----------------------------------------------------------------------

    private void setupTableColumns() {
        // Apply constrained resize + centered cells first so specific factories below override them
        setConstrainedAndCentered(instructionsTable);
        setConstrainedAndCentered(inputTable);
        setConstrainedAndCentered(variableTable);
        setConstrainedAndCentered(historyTable);

        // Instructions (value factories + colArch color override)
        colLineNum.setCellValueFactory(cd -> new ReadOnlyStringWrapper(str(cd.getValue(), "number")));
        colLabel.setCellValueFactory(cd -> new ReadOnlyStringWrapper(str(cd.getValue(), "label")));
        colCommand.setCellValueFactory(cd -> new ReadOnlyStringWrapper(str(cd.getValue(), "command")));
        colCycles.setCellValueFactory(cd -> new ReadOnlyStringWrapper(str(cd.getValue(), "cycles")));
        colArch.setCellValueFactory(cd -> {
            Object supported = cd.getValue().get("supported");
            return new ReadOnlyStringWrapper(Boolean.FALSE.equals(supported) ? "NO" : "OK");
        });
        colArch.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle(empty ? "" : "NO".equals(item) ? "-fx-text-fill: #f38ba8; -fx-alignment: CENTER;"
                        : "-fx-text-fill: #a6e3a1; -fx-alignment: CENTER;");
            }
        });

        // Input table (editable value column — must come after setConstrainedAndCentered)
        if (colInputVar != null) {
            colInputVar.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue()[0]));
            colInputVal.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue()[1]));
            colInputVal.setCellFactory(col -> new javafx.scene.control.cell.TextFieldTableCell<>(
                    new javafx.util.converter.DefaultStringConverter()));
            colInputVal.setOnEditCommit(ev -> {
                ev.getRowValue()[1] = ev.getNewValue();
                inputsMap.put(ev.getRowValue()[0], parseLong(ev.getNewValue()));
            });
            if (inputTable != null) inputTable.setEditable(true);
        }

        // Variable state
        if (colVarName != null) {
            colVarName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue()[0]));
            colVarValue.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue()[1]));
        }

        // History
        if (colHistNum != null) {
            colHistNum.setCellValueFactory(cd -> new ReadOnlyStringWrapper(str(cd.getValue(), "runNumber")));
            colHistArch2.setCellValueFactory(cd -> new ReadOnlyStringWrapper(str(cd.getValue(), "architecture")));
            colHistLevel.setCellValueFactory(cd -> new ReadOnlyStringWrapper(str(cd.getValue(), "level")));
            colHistY.setCellValueFactory(cd -> new ReadOnlyStringWrapper(str(cd.getValue(), "yResult")));
            colHistCycles.setCellValueFactory(cd -> new ReadOnlyStringWrapper(str(cd.getValue(), "cyclesUsed")));
        }
    }

    // -----------------------------------------------------------------------
    // Execution
    // -----------------------------------------------------------------------

    @FXML
    void onRun() {
        String arch = architectureCombo.getSelectionModel().getSelectedItem();
        int level = levelSpinner != null && levelSpinner.getValue() != null ? levelSpinner.getValue() : 0;
        List<Long> inputs = collectInputs();

        runButton.setDisable(true);
        setStatus("Running...");
        cyclesLabel.setText("Running...");

        bg.submit(() -> {
            try {
                String runId = client.startRun(username, programName, isMainProgram, arch, level, inputs);
                // Poll until done
                Map<String, Object> result = null;
                while (result == null || "pending".equals(result.get("status"))) {
                    Thread.sleep(400);
                    result = client.pollRun(runId);
                }
                Map<String, Object> finalResult = result;
                Platform.runLater(() -> {
                    runButton.setDisable(false);
                    Object res = finalResult.get("result");
                    if (res instanceof Map<?,?> resMap) {
                        Object rawCycles = resMap.get("totalCycles");
                        Object rawY      = resMap.get("y");
                        Object rawVars   = resMap.get("variables");
                        long cycles = rawCycles instanceof Number n ? n.longValue() : 0L;
                        long y      = rawY      instanceof Number n ? n.longValue() : 0L;
                        cyclesLabel.setText("Cycles: " + cycles);
                        setStatus("Done. y = " + y);
                        if (rawVars instanceof Map<?,?> varsMap) applyVariableState(varsMap);
                        refreshCredits();
                        refreshHistory();
                    }
                });
            } catch (HttpApiClient.InsufficientCreditsException e) {
                Platform.runLater(() -> {
                    runButton.setDisable(false);
                    showAlert(Alert.AlertType.WARNING, "Insufficient Credits", e.getMessage());
                    setStatus("Not enough credits.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    runButton.setDisable(false);
                    setStatus("Run error: " + e.getMessage());
                });
            }
        });
    }

    @FXML
    void onDebugStart() {
        String arch = architectureCombo.getSelectionModel().getSelectedItem();
        int level = levelSpinner != null && levelSpinner.getValue() != null ? levelSpinner.getValue() : 0;
        List<Long> inputs = collectInputs();

        bg.submit(() -> {
            try {
                Map<String, Object> result = client.startDebug(username, programName, isMainProgram,
                        arch, level, inputs);
                Platform.runLater(() -> {
                    currentDebugId = (String) result.get("debugId");
                    int idx = ((Number) result.get("currentIndex")).intValue();
                    applyDebugState(result, idx);
                    setDebugMode(true);
                    setStatus("Debug started. Step through instructions.");
                });
            } catch (HttpApiClient.InsufficientCreditsException e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.WARNING, "Insufficient Credits", e.getMessage()));
            } catch (Exception e) {
                Platform.runLater(() -> setStatus("Debug start error: " + e.getMessage()));
            }
        });
    }

    @FXML
    void onStep() {
        if (currentDebugId == null) return;
        bg.submit(() -> {
            try {
                Map<String, Object> result = client.debugStep(currentDebugId);
                Platform.runLater(() -> {
                    int idx = ((Number) result.get("currentIndex")).intValue();
                    boolean finished = Boolean.TRUE.equals(result.get("finished"));
                    boolean outOfCredits = Boolean.TRUE.equals(result.get("outOfCredits"));

                    applyDebugState(result, idx);
                    refreshCredits();

                    if (outOfCredits) {
                        currentDebugId = null;
                        setDebugMode(false);
                        showAlert(Alert.AlertType.WARNING, "Out of Credits",
                                "You ran out of credits. Execution stopped.");
                        setStatus("Stopped: out of credits.");
                    } else if (finished) {
                        currentDebugId = null;
                        setDebugMode(false);
                        setStatus("Program finished.");
                        refreshHistory();
                        showAlert(Alert.AlertType.INFORMATION, "Done", "Program finished.");
                    } else {
                        highlightRow(idx);
                        setStatus("Step done. Index: " + idx);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> setStatus("Step error: " + e.getMessage()));
            }
        });
    }

    @FXML
    void onResume() {
        if (currentDebugId == null) return;
        bg.submit(() -> {
            try {
                Map<String, Object> result = client.debugResume(currentDebugId);
                Platform.runLater(() -> {
                    currentDebugId = null;
                    setDebugMode(false);
                    applyVariableState((Map<?,?>) result.getOrDefault("variables", Map.of()));
                    refreshCredits();
                    refreshHistory();
                    setStatus("Program resumed to finish.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> setStatus("Resume error: " + e.getMessage()));
            }
        });
    }

    @FXML
    void onStopDebug() {
        if (currentDebugId == null) return;
        String id = currentDebugId;
        currentDebugId = null;
        setDebugMode(false);
        bg.submit(() -> client.debugStop(id));
        setStatus("Debug stopped.");
    }

    @FXML
    void onBack() {
        Stage stage = (Stage) programNameLabel.getScene().getWindow();
        stage.close();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void setDebugMode(boolean active) {
        runButton.setDisable(active);
        debugStartButton.setDisable(active);
        stepButton.setDisable(!active);
        resumeButton.setDisable(!active);
        stopDebugButton.setDisable(!active);
        if (architectureCombo != null) architectureCombo.setDisable(active);
        if (levelSpinner != null) levelSpinner.setDisable(active);
    }

    @SuppressWarnings("unchecked")
    private void applyDebugState(Map<String, Object> result, int currentIdx) {
        Object vars = result.get("variables");
        if (vars instanceof Map<?,?> varMap) {
            applyVariableState(varMap);
        }
        Object credObj = result.get("credits");
        if (credObj != null) {
            creditsLabel.setText("Credits: " + ((Number)credObj).longValue());
        }
        highlightRow(currentIdx);
    }

    private void highlightRow(int idx) {
        instructionsTable.getSelectionModel().clearSelection();
        if (idx >= 0 && idx < instructionsTable.getItems().size()) {
            instructionsTable.getSelectionModel().select(idx);
            instructionsTable.scrollTo(idx);
        }
    }

    @SuppressWarnings("unchecked")
    private void applyVariableState(Map<?,?> vars) {
        if (variableTable == null) return;
        List<String[]> rows = new ArrayList<>();
        // Sort: y first, then x*, then z*
        List<String> keys = vars.keySet().stream()
                .map(Object::toString)
                .sorted((a, b) -> {
                    String la = a.toLowerCase(), lb = b.toLowerCase();
                    if (la.equals("y")) return -1;
                    if (lb.equals("y")) return 1;
                    if (la.startsWith("x") && lb.startsWith("x")) return compareSuffix(la, lb, 1);
                    if (la.startsWith("x")) return -1;
                    if (lb.startsWith("x")) return 1;
                    if (la.startsWith("z") && lb.startsWith("z")) return compareSuffix(la, lb, 1);
                    return la.compareTo(lb);
                }).collect(Collectors.toList());
        for (String k : keys) {
            Object v = vars.get(k);
            String valStr = v == null ? "0" : (v instanceof Number) ? String.valueOf(((Number)v).longValue()) : v.toString();
            rows.add(new String[]{k, valStr});
        }
        variableTable.getItems().setAll(rows);
    }

    private void refreshCredits() {
        bg.submit(() -> {
            try {
                List<Map<String, Object>> users = client.getUsers();
                users.stream()
                        .filter(u -> username.equals(u.get("username")))
                        .findFirst()
                        .ifPresent(u -> Platform.runLater(() ->
                                creditsLabel.setText("Credits: " + u.get("credits"))));
            } catch (Exception ignored) {}
        });
    }

    private void refreshHistory() {
        bg.submit(() -> {
            try {
                List<Map<String, Object>> history = client.getUserHistory(username);
                List<Map<String, Object>> filtered = history.stream()
                        .filter(h -> programName.equals(h.get("programName"))
                                && isMainProgram == Boolean.TRUE.equals(h.get("isMainProgram")))
                        .collect(Collectors.toList());
                Platform.runLater(() -> {
                    if (historyTable != null) historyTable.getItems().setAll(filtered);
                });
            } catch (Exception ignored) {}
        });
    }

    private List<Long> collectInputs() {
        List<Long> result = new ArrayList<>();
        for (String[] row : inputRows) {
            String val = row[1];
            result.add(parseLong(val));
        }
        return result;
    }

    private static long parseLong(String s) {
        try { return Long.parseLong(s.trim()); } catch (Exception e) { return 0L; }
    }

    private static int compareSuffix(String a, String b, int from) {
        try {
            return Integer.compare(Integer.parseInt(a.substring(from)), Integer.parseInt(b.substring(from)));
        } catch (Exception e) { return a.compareTo(b); }
    }

    private static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return "";
        if (v instanceof Double d) {
            long l = d.longValue();
            return d == l ? String.valueOf(l) : String.format("%.1f", d);
        }
        return v.toString();
    }

    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private static void setConstrainedAndCentered(TableView<?> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        for (TableColumn<?,?> col : table.getColumns()) {
            ((TableColumn<Object,Object>) col).setCellFactory(tc -> {
                TableCell<Object,Object> cell = new TableCell<>() {
                    @Override protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.toString());
                    }
                };
                cell.setAlignment(Pos.CENTER);
                cell.setStyle("-fx-alignment: CENTER;");
                return cell;
            });
        }
    }
}
