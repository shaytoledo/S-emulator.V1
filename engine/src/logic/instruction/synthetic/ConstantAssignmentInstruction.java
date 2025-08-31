package logic.instruction.synthetic;

import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
import logic.instruction.basic.IncreaseInstruction;
import logic.label.FixedLabel;
import logic.label.Label;
import core.program.VariableAndLabelMenger;
import logic.variable.Variable;

import java.util.ArrayList;
import java.util.List;
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

    @Override
    public Map<String, String> args() {
        return argsMap;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {
        List<Instruction> myInstructions = new ArrayList<>();

        switch (extensionLevel) {
            case 0:
                return List.of(this);
            case 1: {
                Variable v = getVariable();
                Instruction inst1 = new ZeroVariableInstruction(v,getLabel(), argsMap);
                myInstructions.add(inst1);
                Instruction instr2 = new IncreaseInstruction(v, argsMap);
                myInstructions.add(instr2);

                return myInstructions;

            }
            default:
                Variable v = getVariable();

                Instruction inst1 = new ZeroVariableInstruction(v,getLabel(), argsMap);
                List<Instruction> zeroExtend = inst1.extend(extensionLevel - 1, vlm);
                myInstructions.addAll(zeroExtend);

                Instruction instr2 = new IncreaseInstruction(v, argsMap);
                myInstructions.add(instr2);
                return myInstructions;
        }
    }
}
