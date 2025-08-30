package logic.instruction.basic;

import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.program.VariableAndLabelMenger;
import logic.variable.Variable;

import java.util.List;
import java.util.Map;

public class DecreaseInstruction extends AbstractInstruction {

    public DecreaseInstruction(Variable var, Label lineLabel, Map<String,String> argsMap) {
        super(InstructionData.DECREASE, var, lineLabel, argsMap);
        basic = true;
    }

    public DecreaseInstruction(Variable var, Map<String,String> argsMap) {
        super(InstructionData.DECREASE, var, argsMap);
        basic = true;
    }


    @Override
    public Label execute(ExecutionContext context) {

        long variableValue = context.getVariableValue(getVariable().getRepresentation());
        variableValue = Math.max(0, variableValue - 1);
        context.updateVariable(getVariable().getRepresentation(), variableValue);

        return FixedLabel.EMPTY;
    }

    @Override
    public String toDisplayString() {
        return getVariable().getRepresentation() + " <- " + getVariable().getRepresentation() + " - 1";
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