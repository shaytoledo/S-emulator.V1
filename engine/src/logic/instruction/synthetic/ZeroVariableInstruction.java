package logic.instruction.synthetic;

import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.InstructionData;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.Map;

public class ZeroVariableInstruction extends AbstractInstruction {

    public ZeroVariableInstruction(Variable var, Map<String,String> argsMap) {
        super(InstructionData.ZERO_VARIABLE, var, argsMap);
    }

    public ZeroVariableInstruction(Variable var, Label label, Map<String,String> argsMap) {
        super(InstructionData.ZERO_VARIABLE, var, label, argsMap);
    }
    @Override
    public Label execute(ExecutionContext context) {
        context.updateVariable(getVariable().getRepresentation(), 0L);
        return FixedLabel.EMPTY;
    }


    @Override
    public String toDisplayString() {
        return getVariable().getRepresentation() + " <- 0";
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
