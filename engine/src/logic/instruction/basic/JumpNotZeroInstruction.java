package logic.instruction.basic;

import core.program.VariableAndLabelMenger;
import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;

import java.util.ArrayList;
import java.util.List;

public class JumpNotZeroInstruction extends AbstractInstruction {

    private Label jnzLabel;

    public JumpNotZeroInstruction(Variable var, Label target, Label lineLabel) {
        super(InstructionData.JUMP_NOT_ZERO, var, lineLabel);
        this.jnzLabel = target;
        basic = true;
    }

    public JumpNotZeroInstruction(Variable var, Label target) {
        super(InstructionData.JUMP_NOT_ZERO, var);
        this.jnzLabel = target;
        basic = true;
    }

    @Override
    public Instruction clone() {
        if(getLabel() == null)
            return new JumpNotZeroInstruction(getVariable(), jnzLabel);
        else {
            return new JumpNotZeroInstruction(getVariable(), jnzLabel, getLabel());
        }
    }

    @Override
    public Label execute(ExecutionContext context, VariableAndLabelMenger vlm) {
        long variableValue = context.getVariableValue(getVariable());

        if (variableValue != 0) {
            return jnzLabel;
        }
        return FixedLabel.EMPTY;

    }

    @Override
    public String toDisplayString() {
        return "IF " + getVariable().getRepresentation() + "!=0 GOTO " + jnzLabel.getLabelRepresentation();
    }
    
    @Override
    public int getMaxLevel() {
        return 0;
    }

    @Override
    public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {
        return List.of(this.clone());

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
    public List<Variable> getAllVariables() {
        return List.of(getVariable());
    }

    @Override
    public List<Label> getAllLabels() {
        if (getLabel() == null ) {
            return List.of(jnzLabel);
        } else {
            return List.of(getLabel(), jnzLabel);
        }
    }

    @Override
    public void replace(Variable oldVar, Variable newVar) {
        if(getVariable().equals(oldVar)) {
            setVariable(newVar);
        }
    }

    @Override
    public void replace(Label oldLabel, Label newLabel) {
        if(getLabel().equals(oldLabel)) {
            setLabel(newLabel);
        }
        if (jnzLabel.equals(oldLabel)) {
            jnzLabel = newLabel;
        }
    }
}
