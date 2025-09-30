package logic.instruction.basic;

import core.program.VariableAndLabelMenger;
import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.ArrayList;
import java.util.List;

public class NoOpInstruction extends AbstractInstruction {

    public NoOpInstruction(Variable var, Label lineLabel) {
        super(InstructionData.NO_OP, var, lineLabel);
        basic = true;
    }

    public NoOpInstruction(Variable var) {
        super(InstructionData.NO_OP, var);
        basic = true;
    }

    @Override
    public Instruction clone() {
        if(getLabel() == null)
            return new NoOpInstruction(getVariable());
        else {
            return new NoOpInstruction(getVariable(), getLabel());
        }
    }

    @Override
    public Label execute(ExecutionContext context, VariableAndLabelMenger vlm) {
        return FixedLabel.EMPTY;
    }

    @Override
    public String toDisplayString() {
        return getVariable().getRepresentation() + " <- " + getVariable().getRepresentation();
    }

    @Override
    public int getMaxLevel() {
        return 0;
    }

    @Override
    public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {
        return List.of(this.clone());
    }

    @Override
    public List<String> getAllInfo() {
        List<String> list = new ArrayList<>();
        if (getLabel() != null) {
            list.add(getLabel().getLabelRepresentation());
        }
        return list;
    }
    @Override
    public List<Variable> getAllVariables() {
        return List.of(getVariable());
    }

    @Override
    public List<Label> getAllLabels() {
        if (getLabel() == null) {
            return List.of();
        } else {
            return List.of(getLabel());
        }
    }

    @Override
    public void replace(Variable oldVar, Variable newVar) {
        if(getVariable().equals(oldVar)) {
            setVariable(newVar);
        }
    }

    @Override
    public void replace(Label oldLabel, Label newLabel) {
        if(getLabel().equals(oldLabel)) {
            setLabel(newLabel);
        }
    }
}
