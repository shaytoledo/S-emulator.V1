package logic.instruction;

import logic.instruction.basic.NoOpInstruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;
import logic.variable.VariableImpl;

import java.util.List;

public abstract class AbstractInstruction implements Instruction {

    private final InstructionData instructionData;
    private Label label;
    private Variable variable;
    public boolean basic = false;

    public List<Instruction> myInstructions;


    public AbstractInstruction(InstructionData instructionData, Variable variable ) {
        this(instructionData, variable, FixedLabel.EMPTY);
    }

    public AbstractInstruction(InstructionData instructionData, Variable variable, Label label) {
        this.instructionData = instructionData;
        this.label = label;
        this.variable = variable;

    }

    public void setBasic(boolean isBasic) {
        this.basic = isBasic;
    }

    @Override
    public String getName() {
        return instructionData.getName();
    }

    @Override
    public int cycles() {
        return instructionData.getCycles();
    }

    @Override
    public Label getLabel() {
        return label;
    }

    @Override
    public Variable getVariable() {
        return variable;
    }

    @Override
    public boolean isBasic() {
        return basic;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Instruction clone() {
        return new NoOpInstruction(getVariable());
    }

}