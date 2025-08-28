package logic.program;

import logic.instruction.Instruction;
import logic.label.Label;
import logic.variable.Variable;

import java.util.List;

public interface Program {


    boolean validate();

    int calculateMaxDegree();

    int calculateCycles();

    Instruction getNextInstructionLabel(Instruction currentInstruction);

    Instruction getInstructionByLabel(Label nextLabel);


    String getName();

    void addInstruction(Instruction instruction);

    List<Instruction> getInstructions();

    public List<Variable> getVariables();

    public List<Label> getLabels();

    public Label getExitLabel();

    public List<String> getVariablesPeek();

    public List<String> getLabelsPeek();

    public List<String> getInstructionsPeek();
}
