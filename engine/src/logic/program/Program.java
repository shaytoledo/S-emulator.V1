package logic.program;

import dto.InstructionView;
import logic.instruction.Instruction;
import logic.label.Label;
import logic.variable.Variable;

import java.util.List;

public interface Program {


    int calculateMaxDegree();

    int calculateCycles();

    Instruction getNextInstructionLabel(Instruction currentInstruction);

    Instruction getInstructionByLabel(Label nextLabel);


    String getName();

    void addInstruction(Instruction instruction);

    List<Instruction> getInstructions();

    List<Variable> getVariables();

    List<Label> getLabels();

    Label getExitLabel();

    List<String> getVariablesPeek();

    List<String> getLabelsPeek();

    //void expendToLevelForRun(int level);
    List<List<InstructionView>> expendToLevel(int level);


    // for show
    List<InstructionView> getInstructionsPeek();
}
