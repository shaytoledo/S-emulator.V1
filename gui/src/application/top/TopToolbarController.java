package application.top;

import application.main.MainLayoutController;
import dto.LoadReport;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class TopToolbarController {

    MainLayoutController mainLayoutController;

    @FXML private Button Collapse;
    @FXML private TextField CurrentFromMaximumDegree;
    @FXML private TextField CurrentlyLoadedFilePath;
    @FXML private Button Expand;
    @FXML private ComboBox<?> HighlightSelection;
    @FXML private Button LoadFileButton;
    @FXML private ComboBox<?> ProgramOrFunctionSelector;
    @FXML private TextField expendLevel;

    @FXML private ProgressBar progressBar;

    @FXML private Label statusLabel;

    private File lastDir = new File(System.getProperty("user.home"));


    @FXML
    void LoadListener(ActionEvent event) {
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
            CurrentlyLoadedFilePath.setText(file.getName());
            lastDir = file.getParentFile();
        }
        statusLabel.setText("");


        //LoadReport details = mainLayoutController.engine.loadProgram(Path.of(CurrentlyLoadedFilePath.getText()));
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

            statusLabel.setText("Finished");

            // hide "Finished" after 5s
            javafx.animation.PauseTransition hide = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
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











    @FXML
    void filePathListener(ActionEvent event) {
    }



    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }
}
