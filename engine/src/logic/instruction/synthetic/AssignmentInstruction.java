package logic.instruction.synthetic;

import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.InstructionData;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.Map;

public class AssignmentInstruction extends AbstractInstruction {

    Variable assignedVariable;

    public AssignmentInstruction(Variable target, Variable source, Map<String,String> argsMap) {
        super(InstructionData.ASSIGNMENT, target, argsMap);
        this.assignedVariable = source;
    }

    public AssignmentInstruction(Variable target, Variable source, Label label, Map<String,String> argsMap) {
        super(InstructionData.ASSIGNMENT, target, label, argsMap);
        this.assignedVariable = source;
    }


    @Override
    public Label execute(ExecutionContext context) {
        long assignedValue = context.getVariableValue(assignedVariable.getRepresentation());
        context.updateVariable(getVariable().getRepresentation(), assignedValue);
        return FixedLabel.EMPTY;
    }


    @Override
    public String toDisplayString() {
        //return getVariable().getRepresentation() + " <- " + argsMap.getOrDefault("assignedVariable","?");
        return getVariable().getRepresentation() + " <- " + assignedVariable.getRepresentation();

    }
}
