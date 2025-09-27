package logic.argument;

import core.program.VariableAndLabelMenger;
import logic.execution.ExecutionContext;
import logic.instruction.Instruction;

import java.util.List;

public class ConstantArgument implements Argument {
    long me;

    public ConstantArgument(long me) {
        this.me = me;
    }

    @Override
    public long evaluate(ExecutionContext context, VariableAndLabelMenger vlm) {
        return me;
    }

    @Override
    public  List<Exception> validate(ExecutionContext context) {
        return List.of();
    }

    @Override
    public String toDisplayString() {
        return String.valueOf(me);
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
        return List.of();
    }

    @Override
    public List<Instruction> getExtendedInstructions(int extensionLevel, VariableAndLabelMenger vlm) {
        return List.of();
    }

}
