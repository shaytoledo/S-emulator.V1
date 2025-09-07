package logic.instruction.basic;

import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
import logic.label.FixedLabel;
import logic.label.Label;
import core.program.VariableAndLabelMenger;
import logic.variable.Variable;

import java.util.List;
import java.util.Map;

public class JumpNotZeroInstruction extends AbstractInstruction {

    private final Label jnzLabel;

    public JumpNotZeroInstruction(Variable var, Label target, Label lineLabel) {
        super(InstructionData.JUMP_NOT_ZERO, var, lineLabel);
        this.jnzLabel = target;
        basic = true;
    }

    public JumpNotZeroInstruction(Variable var, Label target) {
        super(InstructionData.JUMP_NOT_ZERO, var);
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
    
    @Override
    public int getMaxLevel() {
        return 0;
    }

    @Override
    public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {
        return List.of(this);

    }
}
