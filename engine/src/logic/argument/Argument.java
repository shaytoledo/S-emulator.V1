package logic.argument;

import core.program.VariableAndLabelMenger;
import javafx.util.Pair;
import logic.execution.ExecutionContext;
import logic.instruction.Instruction;
import logic.label.Label;

import java.util.List;

public interface Argument {

    // Run the argument and return its value
    long evaluate(ExecutionContext context, VariableAndLabelMenger vlm, int cycleCount);

    // Display-friendly string for debugging/logs
    String toDisplayString();

    // Return the maximum depth level of this argument
    int getMaxLevel();

    // Expand the argument into instructions if needed
    //Pair<List<Instruction>, Label> extend(int extensionLevel, VariableAndLabelMenger vlm);
    List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm);

    // Get all the labels and variables in this section
    List<String> getAllInfo();

}
