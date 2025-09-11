package application.left;

import application.main.MainLayoutController;
import dto.InstructionView;
import dto.ProgramSummary;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import logic.exception.LoadProgramException;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.reverse;

public class LeftToolbarController {

    MainLayoutController mainLayoutController;

    @FXML private TableColumn<InstructionView, String> colBS;
    @FXML private TableColumn<InstructionView, Integer> colCycles;
    @FXML private TableColumn<InstructionView, Integer> colNumber;
    @FXML private TableColumn<InstructionView, String> colInstruction;
    @FXML private TableColumn<InstructionView, String> colLabel;
    @FXML private TableView<InstructionView> instructionsTable;
    @FXML private TableColumn<InstructionView, String> colHistoryBS;
    @FXML private TableColumn<InstructionView, Integer> colHistoryCycles;
    @FXML private TableColumn<InstructionView, Integer> colHistoryNumber;
    @FXML private TableColumn<InstructionView, String> colHistoryInstruction;
    @FXML private TableColumn<InstructionView, String> colHistoryLabel;
    @FXML private TableView<InstructionView> instructionsHistoryTable;
    @FXML private VBox left;
    @FXML private TextField SummaryLine;
    @FXML private Label SelectedInstructionHistoryChain;


    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void clearAll() {
        instructionsTable.getItems().clear();
        clearHistory();
        SummaryLine.setText("");
    }
    public void clearHistory() {
        instructionsHistoryTable.getItems().clear();
    }

    public void showProgram(int level) {
        try {
            List<List<InstructionView>> extendInstructions =
                    mainLayoutController.engine.expandProgramToLevelForExtend(level);


            // table columns
            colNumber.setCellValueFactory(cell ->
                    new ReadOnlyObjectWrapper<>(instructionsTable.getItems().indexOf(cell.getValue()) + 1));
            colBS.setCellValueFactory(cell ->
                    new ReadOnlyStringWrapper(String.valueOf(cell.getValue().type())));
            colInstruction.setCellValueFactory(cell ->
                    new ReadOnlyStringWrapper(cell.getValue().command()));
            colCycles.setCellValueFactory(cell ->
                    new ReadOnlyObjectWrapper<>(cell.getValue().cycles()));
            colLabel.setCellValueFactory(cell ->
                    new ReadOnlyStringWrapper(cell.getValue().label()));

            List<InstructionView> needed = extendInstructions.stream()
                    .filter(list -> list != null && !list.isEmpty())
                    .map(list -> list.get(list.size() - 1))
                    .toList();

            instructionsTable.getItems().setAll(needed);
            summary(needed);
        } catch (LoadProgramException e) {
            System.out.println(e.getMessage());
        }
    }
    public void showProgram() {
        int level = mainLayoutController.getCurrentLevel();
        showProgram(level);
    }

    private void summary(List<InstructionView> loadedInstructions) {
        long total = loadedInstructions.size();
        long syntheticCount = loadedInstructions.stream()
                .filter(iv -> String.valueOf(iv.type()).equalsIgnoreCase("S"))
                .count();
        long basicCount = loadedInstructions.stream()
                .filter(iv -> String.valueOf(iv.type()).equalsIgnoreCase("B"))
                .count();

        SummaryLine.setText(
                String.format("Total: %d | Synthetic: %d | Basic: %d", total, syntheticCount, basicCount)
        );

    }

    @FXML
    private void showHistoryChain(MouseEvent event) {
        if (event.getClickCount() == 1) {
            InstructionView selected = instructionsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int rowIndex = instructionsTable.getSelectionModel().getSelectedIndex();

                List<List<InstructionView>> extendInstructions =
                        mainLayoutController.engine.expandProgramToLevelForExtend(
                                mainLayoutController.getCurrentLevel()
                        );

                // logic to get the history chain of the selected instruction
                List<InstructionView> chain = extendInstructions.get(rowIndex);
                List<InstructionView> toShow = new ArrayList<>(chain);
                reverse(toShow);

                colHistoryNumber.setCellValueFactory(cell ->
                        new ReadOnlyObjectWrapper<>(toShow.indexOf(cell.getValue()) + 1));
                colHistoryBS.setCellValueFactory(cell ->
                        new ReadOnlyStringWrapper(String.valueOf(cell.getValue().type())));
                colHistoryInstruction.setCellValueFactory(cell ->
                        new ReadOnlyStringWrapper(cell.getValue().command()));
                colHistoryCycles.setCellValueFactory(cell ->
                        new ReadOnlyObjectWrapper<>(cell.getValue().cycles()));
                colHistoryLabel.setCellValueFactory(cell ->
                        new ReadOnlyStringWrapper(cell.getValue().label()));
                instructionsHistoryTable.getItems().setAll(toShow);
            }
        }
    }
}