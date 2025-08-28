package logic.instruction.basic;

import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.InstructionData;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.Map;

public class JumpNotZeroInstruction extends AbstractInstruction {

    private final Label jnzLabel;

    public JumpNotZeroInstruction(Variable var, Label target, Label lineLabel, Map<String,String> argsMap) {
        super(InstructionData.JUMP_NOT_ZERO, var, lineLabel, argsMap);
        this.jnzLabel = target;
        basic = true;
    }

    public JumpNotZeroInstruction(Variable var, Label target, Map<String,String> argsMap) {
        super(InstructionData.JUMP_NOT_ZERO, var, argsMap);
        this.jnzLabel = target;
        basic = true;
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable().getRepresentation());

        if (variableValue != 0) {
            return jnzLabel;
        }
        return FixedLabel.EMPTY;

    }

    @Override
    public String toDisplayString() {
        return "IF " + getVariable().getRepresentation() + "!=0 GOTO " + jnzLabel.getLabelRepresentation();
    }
}
