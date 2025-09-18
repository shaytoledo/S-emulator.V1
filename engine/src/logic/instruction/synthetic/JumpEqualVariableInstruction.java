package logic.instruction.synthetic;

import core.program.VariableAndLabelMenger;
import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
import logic.instruction.basic.DecreaseInstruction;
import logic.instruction.basic.NoOpInstruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.ArrayList;
import java.util.List;

public class JumpEqualVariableInstruction extends AbstractInstruction {

    Label jnzLabel;
    Variable other;

    public JumpEqualVariableInstruction(Variable var, Label target, Variable other) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, var);
        this.jnzLabel = target;
        this.other = other;
    }

    public JumpEqualVariableInstruction(Variable var, Label target, Variable other, Label lineLabel) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, var, lineLabel);
        this.jnzLabel = target;
        this.other = other;
    }

    public Variable getOther() {
        return other;
    }

    @Override
    public Label execute(ExecutionContext context) {
        if (context.getVariableValue(getVariable()) == context.getVariableValue(other)) {
            return jnzLabel;
        }
        return FixedLabel.EMPTY;
    }

    @Override
    public String toDisplayString() {
        return "JE " + getVariable().getRepresentation() + " == " + other.getRepresentation() + " -> " + jnzLabel.getLabelRepresentation();
    }

    @Override
    public int getMaxLevel() {
        return 3;
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
        list.add(other.getRepresentation());
        return list;
    }

    @Override
    public List<Variable> getAllVariables() {
        return List.of(getVariable(), other);
    }

    @Override
    public List<Label> getAllLabels() {
        if (getLabel() == null) {
            return List.of(jnzLabel);
        } else {
            return List.of(getLabel(), jnzLabel);
        }
    }

    @Override
    public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {
        List<Instruction> myInstructions = new ArrayList<>();

        switch (extensionLevel) {
            case 0:
                return List.of(this);
            case 1: {
                Label label1 = vlm.newLabel();
                Label label2 = vlm.newLabel();
                Label label3 = vlm.newLabel();
                Variable z1 = vlm.newZVariable();
                Variable z2 = vlm.newZVariable();

                Label jnzLabel = this.jnzLabel;
                Variable vTag = this.other;
                Variable v = this.getVariable();


                Instruction instr2 = new AssignmentInstruction(z1, v,getLabel());
                Instruction instr3 = new AssignmentInstruction(z2, vTag);
                Instruction instr4 = new JumpZeroInstruction(z1, label3, label2);
                Instruction instr5 = new JumpZeroInstruction(z2, label3);
                Instruction instr6 = new DecreaseInstruction(z1);
                Instruction instr7 = new DecreaseInstruction(z2);
                Instruction instr8 = new GoToInstruction(z1, jnzLabel);
                Instruction instr9 = new JumpZeroInstruction(z2, label1);
                Instruction instr10 = new NoOpInstruction(v);


                myInstructions.add(instr2);
                myInstructions.add(instr3);
                myInstructions.add(instr4);
                myInstructions.add(instr5);
                myInstructions.add(instr6);
                myInstructions.add(instr7);
                myInstructions.add(instr8);
                myInstructions.add(instr9);
                myInstructions.add(instr10);

                return myInstructions;
            }
            case 2: {
                Label label1 = vlm.newLabel();
                Label label2 = vlm.newLabel();
                Label label3 = vlm.newLabel();
                Variable z1 = vlm.newZVariable();
                Variable z2 = vlm.newZVariable();

                Label jnzLabel = this.jnzLabel;
                Variable vTag = this.other;
                Variable v = this.getVariable();


                Instruction instr2 = new AssignmentInstruction(z1, v,getLabel());
                List<Instruction> assignExt1 = instr2.extend(1, vlm);

                Instruction instr3 = new AssignmentInstruction(z2, vTag);
                List<Instruction> assignExt2 = instr3.extend(1, vlm);

                Instruction instr4 = new JumpZeroInstruction(z1, label3, label2);
                List<Instruction> jumpZerExt1 = instr4.extend(1, vlm);

                Instruction instr5 = new JumpZeroInstruction(z2, label3);
                List<Instruction> jumpZerExt2 = instr5.extend(1, vlm);

                Instruction instr6 = new DecreaseInstruction(z1);
                Instruction instr7 = new DecreaseInstruction(z2);
                Instruction instr8 = new GoToInstruction(z1, jnzLabel);
                List<Instruction> gotoExt1 = instr8.extend(1, vlm);

                Instruction instr9 = new JumpZeroInstruction(z2, label1);
                List<Instruction> jumpZerExt3 = instr9.extend(1, vlm);

                Instruction instr10 = new NoOpInstruction(v);


                myInstructions.addAll(assignExt1);
                myInstructions.addAll(assignExt2);
                myInstructions.addAll(jumpZerExt1);
                myInstructions.addAll(jumpZerExt2);
                myInstructions.add(instr6);
                myInstructions.add(instr7);
                myInstructions.addAll(gotoExt1);
                myInstructions.addAll(jumpZerExt3);
                myInstructions.add(instr10);

                return myInstructions;
            }
            default: {
                Label label1 = vlm.newLabel();
                Label label2 = vlm.newLabel();
                Label label3 = vlm.newLabel();
                Variable z1 = vlm.newZVariable();
                Variable z2 = vlm.newZVariable();

                Label jnzLabel = this.jnzLabel;
                Variable vTag = this.other;
                Variable v = this.getVariable();


                Instruction instr2 = new AssignmentInstruction(z1, v,getLabel());
                List<Instruction> assignExt1 = instr2.extend(2, vlm);

                Instruction instr3 = new AssignmentInstruction(z2, vTag);
                List<Instruction> assignExt2 = instr3.extend(2, vlm);

                Instruction instr4 = new JumpZeroInstruction(z1, label3, label2);
                List<Instruction> jumpZerExt1 = instr4.extend(2, vlm);

                Instruction instr5 = new JumpZeroInstruction(z2, label3);
                List<Instruction> jumpZerExt2 = instr5.extend(2, vlm);

                Instruction instr6 = new DecreaseInstruction(z1);
                Instruction instr7 = new DecreaseInstruction(z2);
                Instruction instr8 = new GoToInstruction(z1, jnzLabel);
                List<Instruction> gotoExt1 = instr8.extend(1, vlm);

                Instruction instr9 = new JumpZeroInstruction(z2, label1);
                List<Instruction> jumpZerExt3 = instr9.extend(2, vlm);

                Instruction instr10 = new NoOpInstruction(v);

                myInstructions.addAll(assignExt1);
                myInstructions.addAll(assignExt2);
                myInstructions.addAll(jumpZerExt1);
                myInstructions.addAll(jumpZerExt2);
                myInstructions.add(instr6);
                myInstructions.add(instr7);
                myInstructions.addAll(gotoExt1);
                myInstructions.addAll(jumpZerExt3);
                myInstructions.add(instr10);

                return myInstructions;
            }
        }
    }
}
