package logic.instruction.synthetic;

import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.InstructionData;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.Map;

public class GoToInstruction extends AbstractInstruction {

    private final Label target;

    public GoToInstruction(Variable var, Label target, Map<String,String> argsMap) {
        super(InstructionData.GOTO_LABEL, var, argsMap);
        this.target = target;
    }

    public GoToInstruction(Variable var, Label target, Label lineLabel, Map<String,String> argsMap) {
        super(InstructionData.GOTO_LABEL, var, lineLabel, argsMap);
        this.target = target;
    }

    @Override
    public Label execute(ExecutionContext context) {
        return target;
    }


    @Override
    public String toDisplayString() {
        return  "GOTO " + argsMap.getOrDefault("gotoLabel","?");
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
