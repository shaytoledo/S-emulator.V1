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

public class DecreaseInstruction extends AbstractInstruction {

    public DecreaseInstruction(Variable var, Label lineLabel) {
        super(InstructionData.DECREASE, var, lineLabel);
        basic = true;
    }

    public DecreaseInstruction(Variable var) {
        super(InstructionData.DECREASE, var);
        basic = true;
    }


    @Override
    public Label execute(ExecutionContext context) {

        long variableValue = context.getVariableValue(getVariable());
        variableValue = Math.max(0, variableValue - 1);
        context.updateVariable(getVariable(), variableValue);

        return FixedLabel.EMPTY;
    }

    @Override
    public String toDisplayString() {
        return getVariable().getRepresentation() + " <- " + getVariable().getRepresentation() + " - 1";
    }

    @Override
    public int getMaxLevel() {
        return 0;
    }

    @Override
     public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {
        return List.of(this);

     }

    @Override
    public List<String> getAllInfo() {
        List<String> list = new ArrayList<>();
        if (getLabel() != null) {
            list.add(getLabel().getLabelRepresentation());
        }
        if (getVariable() != null) {
            list.add(getVariable().getRepresentation());
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


}