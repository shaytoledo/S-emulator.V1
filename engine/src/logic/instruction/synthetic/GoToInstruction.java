package logic.instruction.synthetic;

import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
import logic.instruction.basic.IncreaseInstruction;
import logic.instruction.basic.JumpNotZeroInstruction;
import logic.label.Label;
import core.program.VariableAndLabelMenger;
import logic.variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GoToInstruction extends AbstractInstruction {

    private final Label target;

    public GoToInstruction(Variable var, Label target) {
        super(InstructionData.GOTO_LABEL, var);
        this.target = target;
    }

    public GoToInstruction(Variable var, Label target, Label lineLabel) {
        super(InstructionData.GOTO_LABEL, var, lineLabel);
        this.target = target;
    }

    @Override
    public Label execute(ExecutionContext context) {
        return target;
    }

    @Override
    public String toDisplayString() {
        return  "GOTO " + target.getLabelRepresentation();

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
        list.add(target.getLabelRepresentation());
        return list;
    }

    @Override
    public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {
        List<Instruction> myInstructions = new ArrayList<>();

        switch (extensionLevel) {
            case 0:
                return List.of(this);
            default: {
                Variable tempVar1 = vlm.newZVariable();

                Instruction instr2 = new IncreaseInstruction(tempVar1,getLabel());
                Instruction instr3 = new JumpNotZeroInstruction(tempVar1, target);


                myInstructions.add(instr2);
                myInstructions.add(instr3);
                return myInstructions;

            }
        }
    }
}
