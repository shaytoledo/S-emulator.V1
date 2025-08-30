package logic.instruction;

import logic.execution.ExecutionContext;
import logic.label.Label;
import logic.program.VariableAndLabelMenger;
import logic.variable.Variable;

import java.util.List;
import java.util.Map;

public interface Instruction {

    String getName();

    Label execute(ExecutionContext context);

    int cycles();

    Label getLabel();

    Variable getVariable();

    boolean isBasic();

    String toDisplayString();

    Map<String,String> args();

    int getMaxLevel();

    List<Instruction> extend(int extentionLevel, VariableAndLabelMenger vlm);

}
