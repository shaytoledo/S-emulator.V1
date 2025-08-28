package logic.instruction.synthetic;

import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.InstructionData;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.Map;

public class JumpZeroInstruction extends AbstractInstruction {

    private final Label jnzLabel;

    public JumpZeroInstruction(Variable var, Label target, Map<String,String> argsMap) {
        super(InstructionData.JUMP_ZERO, var, argsMap);
        this.jnzLabel = target;
    }

    public JumpZeroInstruction(Variable var, Label target, Label lineLabel, Map<String,String> argsMap) {
        super(InstructionData.JUMP_ZERO, var, lineLabel, argsMap);
        this.jnzLabel = target;
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable().getRepresentation());

        if (variableValue == 0) {
            return jnzLabel;
        }
        return FixedLabel.EMPTY;

    }


    @Override
    public String toDisplayString() {
        return  "JZ " + getVariable().getRepresentation() + " -> " + argsMap.getOrDefault("JZLabel","?");
    }
}
