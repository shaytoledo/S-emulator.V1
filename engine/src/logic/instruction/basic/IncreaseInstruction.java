package logic.instruction.basic;

import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.InstructionData;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.Map;

public class IncreaseInstruction extends AbstractInstruction {

    public IncreaseInstruction(Variable var, Label lineLabel, Map<String,String> argsMap) {
        super(InstructionData.INCREASE, var, lineLabel, argsMap);
        basic = true;
    }

    public IncreaseInstruction(Variable var, Map<String,String> argsMap) {
        super(InstructionData.INCREASE, var, argsMap);
        basic = true;
    }

    @Override
    public Label execute(ExecutionContext context) {

        long variableValue = context.getVariableValue(getVariable().getRepresentation());
        variableValue++;
        context.updateVariable(getVariable().getRepresentation(), variableValue);

        return FixedLabel.EMPTY;
    }

    @Override
    public String toDisplayString() {
        return getVariable().getRepresentation() + " <- " + getVariable().getRepresentation() + " + 1";
    }
}