package logic.instruction.synthetic;

import core.program.VariableAndLabelMenger;
import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
import logic.instruction.basic.IncreaseInstruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.ArrayList;
import java.util.List;

public class ConstantAssignmentInstruction extends AbstractInstruction {

    private final long constant;

    public ConstantAssignmentInstruction(Variable target, long constant) {
        super(InstructionData.CONSTANT_ASSIGNMENT, target);
        this.constant = constant;
    }

    public ConstantAssignmentInstruction(Variable target, long constant, Label label) {
        super(InstructionData.CONSTANT_ASSIGNMENT, target, label);
        this.constant = constant;
    }

    @Override
    public Label execute(ExecutionContext context) {
        context.updateVariable(getVariable(), constant);
        return FixedLabel.EMPTY;
    }

    @Override
    public String toDisplayString() {
        return getVariable().getRepresentation() + " <- " + constant;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    public List<String> getAllInfo() {
        List<String> list = new ArrayList<>();
        if (getLabel() != null) {
            list.add(getLabel().getLabelRepresentation());
        }
        if (getVariable() != null) {
            list.add(getVariable().getRepresentation());
        }
        return list;
    }

    @Override
    public List<Variable> getAllVariables() {
        return List.of(getVariable());
    }

    @Override
    public List<Label> getAllLabels() {
        if (getLabel() == null) {
            return List.of();
        } else {
            return List.of(getLabel());
        }
    }

    @Override
    public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {
        List<Instruction> myInstructions = new ArrayList<>();

        switch (extensionLevel) {
            case 0:
                return List.of(this);
            case 1: {
                Variable v = getVariable();
                Instruction inst1 = new ZeroVariableInstruction(v,getLabel());
                myInstructions.add(inst1);
                Instruction instr2 = new IncreaseInstruction(v);
                myInstructions.add(instr2);

                return myInstructions;

            }
            default:
                Variable v = getVariable();

                Instruction inst1 = new ZeroVariableInstruction(v,getLabel());
                List<Instruction> zeroExtend = inst1.extend(extensionLevel - 1, vlm);
                myInstructions.addAll(zeroExtend);

                Instruction instr2 = new IncreaseInstruction(v);
                myInstructions.add(instr2);
                return myInstructions;
        }
    }
}
