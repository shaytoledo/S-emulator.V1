package logic.instruction;

import core.program.VariableAndLabelMenger;
import logic.execution.ExecutionContext;
import logic.label.Label;
import logic.variable.Variable;

import java.util.List;

public interface Instruction {

    String getName();
    Label execute(ExecutionContext context);
    int cycles();
    Label getLabel();
    Variable getVariable();
    boolean isBasic();
    String toDisplayString();
    int getMaxLevel();
    List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm);
    List<String> getAllInfo();

}
