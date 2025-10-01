package core.program;

import dto.InstructionView;
import dto.RunSummary;
import dto.functionView;
import logic.instruction.Instruction;
import logic.label.Label;

import java.util.ArrayList;
import java.util.List;

public interface Program {

    int calculateMaxDegree();
    Instruction getNextInstructionLabel(Instruction currentInstruction);
    Instruction getInstructionByLabel(Label nextLabel);
    String getName();
    List<Instruction> getInstructions();
    List<String> getVariablesPeek();
    List<String> getLabelsPeek();
    List<List<InstructionView>> expendToLevelForExtend(int level);
    List<InstructionView> instructionViewsAfterExtendRunShow(int level);
    List<InstructionView> getInstructionsPeek();
    VariableAndLabelMenger getvlm();
    List<List<String>> getInfo(int level);
    List<Function> getFunctions();
    List<functionView> getAllFunctionViews();


  List<RunSummary> getsummaries();

}
