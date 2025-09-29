package logic.argument;

import core.program.VariableAndLabelMenger;
import javafx.util.Pair;
import logic.execution.ExecutionContext;
import logic.instruction.Instruction;
import logic.label.Label;
import logic.variable.Variable;

import java.util.List;

public class VariableArgument implements Argument {
    Variable me;

    public VariableArgument (Variable me) {
        this.me = me;
    }

    @Override
    public long evaluate(ExecutionContext context, VariableAndLabelMenger vlm, int cycleCount) {
        return context.getVariableValue(me);
    }

    @Override
    public String toDisplayString() {
        return me.getRepresentation();
    }

    @Override
    public int getMaxLevel() {
        return 0;
    }

    @Override
    public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {
        return List.of();
    }

    @Override
    public List<String> getAllInfo() {
        return List.of(me.getRepresentation());
    }

}
