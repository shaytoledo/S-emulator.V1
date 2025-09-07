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
    public Label execute(ExecutionContext context) {
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
        return List.of(this);
    }
}
