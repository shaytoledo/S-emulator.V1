package application.top;

import application.main.MainLayoutController;
import core.program.VariableAndLabelMenger;
import dto.InstructionView;
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
import java.util.*;
import java.util.stream.Collectors;

import static javafx.util.Duration.seconds;


public class TopToolbarController {

    MainLayoutController mainLayoutController;

    // Won't be used
    @FXML public Button Expand;
    @FXML public Button Collapse;
    @FXML private Label CurrentFromMaximumDegree;

    // Need to be implemented
    @FXML public ComboBox<String> HighlightSelection;
    @FXML private ComboBox<?> ProgramOrFunctionSelector;

    @FXML private TextField howMuchToCollapse;
    @FXML private TextField howMuchToExpand;
    @FXML private TextField CurrentlyLoadedFilePath;
    @FXML private Button LoadFileButton;
    @FXML private TextField expendLevel;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;

    private File lastDir = new File(System.getProperty("user.home"));
    private int currentLevel = 0;
    private PauseTransition clearStatusLater;



    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void clearAll() {
        HighlightSelection.getItems().clear();
        expendLevel.setText("");
        currentLevel = 0;
    }

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
        } else {
            return;
        }

        // Safely reset the status label before (re)binding it in startLoadFileProgress
        if (clearStatusLater != null) { // cancel any previous clear-timer
            clearStatusLater.stop();
            clearStatusLater = null;
        }
        statusLabel.textProperty().unbind();  // IMPORTANT: unbind before setText
        statusLabel.setText("");

        startLoadFileProgress(file);
    }

    /** Functions to load by the engine the file and to show the progressbar of the load thread **/
    private void startLoadFileProgress(File file) {
        // Cancel any previous "clear status" timer to avoid late setText("") on a bound label
        if (clearStatusLater != null) {
            clearStatusLater.stop();
            clearStatusLater = null;
        }

        Task<LoadReport> task = createLoadTask(file);

        // Bind UI to task state
        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());
        progressBar.visibleProperty().bind(task.runningProperty());
        LoadFileButton.disableProperty().bind(task.runningProperty());

        task.setOnSucceeded(ev -> {
            // Always unbind before setting values manually
            progressBar.progressProperty().unbind();
            statusLabel.textProperty().unbind();
            progressBar.visibleProperty().unbind();
            LoadFileButton.disableProperty().unbind();

            LoadReport report = task.getValue();

            if (report != null && report.errors() != null && !report.errors().isEmpty()) {
                // Now safe to set text because we unbound above
                statusLabel.setText("errors (" + report.errors().size() + ")");
                showErrorsPopup(report.errors());
                mainLayoutController.showProgram();
            } else {
                statusLabel.setText("Finished");
                currentLevel = 0;
                CurrentlyLoadedFilePath.setText(file.getAbsolutePath());
                mainLayoutController.clearAll();
            }

            // Schedule a safe clear of the status label
            clearStatusLater = new PauseTransition(seconds(5));
            clearStatusLater.setOnFinished(e -> {
                // Only clear if label is not currently bound (e.g., a new task may have started)
                if (!statusLabel.textProperty().isBound()) {
                    statusLabel.setText("");
                }
            });
            clearStatusLater.play();

            // Show the program if a file is loaded
            String path = CurrentlyLoadedFilePath.getText();
            if (path != null && !path.isEmpty() && !path.equals("Currently Loaded File")) {
                mainLayoutController.showProgram();
            }
        });

        task.setOnFailed(ev -> {
            // Unbind UI first
            progressBar.progressProperty().unbind();
            statusLabel.textProperty().unbind();
            progressBar.visibleProperty().unbind();
            LoadFileButton.disableProperty().unbind();

            Throwable ex = task.getException();
            statusLabel.setText("ERROR: " + (ex != null ? ex.getMessage() : "Unknown"));
            if (ex != null) ex.printStackTrace();

            // Also clear the status after a short delay, safely
            clearStatusLater = new PauseTransition(seconds(5));
            clearStatusLater.setOnFinished(e -> {
                if (!statusLabel.textProperty().isBound()) {
                    statusLabel.setText("");
                }
            });
            clearStatusLater.play();
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
    void collapseListener(ActionEvent event) {
        if (currentLevel > 0 ) {
            try {
                if (currentLevel - Integer.parseInt(howMuchToCollapse.getText()) > mainLayoutController.engine.getMaxExpandLevel() ||
                        currentLevel - Integer.parseInt(howMuchToCollapse.getText()) < 0) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Collapse");
                    alert.setHeaderText(null);
                    alert.setContentText("The collapse value minus the current level exceeds the minimum expand level.");
                    alert.showAndWait();
                } else {
                    currentLevel -= Integer.parseInt(howMuchToCollapse.getText());
                    mainLayoutController.showProgram();
                    mainLayoutController.getLeft().clearHistory();
                }
                howMuchToCollapse.setText("1");

            } catch (Exception e) {
                howMuchToCollapse.setText("1");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Collapse Value");
                alert.setHeaderText(null);
                alert.setContentText("The collapse value must be a number.");
                alert.showAndWait();
            }
        }
    }

    @FXML
    void extendListener(ActionEvent event) {
        if (currentLevel < mainLayoutController.engine.getMaxExpandLevel()) {
            try {
                if (Integer.parseInt(howMuchToExpand.getText()) + currentLevel > mainLayoutController.engine.getMaxExpandLevel()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Extend");
                    alert.setHeaderText(null);
                    alert.setContentText("The Extend value plus the current level exceeds the maximum expand level.");
                    alert.showAndWait();
                } else {
                    currentLevel += Integer.parseInt(howMuchToExpand.getText());
                    mainLayoutController.showProgram();
                    mainLayoutController.getLeft().clearHistory();
                }
                howMuchToExpand.setText("1");

            } catch (Exception e) {
                howMuchToExpand.setText("1");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Extend Value");
                alert.setHeaderText(null);
                alert.setContentText("The collapse value must be a number.");
                alert.showAndWait();
            }
        }
    }

    public void showProgram() {
        int maxDegree = mainLayoutController.engine.getMaxExpandLevel();
        expendLevel.setText(currentLevel+ "/" +maxDegree);
        updateHighlighting();
    }

    public int  getCurrentLevel() {
        return currentLevel;
    }

    private void updateHighlighting() {
        /// all the variable and label in the expended program, now need to know where is each label and variable
        /// X Y Z
        VariableAndLabelMenger vlm = mainLayoutController.engine.getVlm();
        List<String> allVariables = vlm.getAll();

        List<String> xVariables = allVariables.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.regionMatches(true, 0, "x", 0, 1))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        List<String> zVariables = allVariables.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.regionMatches(true, 0, "z", 0, 1))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        List<String> yVariables = allVariables.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.regionMatches(true, 0, "y", 0, 1))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();


        // L*
        List<String> lLabels = allVariables.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.regionMatches(true, 0, "l", 0, 1))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        List<String> options = new ArrayList<>(yVariables);
        options.addAll(xVariables);
        options.addAll(zVariables);
        options.addAll(lLabels);

        HighlightSelection.getItems().setAll(options);
        HighlightSelection.setPromptText("Choose variable/label…");
        HighlightSelection.getSelectionModel().clearSelection();
    }

    @FXML
    void selectionListener(ActionEvent event) {
        Set<Integer> indices = getHighlightedIndices();
        mainLayoutController.getLeft().boldRows(indices);
    }

    public Set<Integer> getHighlightedIndices() {
        String selected = HighlightSelection.getSelectionModel().getSelectedItem();
        List<List<String>> all = mainLayoutController.engine.getInfoForEachInstruction(mainLayoutController.getCurrentLevel());

        List<Integer> indices = new ArrayList<>();
        int index = 0;
        for (List<String> varsAndLabels : all) {
            if (varsAndLabels.contains(selected)) {
                indices.add(index);
            }
            index++;
        }
        return new HashSet<>(indices);
    }
}