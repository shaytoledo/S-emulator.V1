package logic.argument;

import core.program.Function;
import core.program.VariableAndLabelMenger;
import logic.execution.ExecutionContext;
import logic.execution.FunctionExecutor;
import logic.instruction.Instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FunctionArgument implements Argument {

    private String name;
    private String userString;
    public List<Argument> arguments;
    private List<Function> functions;

    public FunctionArgument(String name, List<Argument> arguments, List<Function> funcs) {
        this.name = name;
        this.arguments = arguments;
        this.functions = funcs;

        for (Function f : funcs) {
            if (f.getName().equals(name)) {
                this.userString = f.getUserString();
                break;
            }
        }

    }



    @Override
    public long evaluate(ExecutionContext context) {
        // Evaluate all child arguments in the *current* context to numeric values
        List<Long> values = new ArrayList<>(arguments.size());
        for (Argument arg : arguments) {
            long v = arg.evaluate(context);
            values.add(v);
        }

        Map<String , Function> functionsByName = context.getFunctions();
        Function fn = functionsByName.get(name);

        // Create a new FunctionExecutor for the function and run it with the evaluated arguments x1 = arg1 ...
        FunctionExecutor currentExecutor = new FunctionExecutor(fn, functions);
        long result = currentExecutor.run(values);

        return result;
    }

    @Override
    public  List<Exception> validate(ExecutionContext context) {
        // for each funcction need to check if exists\

        return List.of();
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        for (Argument arg : arguments) {
            sb.append(",");
            sb.append(arg.toDisplayString());

        }
        return "(" + userString + sb + ")";
    }

    @Override
    public int getMaxLevel() {

        int base = 0;

        for (Function func : functions) {
            if (func.getName().equals(name)) {
                base = func.calculateMaxDegree(); // ensure its calculated
            }
        }

        int best = base + 1; // +1 for the function call itself
        for (Argument arg : arguments) {
            if( arg instanceof FunctionArgument ) {
                int sub = 1 + ((FunctionArgument) arg).getMaxLevel(); // +1 for the argument function call itself
                if (sub > best) {
                    best = sub;
                }
            }
        }
        return best;
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
