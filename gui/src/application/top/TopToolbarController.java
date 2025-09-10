package application.top;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class TopToolbarController {

    @FXML
    private Button Collapse;

    @FXML
    private TextField CurrentFromMaximumDegree;

    @FXML
    private TextField CurrentlyLoadedFilePath;

    @FXML
    private Button Expand;

    @FXML
    private ComboBox<?> HighlightSelection;

    @FXML
    private Button LoadFileButton;

    @FXML
    private ComboBox<?> ProgramOrFunctionSelector;

    @FXML
    private TextField expendLevel;

    @FXML
    void LoadListener(ActionEvent event) {
        String filePath = CurrentlyLoadedFilePath.getText();


    }

    @FXML
    void filePathListener(ActionEvent event) {


    }

}
