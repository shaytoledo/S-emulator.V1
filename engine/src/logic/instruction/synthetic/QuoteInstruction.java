package logic.instruction.synthetic;

import core.program.Function;
import core.program.VariableAndLabelMenger;
import logic.argument.Argument;
import logic.argument.ConstantArgument;
import logic.argument.FunctionArgument;
import logic.argument.VariableArgument;
import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
import logic.label.Label;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;

import java.util.ArrayList;
import java.util.List;

public class QuoteInstruction extends AbstractInstruction {

    FunctionArgument function;
    List<Function> allFunctions;

    public QuoteInstruction(String name, String functionArguments,Variable var,  List<Function> funcs, Label lineLabel) {
        super(InstructionData.QUOTE, var, lineLabel);
        List<Argument> arguments = new ArrayList<>(toArguments(functionArguments ,funcs));
        function = new FunctionArgument(name, arguments, funcs);
        this.allFunctions = funcs;
    }

    public QuoteInstruction(String name, String functionArguments,Variable var,  List<Function> funcs) {
        super(InstructionData.QUOTE, var);
        List<Argument> arguments = new ArrayList<>(toArguments(functionArguments ,funcs));
        function = new FunctionArgument(name, arguments, funcs);
    }

    List<String> splitArguments(String input) {
        List<String> stringArgs = new ArrayList<>();

        StringBuilder current = new StringBuilder();
        int depth = 0;

        for (char c : input.toCharArray()) {
            if (c == '(') {
                depth++;
                current.append(c);
            } else if (c == ')') {
                depth--;
                current.append(c);
            } else if (c == ',' && depth == 0) {
                stringArgs.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            stringArgs.add(current.toString().trim());
        }

        return stringArgs;
    }

    List<Argument> toArguments(String input, List<Function> funcs) {

        List<String> stringArgs = new ArrayList<>(splitArguments(input));

        List<Argument> result = new ArrayList<>();
        for (String arg : stringArgs) {
            if (arg.startsWith("CONST")) {
                String num = arg.substring("CONST".length()).trim();
                if (num.isEmpty())
                    throw new IllegalArgumentException("CONST without value: " + arg);
                Argument constArg = new ConstantArgument(Long.parseLong(num));
                result.add(constArg);
            } else if(arg.startsWith("x")) {
                Argument varArg = new VariableArgument(new VariableImpl(VariableType.INPUT,Integer.parseInt(arg.substring("x".length()))));
                result.add(varArg);
            } else if(arg.startsWith("z")) {
                Argument varArg = new VariableArgument(new VariableImpl(VariableType.WORK,Integer.parseInt(arg.substring("z".length()))));
                result.add(varArg);
            } else if(arg.startsWith("y")) {
                Argument varArg = new VariableArgument(new VariableImpl(VariableType.RESULT,1));
                result.add(varArg);

            } else if (arg.startsWith("(") && arg.endsWith(")")) {
                String inner = arg.substring(1, arg.length() - 1).trim();


                int commaIndex = inner.indexOf(',');
                if (commaIndex < 0) { // error or that isnt function call its just const or variable
                    Argument curr;
                    if (inner.startsWith("CONST")) {
                        String num = inner.substring("CONST".length()).trim();
                        if (num.isEmpty())
                            throw new IllegalArgumentException("CONST without value: " + inner);
                        curr = new ConstantArgument(Long.parseLong(num));
                    } else if(inner.startsWith("x")) {
                        curr = new VariableArgument(new VariableImpl(VariableType.INPUT,Integer.parseInt(inner.substring("x".length()))));
                    } else if(inner.startsWith("z")) {
                        curr = new VariableArgument(new VariableImpl(VariableType.WORK,Integer.parseInt(inner.substring("z".length()))));
                    } else if(inner.startsWith("y")) {
                        curr = new VariableArgument(new VariableImpl(VariableType.RESULT,1));
                    } else {
                        throw new IllegalArgumentException("Must contain a variable or constant if not using a function " + inner);
                    }
                    result.add(curr);
                }
                else {
                    String targetFunctionName = inner.substring(0, commaIndex).trim();

                    // check if function exists
//                    if (!isValidFunction(targetFunctionName, funcs)) {
//                        throw new IllegalArgumentException("Function not defined: " + targetFunctionName);
//                    }

                    String functionArguments  = inner.substring(commaIndex + 1).trim();

                    FunctionArgument func = new FunctionArgument(targetFunctionName, toArguments(functionArguments, funcs), funcs);
                    result.add(func);
                }
            }
            else {
                throw new IllegalArgumentException("Unsupported argument format: " + arg);
            }

        }

        return result;
    }

    private boolean isValidFunction(String name, List<Function> funcs) {
        for (Function func : funcs) {
            if (func.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String toDisplayString() {
        return getVariable().getRepresentation() + " <- " + function.toDisplayString();
    }

    @Override
    public int getMaxLevel() {
        return function.getMaxLevel();
    }






    @Override
    public Label execute(ExecutionContext context) {
        return null;
    }

    @Override
    public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {
        return function.get;
    }

    @Override
    public List<String> getAllInfo() {
        return List.of();
    }

    @Override
    public List<Variable> getAllVariables() {
        return List.of();
    }

    @Override
    public List<Label> getAllLabels() {
        return List.of();
    }











//    @Override
//    public int getCycles() {
//        return cycles;
//    }

}
