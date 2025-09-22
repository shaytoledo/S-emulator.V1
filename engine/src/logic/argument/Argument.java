package logic.argument;

import core.program.VariableAndLabelMenger;
import logic.execution.ExecutionContext;
import logic.instruction.Instruction;

import java.util.List;

public interface Argument {

    // Run the argument and return its value
    long evaluate(ExecutionContext context);

    // Validate if the argument is legal in the current context
    List<Exception> validate(ExecutionContext context);

    // Display-friendly string for debugging/logs
    String toDisplayString();

    // Return the maximum depth level of this argument
    int getMaxLevel();

    // Expand the argument into instructions if needed
    List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm);

    // Get all the labels and variables in this section
    List<String> getAllInfo();

    List<Instruction> getExtendedInstructions(int extensionLevel, VariableAndLabelMenger vlm);
}
