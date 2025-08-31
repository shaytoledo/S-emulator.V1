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

    @Override
    public Map<String, String> args() {
        return argsMap;
    }

    @Override
    public int getMaxLevel() {
        return 0;
    }

    @Override
    public List<Instruction> extend(int extentionLevel, VariableAndLabelMenger vlm) {
        return List.of(this);

    }
}
