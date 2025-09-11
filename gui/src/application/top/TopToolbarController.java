package application.top;

import application.main.MainLayoutController;
import dto.LoadReport;
import javafx.animation.PauseTransition;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

import static javafx.util.Duration.seconds;


public class TopToolbarController {

    MainLayoutController mainLayoutController;

    @FXML private Button Collapse;
    @FXML private Label CurrentFromMaximumDegree;
    @FXML private TextField CurrentlyLoadedFilePath;
    @FXML private Button Expand;
    @FXML private ComboBox<?> HighlightSelection;
    @FXML private Button LoadFileButton;
    @FXML private ComboBox<?> ProgramOrFunctionSelector;
    @FXML private TextField expendLevel;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    @FXML private Button showProgram;

    private File lastDir = new File(System.getProperty("user.home"));
    private int currentLevel = 0;


    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void clearAll() {
        expendLevel.setText("");
        currentLevel = 0;
    }

    /** Functions to load by the engine the file and to show the progressbar of the load thread **/
    @FXML
    void LoadListener(ActionEvent event) throws InterruptedException {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open Resource File");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml")
        );
        if (lastDir != null && lastDir.isDirectory()) {
            fc.setInitialDirectory(lastDir);
        }
        Stage stage = (Stage) ((Node) LoadFileButton).getScene().getWindow();

        File file = fc.showOpenDialog(stage);
        if (file != null) {
            mainLayoutController.clearAll();
            lastDir = file.getParentFile();
        }
        else {
            return;
        }
        statusLabel.setText("");

        startLoadFileProgress(file);
    }
    private void startLoadFileProgress(File file) {
        Task<LoadReport> task = createLoadTask(file);

        // bind UI
        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());
        progressBar.visibleProperty().bind(task.runningProperty());
        LoadFileButton.disableProperty().bind(task.runningProperty());

        task.setOnSucceeded(ev -> {
            // unbind
            progressBar.progressProperty().unbind();
            statusLabel.textProperty().unbind();
            progressBar.visibleProperty().unbind();
            LoadFileButton.disableProperty().unbind();

            LoadReport report = task.getValue();

            if (report != null && report.errors() != null && !report.errors().isEmpty()) {
                statusLabel.setText("errors (" + report.errors().size() + ")");
                showErrorsPopup(report.errors());
                mainLayoutController.showProgram();
            } else {
                statusLabel.setText("Finished");
                currentLevel = 0;
                CurrentlyLoadedFilePath.setText(file.getName());
                mainLayoutController.clearAll();
            }
            // hide "Finished" after 5s
            PauseTransition hide = new PauseTransition(seconds(5));
            hide.setOnFinished(e  -> statusLabel.setText(""));
            hide.play();
        });

        task.setOnFailed(ev -> {
            progressBar.progressProperty().unbind();
            statusLabel.textProperty().unbind();
            progressBar.visibleProperty().unbind();
            LoadFileButton.disableProperty().unbind();

            Throwable ex = task.getException();
            statusLabel.setText("ERROR: " + (ex != null ? ex.getMessage() : "Unknown"));
            if (ex != null) ex.printStackTrace();
        });

        new Thread(task, "load-program-task").start();

    }

    private Task<LoadReport> createLoadTask(File file) {
        return new Task<>() {
            @Override
            protected LoadReport call() throws Exception {
                updateMessage("Loading...");
                updateProgress(-1, 1);        // indeterminate
                LoadReport report = mainLayoutController.engine.loadProgram(file.toPath());
                updateProgress(1, 1);         // 100%
                updateMessage("Finished");
                return report;
            }
        };
    }
    // Pop up a window to show the errors occurred during the load
    private void showErrorsPopup(List<Exception> errors) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
        alert.setTitle("Load Errors");
        alert.setHeaderText("Loading completed with errors (" + errors.size() + ")");

        // Short messages list (bullet points)
        String messages =
                errors.stream()
                        .map(ex -> ex.getMessage() != null && !ex.getMessage().isBlank()
                                ? ex.getMessage()
                                : ex.getClass().getSimpleName())
                        .map(msg -> "• " + msg)
                        .collect(Collectors.joining("\n"));

        Label msgsLabel = new Label(messages);
        msgsLabel.setWrapText(true);

        VBox content = new VBox(8, msgsLabel);
        content.setFillWidth(true);

        // Expandable content: full stack traces
        String traces = errors.stream()
                .map(this::stackTraceToString)
                .collect(Collectors.joining("\n\n"));

        TextArea details = new TextArea(traces);
        details.setEditable(false);
        details.setWrapText(false);
        details.setPrefRowCount(16);
        details.setPrefColumnCount(100);

        DialogPane dp = alert.getDialogPane();
        dp.setContent(content);
        dp.setExpandableContent(details);
        dp.setExpanded(false); // collapsed by default

        alert.showAndWait();
    }
    private String stackTraceToString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }


    @FXML
    void showListener(ActionEvent event) {
        if (CurrentlyLoadedFilePath.getText().equals("Currently Loaded File") || CurrentlyLoadedFilePath.getText().isEmpty()) {
            return;
        } else {
            mainLayoutController.showProgram();
        }
    }

    @FXML
    void collapseListener(ActionEvent event) {
        if (currentLevel > 0) {
            currentLevel--;
            mainLayoutController.showProgram();
            mainLayoutController.getLeft().clearHistory();
        }
    }

    @FXML
    void extendListener(ActionEvent event) {
        if (currentLevel < mainLayoutController.engine.getMaxExpandLevel()) {
            currentLevel++;
            mainLayoutController.showProgram();
            mainLayoutController.getLeft().clearHistory();
        }
    }

    public void showProgram() {
        int maxDegree = mainLayoutController.engine.getMaxExpandLevel();
        expendLevel.setText(currentLevel+ "/" +maxDegree);
    }

    public int  getCurrentLevel() {
        return currentLevel;
    }
}
