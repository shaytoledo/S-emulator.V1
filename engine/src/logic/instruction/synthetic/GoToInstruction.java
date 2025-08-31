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
        return  "GOTO " + target.getLabelRepresentation();

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
            default: {
                Variable tempVar1 = vlm.newZVariable();

                Instruction instr2 = new IncreaseInstruction(tempVar1,getLabel(), argsMap);
                Instruction instr3 = new JumpNotZeroInstruction(tempVar1, target, argsMap);


                myInstructions.add(instr2);
                myInstructions.add(instr3);
                return myInstructions;

            }
        }
    }
}
