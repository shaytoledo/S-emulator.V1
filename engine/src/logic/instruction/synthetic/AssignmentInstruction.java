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

public class AssignmentInstruction extends AbstractInstruction {

    Variable assignedVariable;

    public AssignmentInstruction(Variable target, Variable source, Map<String,String> argsMap) {
        super(InstructionData.ASSIGNMENT, target, argsMap);
        this.assignedVariable = source;
    }

    public AssignmentInstruction(Variable target, Variable source, Label label, Map<String,String> argsMap) {
        super(InstructionData.ASSIGNMENT, target, label, argsMap);
        this.assignedVariable = source;
    }


    @Override
    public Label execute(ExecutionContext context) {
        long assignedValue = context.getVariableValue(assignedVariable.getRepresentation());
        context.updateVariable(getVariable().getRepresentation(), assignedValue);
        return FixedLabel.EMPTY;
    }


    @Override
    public String toDisplayString() {
        //return getVariable().getRepresentation() + " <- " + argsMap.getOrDefault("assignedVariable","?");
        return getVariable().getRepresentation() + " <- " + assignedVariable.getRepresentation();

    }

    @Override
    public Map<String, String> args() {
        return argsMap;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    public List<Instruction> extend(int extentionLevel, VariableAndLabelMenger vlm) {
        List<Instruction> myInstructions = new ArrayList<>();

        switch (extentionLevel) {
            case 0:
                return List.of(this);
            case 1: {
                Label label1 = vlm.newLabel();
                Label label2 = vlm.newLabel();
                Label label3 = vlm.newLabel();
                Variable v = this.getVariable();
                Variable vTag = assignedVariable;
                Variable z1 = vlm.newZVariable();
                Instruction instr1 = new NoOpInstruction(v, getLabel(), argsMap);
                Instruction instr2 = new ZeroVariableInstruction(v, argsMap);
                Instruction instr3 = new JumpNotZeroInstruction(vTag,label1, argsMap);
                Instruction instr4 = new GoToInstruction(vTag,label3, argsMap);
                Instruction instr5 = new DecreaseInstruction(vTag,label1, argsMap);
                Instruction instr6 = new IncreaseInstruction(z1, argsMap);
                Instruction instr7 = new JumpNotZeroInstruction(vTag,label1, argsMap);
                Instruction instr8 = new DecreaseInstruction(z1,label2, argsMap);
                Instruction instr9 = new IncreaseInstruction(v, argsMap);
                Instruction instr10 = new IncreaseInstruction(vTag, argsMap);
                Instruction instr11 = new JumpNotZeroInstruction(z1,label2, argsMap);
                Instruction instr12 = new NoOpInstruction(v, label3, argsMap);
                myInstructions.add(this);
                myInstructions.add(instr1);
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
                Instruction instr1 = new NoOpInstruction(v, getLabel(), argsMap);
                Instruction instr2 = new ZeroVariableInstruction(v, argsMap);
                List<Instruction> zeroExtend = instr2.extend(1, vlm);

                Instruction instr3 = new JumpNotZeroInstruction(vTag,label1, argsMap);
                Instruction instr4 = new GoToInstruction(vTag,label3, argsMap);
                List<Instruction> gotoExtend = instr4.extend(1, vlm);

                Instruction instr5 = new DecreaseInstruction(vTag,label1, argsMap);
                Instruction instr6 = new IncreaseInstruction(z1, argsMap);
                Instruction instr7 = new JumpNotZeroInstruction(vTag,label1, argsMap);
                Instruction instr8 = new DecreaseInstruction(z1,label2, argsMap);
                Instruction instr9 = new IncreaseInstruction(v, argsMap);
                Instruction instr10 = new IncreaseInstruction(vTag, argsMap);
                Instruction instr11 = new JumpNotZeroInstruction(z1,label2, argsMap);
                Instruction instr12 = new NoOpInstruction(v, label3, argsMap);

                myInstructions.add(this);
                myInstructions.add(instr1);

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
