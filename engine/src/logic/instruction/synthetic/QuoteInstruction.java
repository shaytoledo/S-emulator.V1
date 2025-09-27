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
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

public class QuoteInstruction extends AbstractInstruction {

    Function function;
    FunctionArgument arguments;
    List<Function> allFunctions;

    public QuoteInstruction(String name, String functionArguments,Variable var,  List<Function> funcs, Label lineLabel) {
        super(InstructionData.QUOTE, var, lineLabel);
        List<Argument> arguments = new ArrayList<>(toArguments(functionArguments ,funcs));
        this.arguments = new FunctionArgument(name, arguments, funcs);
        this.allFunctions = funcs;


        for (Function f : funcs) {
            if (f.getName().equals(name)) {
                this.function = f;
                break;
            }
        }
    }
    public QuoteInstruction(String name, String functionArguments,Variable var,  List<Function> funcs) {
        super(InstructionData.QUOTE, var);
        List<Argument> arguments = new ArrayList<>(toArguments(functionArguments ,funcs));
        this.arguments = new FunctionArgument(name, arguments, funcs);
        this.allFunctions = funcs;

        for (Function f : funcs) {
            if (f.getName().equals(name)) {
                this.function = f;
                break;
            }
        }
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

        if(input == null) {
            return List.of();
        }

        List<String> stringArgs = new ArrayList<>(splitArguments(input));

        List<Argument> result = new ArrayList<>();
        for (String arg : stringArgs) {
//            if (arg.toUpperCase().startsWith("CONST")) {
//                String num = arg.substring("CONST".length()).trim();
//                if (num.isEmpty())
//                    throw new IllegalArgumentException("CONST without value: " + arg);
//                Argument constArg = new ConstantArgument(Long.parseLong(num));
//                result.add(constArg);
//            } else if(arg.startsWith("x")) {
            if(arg.startsWith("x")) {
                Argument varArg = new VariableArgument(new VariableImpl(VariableType.INPUT,Integer.parseInt(arg.substring("x".length()))));
                result.add(varArg);
            } else if(arg.startsWith("z")) {
                Argument varArg = new VariableArgument(new VariableImpl(VariableType.WORK,Integer.parseInt(arg.substring("z".length()))));
                result.add(varArg);
            } else if(arg.startsWith("y")) {
                Argument varArg = new VariableArgument(new VariableImpl(VariableType.RESULT,1));
                result.add(varArg);

            } else if (arg.startsWith("(") && arg.endsWith(")")) {
                String inner = arg.substring(1, arg.length() - 1).trim(); // only the inside without ()

                int commaIndex = inner.indexOf(',');
                Argument curr;

                if (commaIndex > 0) { // there are arguments for the function
                    String targetFunctionName = inner.substring(0, commaIndex).trim();
                    String functionArguments = inner.substring(commaIndex + 1).trim();
                    curr = new FunctionArgument(targetFunctionName, toArguments(functionArguments, funcs), funcs);
                }
//                        // check if function exists
//                    if (!isValidFunction(targetFunctionName, funcs)) {
//                        throw new IllegalArgumentException("Function not defined: " + targetFunctionName);
//                    }

                else { // only function name without arguments
                    String targetFunctionName = inner.trim();
                    curr = new FunctionArgument(targetFunctionName, List.of(), funcs);
//                    // check if function exists
//                    if (!isValidFunction(targetFunctionName, funcs)) {
//                        throw new IllegalArgumentException("Function not defined: " + targetFunctionName);
//                    }
                }
                result.add(curr);
            }
            else {
                throw new IllegalArgumentException("Unsupported argument format: " + arg);
            }

        }

        return result;
    }

    @Override
    public int getMaxLevel() {
        return arguments.getMaxLevel();
    }

    @Override
    public String toDisplayString() {
        return getVariable().getRepresentation() + " <- " + arguments.toDisplayString();
    }

    @Override
    public List<String> getAllInfo() {
        List<String> all = getAllVariables().stream()
                .map(Variable::getRepresentation)
                .collect(toCollection(java.util.LinkedHashSet::new)) // unique + order
                .stream()
                .collect(toList());


        all.addAll(getAllLabels().stream()
                .map(Label::getLabelRepresentation)
                .collect(toCollection(java.util.LinkedHashSet::new)) // unique + order
                .stream()
                .collect(toList()));
        return all;
    }

    @Override
    public List<Variable> getAllVariables() {
        List<Variable> all = new ArrayList<>();
        return all;
    }

    @Override
    public List<Label> getAllLabels() {
        List<Label> all = new ArrayList<>();
        return all;
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
    public Label execute(ExecutionContext context, VariableAndLabelMenger vlm) {
        // evaluate arguments in arguments evaluate
        // create new context
        // execute function in that context
        // get result and store in variable



        arguments.evaluate(context, vlm);
        // update the variable with the result
        return FixedLabel.EMPTY;
    }






    @Override
    public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {

        // replace the variable with a work variable
        // replace labels with new labels

        // extend the function for the neccesary level


        return List.of(this);
                //arguments.getExtendedInstructions(extensionLevel, vlm);
    }



//    @Override
//    public int getCycles() {
//        return cycles;
//    }

}
