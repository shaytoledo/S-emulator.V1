package logic.instruction.basic;

import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.InstructionData;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.Map;

public class NoOpInstruction extends AbstractInstruction {

    public NoOpInstruction(Variable var, Label lineLabel, Map<String,String> argsMap) {
        super(InstructionData.NO_OP, var, lineLabel, argsMap);
        basic = true;
    }

    public NoOpInstruction(Variable var, Map<String,String> argsMap) {
        super(InstructionData.NO_OP, var, argsMap);
        basic = true;
    }


    @Override
    public Label execute(ExecutionContext context) {
        return FixedLabel.EMPTY;
    }

    @Override
    public String toDisplayString() {
        return getVariable().getRepresentation() + " <- " + getVariable().getRepresentation();
    }
}
