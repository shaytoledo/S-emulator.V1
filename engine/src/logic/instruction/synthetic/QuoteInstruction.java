package logic.instruction.synthetic;

import core.program.Function;
import core.program.VariableAndLabelMenger;
import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
import logic.label.Label;
import logic.variable.Variable;

import java.util.List;
import java.util.Objects;

public class QuoteInstruction extends AbstractInstruction {

//public QuoteInstruction(String functionArguments, List<Function> funcs, List<Exception> errors, Label label) {
//    //this(new InstructionData("QUOTE", functionArguments, errors), null, label);
//}




    public QuoteInstruction(InstructionData instructionData, Variable variable) {
        super(instructionData, variable);
    }

    public QuoteInstruction(InstructionData instructionData, Variable variable, Label label) {
        super(instructionData, variable, label);
    }

    @Override
    public Label execute(ExecutionContext context) {
        return null;
    }

    @Override
    public String toDisplayString() {
        return "";
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
}
