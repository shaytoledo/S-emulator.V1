package application.client.dashboard;

import application.client.execution.ExecutionController;
import application.client.http.HttpApiClient;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;

public class DashboardController {

    // ── Users table ──
    @FXML private TableView<Map<String, Object>> usersTable;
    @FXML private TableColumn<Map<String, Object>, String> colUserName;
    @FXML private TableColumn<Map<String, Object>, String> colUserCredits;
    @FXML private TableColumn<Map<String, Object>, String> colUserCreditsUsed;
    @FXML private TableColumn<Map<String, Object>, String> colUserPrograms;
    @FXML private TableColumn<Map<String, Object>, String> colUserFunctions;
    @FXML private TableColumn<Map<String, Object>, String> colUserRuns;

    // ── Programs table ──
    @FXML private TableView<Map<String, Object>> programsTable;
    @FXML private TableColumn<Map<String, Object>, String> colProgName;
    @FXML private TableColumn<Map<String, Object>, String> colProgUploader;
    @FXML private TableColumn<Map<String, Object>, String> colProgInstructions;
    @FXML private TableColumn<Map<String, Object>, String> colProgMaxLevel;
    @FXML private TableColumn<Map<String, Object>, String> colProgRuns;
    @FXML private TableColumn<Map<String, Object>, String> colProgAvgCredits;
    @FXML private TableColumn<Map<String, Object>, String> colProgArchitecture;

    // ── Functions table ──
    @FXML private TableView<Map<String, Object>> functionsTable;
    @FXML private TableColumn<Map<String, Object>, String> colFuncName;
    @FXML private TableColumn<Map<String, Object>, String> colFuncProgram;
    @FXML private TableColumn<Map<String, Object>, String> colFuncUploader;
    @FXML private TableColumn<Map<String, Object>, String> colFuncInstructions;
    @FXML private TableColumn<Map<String, Object>, String> colFuncMaxLevel;
    @FXML private TableColumn<Map<String, Object>, String> colFuncTimesRun;
    @FXML private TableColumn<Map<String, Object>, String> colFuncAvgCredits;
    @FXML private TableColumn<Map<String, Object>, String> colFuncArchitecture;

    // ── History table ──
    @FXML private TableView<Map<String, Object>> historyTable;
    @FXML private TableColumn<Map<String, Object>, String> colHistRun;
    @FXML private TableColumn<Map<String, Object>, String> colHistType;
    @FXML private TableColumn<Map<String, Object>, String> colHistProgram;
    @FXML private TableColumn<Map<String, Object>, String> colHistArch;
    @FXML private TableColumn<Map<String, Object>, String> colHistLevel;
    @FXML private TableColumn<Map<String, Object>, String> colHistY;
    @FXML private TableColumn<Map<String, Object>, String> colHistCycles;

    // ── Top bar ──
    @FXML private Label creditsLabel;
    @FXML private Label usernameLabel;
    @FXML private Label statusLabel;
    @FXML private Label historyTitleLabel;
    @FXML private Label programCountLabel;
    @FXML private Label functionCountLabel;

    // ── Buttons ──
    @FXML private Button uploadButton;
    @FXML private Button runProgramButton;
    @FXML private Button runFunctionButton;
    @FXML private Button topUpButton;
    @FXML private Button logoutButton;

    private String username;
    private HttpApiClient client;
    private Timeline pollTimeline;

    private final ExecutorService bg = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "dashboard-bg");
        t.setDaemon(true);
        return t;
    });

    // ─────────────────────────────────────────────
    // Init
    // ─────────────────────────────────────────────

    public void init(String username, HttpApiClient client) {
        this.username = username;
        this.client   = client;

        usernameLabel.setText(username);
        creditsLabel.setText("Credits: …");

        setupColumns();
        setConstrainedAndCentered(programsTable);
        setConstrainedAndCentered(functionsTable);
        setConstrainedAndCentered(usersTable);
        setConstrainedAndCentered(historyTable);
        setupSelectionListeners();
        loadMyHistory();
        startPolling();
        refresh();
    }

    // ─────────────────────────────────────────────
    // Column bindings
    // ─────────────────────────────────────────────

    private void setupColumns() {
        // Users
        bindCol(colUserName,        m -> str(m, "username"));
        bindCol(colUserCredits,     m -> str(m, "credits"));
        bindCol(colUserCreditsUsed, m -> str(m, "creditsUsed"));
        bindCol(colUserPrograms,    m -> str(m, "programsUploaded"));
        bindCol(colUserFunctions,   m -> str(m, "functionsContributed"));
        bindCol(colUserRuns,        m -> str(m, "runCount"));

        // Programs
        bindCol(colProgName,         m -> str(m, "name"));
        bindCol(colProgUploader,     m -> str(m, "uploaderName"));
        bindCol(colProgInstructions, m -> str(m, "instructionCount"));
        bindCol(colProgMaxLevel,     m -> str(m, "maxLevel"));
        bindCol(colProgRuns,         m -> str(m, "timesRun"));
        bindCol(colProgAvgCredits,   m -> {
            Object v = m.get("avgCredits");
            if (v == null) return "0";
            double d = ((Number) v).doubleValue();
            return d == Math.floor(d) ? String.valueOf((long) d) : String.format("%.1f", d);
        });
        bindCol(colProgArchitecture, m -> str(m, "architecture"));

        // Functions
        bindCol(colFuncName,         m -> str(m, "name"));
        bindCol(colFuncProgram,      m -> str(m, "parentProgramName"));
        bindCol(colFuncUploader,     m -> str(m, "uploaderName"));
        bindCol(colFuncInstructions, m -> str(m, "instructionCount"));
        bindCol(colFuncMaxLevel,     m -> str(m, "maxLevel"));
        bindCol(colFuncTimesRun,     m -> str(m, "timesRun"));
        bindCol(colFuncAvgCredits,   m -> {
            Object v = m.get("avgCredits");
            if (v == null) return "0";
            double d = ((Number) v).doubleValue();
            return d == Math.floor(d) ? String.valueOf((long) d) : String.format("%.1f", d);
        });
        bindCol(colFuncArchitecture, m -> str(m, "architecture"));

        // History
        bindCol(colHistRun,     m -> str(m, "runNumber"));
        bindCol(colHistType,    m -> Boolean.TRUE.equals(m.get("isMainProgram")) ? "Main" : "Func");
        bindCol(colHistProgram, m -> str(m, "programName"));
        bindCol(colHistArch,    m -> str(m, "architecture"));
        bindCol(colHistLevel,   m -> str(m, "level"));
        bindCol(colHistY,       m -> str(m, "yResult"));
        bindCol(colHistCycles,  m -> str(m, "cyclesUsed"));
    }

    private void setupSelectionListeners() {
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) {
                loadMyHistory();
            } else {
                String who = (String) sel.get("username");
                historyTitleLabel.setText("History: " + who);
                loadHistoryFor(who);
            }
        });
    }

    // ─────────────────────────────────────────────
    // Polling
    // ─────────────────────────────────────────────

    private void startPolling() {
        pollTimeline = new Timeline(new KeyFrame(Duration.millis(1500), ev -> refresh()));
        pollTimeline.setCycleCount(Timeline.INDEFINITE);
        pollTimeline.play();
    }

    private void refresh() {
        bg.submit(() -> {
            try {
                List<Map<String, Object>> users     = client.getUsers();
                List<Map<String, Object>> programs  = client.getPrograms();
                List<Map<String, Object>> functions = client.getFunctions();

                Platform.runLater(() -> {
                    usersTable.getItems().setAll(users);
                    programsTable.getItems().setAll(programs);
                    functionsTable.getItems().setAll(functions);

                    if (programCountLabel  != null) programCountLabel.setText("(" + programs.size() + ")");
                    if (functionCountLabel != null) functionCountLabel.setText("(" + functions.size() + ")");

                    users.stream()
                            .filter(u -> username.equals(u.get("username")))
                            .findFirst()
                            .ifPresent(u -> creditsLabel.setText("Credits: " + longStr(u, "credits")));
                });
            } catch (Exception ignored) { /* server temporarily unreachable */ }
        });
    }

    private void loadHistoryFor(String who) {
        bg.submit(() -> {
            try {
                List<Map<String, Object>> history = client.getUserHistory(who);
                Platform.runLater(() -> historyTable.getItems().setAll(history));
            } catch (Exception e) {
                Platform.runLater(() -> historyTable.getItems().clear());
            }
        });
    }

    private void loadMyHistory() {
        historyTitleLabel.setText("My History");
        loadHistoryFor(username);
    }

    // ─────────────────────────────────────────────
    // Actions
    // ─────────────────────────────────────────────

    @FXML
    void onUpload() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select S-Program XML file");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        File file = fc.showOpenDialog(uploadButton.getScene().getWindow());
        if (file == null) return;

        setStatus("Uploading " + file.getName() + "…");
        bg.submit(() -> {
            try {
                String content = Files.readString(file.toPath());
                String error   = client.uploadProgram(username, content);
                Platform.runLater(() -> {
                    if (error == null) {
                        setStatus("Uploaded: " + file.getName());
                    } else {
                        showError("Upload Failed", error);
                        setStatus("Upload failed.");
                    }
                    refresh();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Upload Error", e.getMessage());
                    setStatus("Upload error.");
                });
            }
        });
    }

    @FXML
    void onRunProgram() {
        Map<String, Object> sel = programsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showError("No Selection", "Select a program from the Programs table first.");
            return;
        }
        openExecution((String) sel.get("name"), true);
    }

    @FXML
    void onRunFunction() {
        Map<String, Object> sel = functionsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showError("No Selection", "Select a function from the Functions table first.");
            return;
        }
        openExecution((String) sel.get("name"), false);
    }

    @FXML
    void onTopUp() {
        TextInputDialog dlg = new TextInputDialog("500");
        dlg.setTitle("Add Credits");
        dlg.setHeaderText(null);
        dlg.setContentText("Amount to add:");
        dlg.showAndWait().ifPresent(s -> {
            try {
                long amount = Long.parseLong(s.trim());
                if (amount <= 0) throw new NumberFormatException();
                bg.submit(() -> {
                    try {
                        long total = client.topUpCredits(username, amount);
                        Platform.runLater(() -> {
                            creditsLabel.setText("Credits: " + total);
                            setStatus("Added " + amount + " credits.");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showError("Top-Up Failed", e.getMessage()));
                    }
                });
            } catch (NumberFormatException e) {
                showError("Invalid Amount", "Enter a positive whole number.");
            }
        });
    }

    @FXML
    void onNewWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/application/client/login/Login.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("S-Emulator");
            stage.setScene(new Scene(root, 440, 420));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("New Window Error", "Failed to open new client window: " + e.getMessage());
        }
    }

    @FXML
    void onLogout() {
        onClose();
        // Navigate back to login
        try {
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/application/client/login/Login.fxml"));
            javafx.scene.Parent root = loader.load();
            stage.setTitle("S-Emulator");
            stage.setScene(new Scene(root, 440, 420));
            stage.setResizable(false);
            stage.setOnCloseRequest(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openExecution(String programName, boolean isMain) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/application/client/execution/ClientExecution.fxml"));
            Parent root = loader.load();

            ExecutionController ec = loader.getController();
            ec.init(username, client, programName, isMain);

            Stage stage = new Stage();
            stage.setTitle("S-Emulator  |  " + programName);
            stage.setScene(new Scene(root, 1100, 720));
            stage.setMinWidth(800);
            stage.setMinHeight(500);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Open Error", "Failed to open execution screen: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // Cleanup
    // ─────────────────────────────────────────────

    public void onClose() {
        if (pollTimeline != null) pollTimeline.stop();
        bg.submit(() -> client.logout(username));
        bg.shutdownNow();
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
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

    private static String longStr(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return "0";
        return String.valueOf(((Number) v).longValue());
    }

    private static void bindCol(TableColumn<Map<String, Object>, String> col,
                                java.util.function.Function<Map<String, Object>, String> f) {
        col.setCellValueFactory(cd -> new ReadOnlyStringWrapper(f.apply(cd.getValue())));
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
