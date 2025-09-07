package logic.instruction.synthetic;

import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
import logic.instruction.basic.DecreaseInstruction;
import logic.instruction.basic.JumpNotZeroInstruction;
import logic.instruction.basic.NoOpInstruction;
import logic.label.FixedLabel;
import logic.label.Label;
import core.program.VariableAndLabelMenger;
import logic.variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZeroVariableInstruction extends AbstractInstruction {

    public ZeroVariableInstruction(Variable var) {
        super(InstructionData.ZERO_VARIABLE, var);
    }

    public ZeroVariableInstruction(Variable var, Label label) {
        super(InstructionData.ZERO_VARIABLE, var, label);
    }

    @Override
    public Label execute(ExecutionContext context) {
        context.updateVariable(getVariable(), 0L);
        return FixedLabel.EMPTY;
    }

    @Override
    public String toDisplayString() {
        return getVariable().getRepresentation() + " <- 0";
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
            default: {
                Label label = vlm.newLabel();
                Instruction instr1 = new NoOpInstruction(getVariable(), getLabel());
                Instruction instr2 = new DecreaseInstruction(getVariable(), label);
                Instruction instr3 = new JumpNotZeroInstruction(getVariable(), label);
                myInstructions.add(instr1);
                myInstructions.add(instr2);
                myInstructions.add(instr3);
                return myInstructions;

            }
        }
    }
}

