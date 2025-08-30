package logic.instruction.synthetic;

import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
import logic.instruction.basic.DecreaseInstruction;
import logic.instruction.basic.IncreaseInstruction;
import logic.instruction.basic.JumpNotZeroInstruction;
import logic.instruction.basic.NoOpInstruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.program.VariableAndLabelMenger;
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

        //return  "GOTO " + argsMap.getOrDefault("gotoLabel","?");
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
    public List<Instruction> extend(int extentionLevel, VariableAndLabelMenger vlm) {
        List<Instruction> myInstructions = new ArrayList<>();

        switch (extentionLevel) {
            case 0:
                return List.of(this);
            default: {
                Label label = vlm.newLabel();
                Variable tempVar1 = vlm.newZVariable();
                Variable tempVar2 = vlm.newZVariable();

                Instruction instr1 = new NoOpInstruction(tempVar1, label, argsMap);
                Instruction instr2 = new IncreaseInstruction(tempVar2, label, argsMap);
                Instruction instr3 = new JumpNotZeroInstruction(tempVar2, getLabel(), argsMap);

                myInstructions.add(this);
                myInstructions.add(instr1);
                myInstructions.add(instr2);
                myInstructions.add(instr3);
                return myInstructions;

            }
        }
    }
}
