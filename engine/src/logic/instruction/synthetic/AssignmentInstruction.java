package logic.instruction.synthetic;

import core.program.VariableAndLabelMenger;
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
import logic.variable.Variable;

import java.util.ArrayList;
import java.util.List;

public class AssignmentInstruction extends AbstractInstruction {

    Variable assignedVariable;

    public AssignmentInstruction(Variable target, Variable source) {
        super(InstructionData.ASSIGNMENT, target);
        this.assignedVariable = source;
    }

    public AssignmentInstruction(Variable target, Variable source, Label label) {
        super(InstructionData.ASSIGNMENT, target, label);
        this.assignedVariable = source;
    }

    public Variable getAssignedVariable() {
        return assignedVariable;
    }
    @Override
    public Label execute(ExecutionContext context) {
        long assignedValue = context.getVariableValue(assignedVariable);
        context.updateVariable(getVariable(), assignedValue);
        return FixedLabel.EMPTY;
    }

    @Override
    public String toDisplayString() {
        return getVariable().getRepresentation() + " <- " + assignedVariable.getRepresentation();
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    public List<String> getAllInfo() {
        List<String> list = new ArrayList<>();
        if (getLabel() != null) {
            list.add(getLabel().getLabelRepresentation());
        }
        if (getVariable() != null) {
            list.add(getVariable().getRepresentation());
        }
        list.add(assignedVariable.getRepresentation());
        return list;
    }

    @Override
    public List<Variable> getAllVariables() {
        return List.of(getVariable(), assignedVariable);
    }

    @Override
    public List<Label> getAllLabels() {
        if (getLabel() == null) {
            return List.of();
        } else {
            return List.of(getLabel());
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
                Variable v = this.getVariable();
                Variable vTag = assignedVariable;
                Variable z1 = vlm.newZVariable();
                Instruction instr2 = new ZeroVariableInstruction(v,getLabel());
                Instruction instr3 = new JumpNotZeroInstruction(vTag,label1);
                Instruction instr4 = new GoToInstruction(vTag,label3);

                Instruction instr5 = new DecreaseInstruction(vTag,label1);
                Instruction instr6 = new IncreaseInstruction(z1);
                Instruction instr7 = new JumpNotZeroInstruction(vTag,label1);
                Instruction instr8 = new DecreaseInstruction(z1,label2);
                Instruction instr9 = new IncreaseInstruction(v);
                Instruction instr10 = new IncreaseInstruction(vTag);
                Instruction instr11 = new JumpNotZeroInstruction(z1,label2);
                Instruction instr12 = new NoOpInstruction(v, label3);

                myInstructions.add(instr2);
                myInstructions.add(instr3);
                myInstructions.add(instr4);
                myInstructions.add(instr5);
                myInstructions.add(instr6);
                myInstructions.add(instr7);
                myInstructions.add(instr8);
                myInstructions.add(instr9);
                myInstructions.add(instr10);
                myInstructions.add(instr11);
                myInstructions.add(instr12);
                return myInstructions;
            }
            default: {
                Label label1 = vlm.newLabel();
                Label label2 = vlm.newLabel();
                Label label3 = vlm.newLabel();
                Variable v = this.getVariable();
                Variable vTag = assignedVariable;
                Variable z1 = vlm.newZVariable();
                //Instruction instr1 = new NoOpInstruction(v, getLabel());
                Instruction instr2 = new ZeroVariableInstruction(v,getLabel());
                List<Instruction> zeroExtend = instr2.extend(1, vlm);

                Instruction instr3 = new JumpNotZeroInstruction(vTag,label1);
                Instruction instr4 = new GoToInstruction(vTag,label3);
                List<Instruction> gotoExtend = instr4.extend(1, vlm);

                Instruction instr5 = new DecreaseInstruction(vTag,label1);
                Instruction instr6 = new IncreaseInstruction(z1);
                Instruction instr7 = new JumpNotZeroInstruction(vTag,label1);
                Instruction instr8 = new DecreaseInstruction(z1,label2);
                Instruction instr9 = new IncreaseInstruction(v);
                Instruction instr10 = new IncreaseInstruction(vTag);
                Instruction instr11 = new JumpNotZeroInstruction(z1,label2);
                Instruction instr12 = new NoOpInstruction(v, label3);

                myInstructions.addAll(zeroExtend);

                myInstructions.add(instr3);

                myInstructions.addAll(gotoExtend);

                myInstructions.add(instr5);
                myInstructions.add(instr6);
                myInstructions.add(instr7);
                myInstructions.add(instr8);
                myInstructions.add(instr9);
                myInstructions.add(instr10);
                myInstructions.add(instr11);
                myInstructions.add(instr12);
                return myInstructions;
            }
        }
    }
}
