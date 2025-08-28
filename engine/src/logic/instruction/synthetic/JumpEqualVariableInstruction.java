package logic.instruction.synthetic;

import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.InstructionData;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.Map;

public class JumpEqualVariableInstruction extends AbstractInstruction {

    Label jnzLabel;
    Variable other;

    public JumpEqualVariableInstruction(Variable var, Label target, Variable other, Map<String,String> argsMap) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, var, argsMap);
        this.jnzLabel = target;
        this.other = other;
    }

    public JumpEqualVariableInstruction(Variable var, Label target, Variable other, Label lineLabel, Map<String,String> argsMap) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, var, lineLabel, argsMap);
        this.jnzLabel = target;
        this.other = other;
    }


    @Override
    public Label execute(ExecutionContext context) {
        if (context.getVariableValue(getVariable().getRepresentation()) == context.getVariableValue(other.getRepresentation())) {
            return jnzLabel;
        }
        return FixedLabel.EMPTY;
    }


    @Override
    public String toDisplayString() {
        return "JE " + getVariable().getRepresentation() + " == " + argsMap.getOrDefault("variableName","?") + " -> " + argsMap.getOrDefault("JEVariableLabel","?");
    }
}
