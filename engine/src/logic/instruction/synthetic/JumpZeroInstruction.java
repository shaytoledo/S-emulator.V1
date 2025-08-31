package logic.instruction.synthetic;

import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
import logic.instruction.basic.JumpNotZeroInstruction;
import logic.instruction.basic.NoOpInstruction;
import logic.label.FixedLabel;
import logic.label.Label;
import core.program.VariableAndLabelMenger;
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
        return "IF " + getVariable().getRepresentation() + " = 0 GOTO " + jnzLabel.getLabelRepresentation();
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
    public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {
        List<Instruction> myInstructions = new ArrayList<>();

        switch (extensionLevel) {
            case 0:
                return List.of(this);
            case 1: {
                Label label1 = vlm.newLabel();

                Instruction instr1 = new JumpNotZeroInstruction(getVariable(),label1,getLabel() ,argsMap);
                Instruction instr2 = new GoToInstruction(getVariable(), jnzLabel, argsMap);
                Instruction instr3 = new NoOpInstruction(getVariable(), label1, argsMap);

                myInstructions.add(instr1);
                myInstructions.add(instr2);
                myInstructions.add(instr3);
                return myInstructions;
            }
            default: {
                Label label1 = vlm.newLabel();

                Instruction instr1 = new JumpNotZeroInstruction(getVariable(),label1,getLabel() ,argsMap);
                Instruction instr2 = new GoToInstruction(getVariable(), jnzLabel, argsMap);
                List <Instruction> instr = instr2.extend(1, vlm);

                Instruction instr3 = new NoOpInstruction(getVariable(), label1, argsMap);

                myInstructions.add(instr1);
                myInstructions.addAll(instr);

                myInstructions.add(instr3);

                return myInstructions;
            }
        }
    }
}
