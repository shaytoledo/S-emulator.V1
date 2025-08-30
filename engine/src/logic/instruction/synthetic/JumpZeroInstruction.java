package logic.instruction.synthetic;

import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
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

public class JumpZeroInstruction extends AbstractInstruction {

    private final Label jnzLabel;

    public JumpZeroInstruction(Variable var, Label target, Map<String,String> argsMap) {
        super(InstructionData.JUMP_ZERO, var, argsMap);
        this.jnzLabel = target;
    }

    public JumpZeroInstruction(Variable var, Label target, Label lineLabel, Map<String,String> argsMap) {
        super(InstructionData.JUMP_ZERO, var, lineLabel, argsMap);
        this.jnzLabel = target;
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable().getRepresentation());

        if (variableValue == 0) {
            return jnzLabel;
        }
        return FixedLabel.EMPTY;

    }


    @Override
    public String toDisplayString() {
        return  "JZ " + getVariable().getRepresentation() + " -> " + argsMap.getOrDefault("JZLabel","?");
    }

    @Override
    public Map<String, String> args() {
        return argsMap;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public List<Instruction> extend(int extentionLevel, VariableAndLabelMenger vlm) {
        List<Instruction> myInstructions = new ArrayList<>();

        switch (extentionLevel) {
            case 0:
                return List.of(this);
            case 1: {
                Label label1 = vlm.newLabel();
                Label label2 = vlm.newLabel();

                Variable tempVar1 = vlm.newZVariable();

                Instruction instr1 = new NoOpInstruction(getVariable(), getLabel(), argsMap);
                Instruction instr2 = new JumpNotZeroInstruction(getVariable(), label1, argsMap);
                Instruction instr3 = new GoToInstruction(tempVar1, label2, argsMap);

                myInstructions.add(this);
                myInstructions.add(instr1);
                myInstructions.add(instr2);
                myInstructions.add(instr3);
                return myInstructions;
            }
            default: {
                Label label1 = vlm.newLabel();
                Label label2 = vlm.newLabel();

                Variable tempVar1 = vlm.newZVariable();

                Instruction instr1 = new NoOpInstruction(getVariable(), getLabel(), argsMap);
                Instruction instr2 = new JumpNotZeroInstruction(getVariable(), label1, argsMap);

                Instruction instr3 = new GoToInstruction(tempVar1, label2, argsMap);
                List <Instruction> instrs = instr3.extend(1, vlm);

                myInstructions.add(this);
                myInstructions.add(instr1);
                myInstructions.add(instr2);

                myInstructions.addAll(instrs);

                return myInstructions;
            }
        }
    }
}
