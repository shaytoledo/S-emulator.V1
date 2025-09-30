package logic.instruction.synthetic;

import core.program.Function;
import core.program.VariableAndLabelMenger;
import javafx.util.Pair;
import logic.argument.Argument;
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

public class JumpEqualFunctionInstruction extends AbstractInstruction {

    Function function;
    FunctionArgument arguments;
    List<Function> allFunctions;
    Label jnzLabel; // label to jump if not equal

    String functionArguments;
    Variable variable;
    int cycles = 0;

    public JumpEqualFunctionInstruction(String name, String functionArguments, Variable var, List<Function> funcs, Label lineLabel, Label jnzLabel) {
        super(InstructionData.JUMP_EQUAL_FUNCTION, var, lineLabel);
        this.functionArguments = functionArguments;
        this.variable = var;
        List<Argument> arguments = new ArrayList<>(toArguments(functionArguments ,funcs));
        this.arguments = new FunctionArgument(name, arguments, funcs);
        this.allFunctions = funcs;
        this.jnzLabel = jnzLabel;


        for (Function f : funcs) {
            if (f.getName().equals(name)) {
                this.function = f;
                break;
            }
        }
    }
    public JumpEqualFunctionInstruction(String name, String functionArguments, Variable var, List<Function> funcs, Label jnzLabel) {
        super(InstructionData.JUMP_EQUAL_FUNCTION, var);
        List<Argument> arguments = new ArrayList<>(toArguments(functionArguments ,funcs));
        this.arguments = new FunctionArgument(name, arguments, funcs);
        this.allFunctions = funcs;
        this.jnzLabel = jnzLabel;

        for (Function f : funcs) {
            if (f.getName().equals(name)) {
                this.function = f;
                break;
            }
        }
    }

    @Override
    public Instruction clone() {
        if(getLabel() == null) {
            return new JumpEqualFunctionInstruction(function.getName(), functionArguments, variable, allFunctions, jnzLabel);
        } else {
            return new JumpEqualFunctionInstruction(function.getName(), functionArguments, variable, allFunctions, jnzLabel, getLabel());
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
        return "IF " + getVariable().getRepresentation() + "=" + arguments.toDisplayString() + " GOTO " + jnzLabel.getLabelRepresentation();
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
        if(getVariable() != null) {
            return List.of(getVariable());
        }
        return List.of();
    }

    @Override
    public List<Label> getAllLabels() {
        if (getLabel() != null) {
            List<Label> labels = new ArrayList<>();
            labels.add(getLabel());
            labels.add(jnzLabel);
            return labels;
        }
        return List.of(jnzLabel);
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

    @Override
    public Label execute(ExecutionContext context, VariableAndLabelMenger vlm) {
//        // evaluate arguments in arguments evaluate
//        // create new context
//        // execute function in that context
//        // get result and store in variable
//
//        Variable var = new VariableImpl(VariableType.RESULT, 1);
//        long oldY = context.getVariableValue(var);
//        arguments.evaluate(context, vlm);
//        long newY = context.getVariableValue(var);
//        context.updateVariable(getVariable(), newY);
//        context.updateVariable(var, oldY);
//        // update the variable with the result





        // Evaluate the quoted function in a pure way (no side effects on the outer context).
        // Evaluate the quoted function (child calls are pure now)
        long functionResult = arguments.evaluate(context, vlm, cycles);
        // Store the function result into this instruction's target variable (often 'y').
        if(functionResult == context.getVariableValue(getVariable())) {
            return jnzLabel;
        }

        return FixedLabel.EMPTY;
    }






    @Override
    public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {
        return List.of(this.clone());
    }


    private boolean isValidFunction(String name, List<Function> funcs) {
        for (Function func : funcs) {
            if (func.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
