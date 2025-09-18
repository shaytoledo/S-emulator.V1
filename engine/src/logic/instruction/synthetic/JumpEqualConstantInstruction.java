package logic.instruction.synthetic;

import core.program.VariableAndLabelMenger;
import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
import logic.instruction.basic.DecreaseInstruction;
import logic.instruction.basic.JumpNotZeroInstruction;
import logic.instruction.basic.NoOpInstruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.ArrayList;
import java.util.List;

public class JumpEqualConstantInstruction extends AbstractInstruction {

    private final Label target;
    private final long constant;

    public JumpEqualConstantInstruction(Variable var, Label target, long constant) {
        super(InstructionData.JUMP_EQUAL_CONSTANT, var);
        this.target = target;
        this.constant = constant;
    }

    public JumpEqualConstantInstruction(Variable var, Label target, long constant, Label lineLabel) {
        super(InstructionData.JUMP_EQUAL_CONSTANT, var, lineLabel);
        this.target = target;
        this.constant = constant;
    }

    @Override
    public Label execute(ExecutionContext context) {
        if (context.getVariableValue(getVariable()) == constant) {
            return target;
        }
        return FixedLabel.EMPTY;
    }

    @Override
    public String toDisplayString() {
        return "JE " + getVariable().getRepresentation() + " == " + constant +" -> " + target.getLabelRepresentation();
    }

    @Override
    public int getMaxLevel() {
        return 3;
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
    public List<Variable> getAllVariables() {
        return List.of(getVariable());
    }

    @Override
    public List<Label> getAllLabels() {
        if (getLabel() == null) {
            return List.of(target);
        } else {
            return List.of(getLabel(), target);
        }
    }

    @Override
    public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {
        List<Instruction> myInstructions = new ArrayList<>();

        switch (extensionLevel) {
            case 0:
                return List.of(this);
            case 1: {
                Variable z1 =  vlm.newZVariable();
                Variable v = getVariable();
                long k = constant;
                Label target = this.target;
                Label label1 = vlm.newLabel();


                Instruction instr1 = new AssignmentInstruction(z1, v,getLabel());
                myInstructions.add(instr1);

                for(int i = 0; i < k; i++) {
                    Instruction jz = new JumpZeroInstruction(z1, label1);
                    Instruction dec = new DecreaseInstruction(z1);
                    myInstructions.add(jz);
                    myInstructions.add(dec);
                }

                Instruction instr4 = new JumpNotZeroInstruction(z1, label1);
                Instruction instr5 = new GoToInstruction(v, target);
                Instruction instr6 = new NoOpInstruction(v, label1);
                myInstructions.add(instr4);
                myInstructions.add(instr5);
                myInstructions.add(instr6);

                return myInstructions;

            }
            case 2: {

                Variable z1 =  vlm.newZVariable();
                Variable v = getVariable();
                long k = constant;
                Label target = this.target;
                Label label1 = vlm.newLabel();


                Instruction instr1 = new AssignmentInstruction(z1, v,getLabel());
                List<Instruction> assExte1 = instr1.extend(1, vlm);
                myInstructions.addAll(assExte1);

                 for(int i = 0; i < k; i++) {
                     Instruction instr2 = new JumpZeroInstruction(z1, target);
                     List<Instruction> re = instr2.extend(1, vlm);
                     myInstructions.addAll(re);

                     Instruction dec = new DecreaseInstruction(z1);
                     myInstructions.add(dec);
                }


                Instruction instr4 = new JumpNotZeroInstruction(z1, label1);
                Instruction instr5 = new GoToInstruction(v, target);
                List<Instruction> gotoExte1 = instr5.extend(1, vlm);

                Instruction instr6 = new NoOpInstruction(v, label1);
                myInstructions.add(instr4);
                myInstructions.addAll(gotoExte1);
                myInstructions.add(instr6);

                return myInstructions;
            }
            default:  {
                Variable z1 =  vlm.newZVariable();
                Variable v = getVariable();
                long k = constant;
                Label target = this.target;
                Label label1 = vlm.newLabel();


                Instruction instr1 = new AssignmentInstruction(z1, v,getLabel());
                List<Instruction> assExte1 = instr1.extend(2, vlm);
                myInstructions.addAll(assExte1);

                for(int i = 0; i < k; i++) {
                    Instruction instr2 = new JumpZeroInstruction(z1, target);
                    List<Instruction> re = instr2.extend(2, vlm);
                    myInstructions.addAll(re);

                    Instruction dec = new DecreaseInstruction(z1);
                    myInstructions.add(dec);
                }

                Instruction instr4 = new JumpNotZeroInstruction(z1, label1);
                Instruction instr5 = new GoToInstruction(v, target);
                List<Instruction> gotoExte1 = instr5.extend(1, vlm);

                Instruction instr6 = new NoOpInstruction(v, label1);
                myInstructions.add(instr4);
                myInstructions.addAll(gotoExte1);
                myInstructions.add(instr6);
                return myInstructions;
            }

        }
    }
}
