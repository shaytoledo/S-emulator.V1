package logic.instruction.synthetic;

import core.program.VariableAndLabelMenger;
import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
import logic.instruction.basic.JumpNotZeroInstruction;
import logic.instruction.basic.NoOpInstruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.ArrayList;
import java.util.List;

public class JumpZeroInstruction extends AbstractInstruction {

    private final Label jnzLabel;

    public JumpZeroInstruction(Variable var, Label target) {
        super(InstructionData.JUMP_ZERO, var);
        this.jnzLabel = target;
    }

    public JumpZeroInstruction(Variable var, Label target, Label lineLabel) {
        super(InstructionData.JUMP_ZERO, var, lineLabel);
        this.jnzLabel = target;
    }

    @Override
    public List<String> getAllInfo() {
        List<String> list = new ArrayList<>();
        if (getLabel() != null) {
            list.add(getLabel().getLabelRepresentation());
        }
        if (getVariable() != null) {
            list.add(getVariable().getRepresentation());
        }
        list.add(jnzLabel.getLabelRepresentation());
        return list;
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable());

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

                Instruction instr1 = new JumpNotZeroInstruction(getVariable(),label1,getLabel());
                Instruction instr2 = new GoToInstruction(getVariable(), jnzLabel);
                Instruction instr3 = new NoOpInstruction(getVariable(), label1);

                myInstructions.add(instr1);
                myInstructions.add(instr2);
                myInstructions.add(instr3);
                return myInstructions;
            }
            default: {
                Label label1 = vlm.newLabel();

                Instruction instr1 = new JumpNotZeroInstruction(getVariable(),label1,getLabel());
                Instruction instr2 = new GoToInstruction(getVariable(), jnzLabel);
                List <Instruction> instr = instr2.extend(1, vlm);

                Instruction instr3 = new NoOpInstruction(getVariable(), label1);

                myInstructions.add(instr1);
                myInstructions.addAll(instr);

                myInstructions.add(instr3);

                return myInstructions;
            }
        }
    }
}
