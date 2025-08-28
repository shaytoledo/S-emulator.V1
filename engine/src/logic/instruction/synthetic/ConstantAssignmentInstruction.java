package logic.instruction.synthetic;

import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.InstructionData;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.Map;

public class ConstantAssignmentInstruction extends AbstractInstruction {

    private final long constant;

    public ConstantAssignmentInstruction(Variable target, long constant, Map<String,String> argsMap) {
        super(InstructionData.CONSTANT_ASSIGNMENT, target, argsMap);
        this.constant = constant;
    }

    public ConstantAssignmentInstruction(Variable target, long constant, Label label, Map<String,String> argsMap) {
        super(InstructionData.CONSTANT_ASSIGNMENT, target, label, argsMap);
        this.constant = constant;
    }

    @Override
    public Label execute(ExecutionContext context) {
        context.updateVariable(getVariable().getRepresentation(), constant);
        return FixedLabel.EMPTY;
    }


    @Override
    public String toDisplayString() {
        return getVariable().getRepresentation() + " <- " + argsMap.getOrDefault("constantValue","?");
    }
}
