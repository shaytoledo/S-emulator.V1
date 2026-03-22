package logic.argument;

import core.program.Function;
import core.program.VariableAndLabelMenger;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.execution.FunctionExecutor;
import logic.instruction.Instruction;
import logic.instruction.synthetic.AssignmentInstruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.label.LabelImpl;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;

import java.util.*;

public class FunctionArgument implements Argument {

    // The name of the function to call
    private String name;
    // The function (cloned in constructor to protect the original)
    private Function function;
    // The user string of the function (for display purposes)
    private String userString;
    // The list of arguments to pass to the function
    public List<Argument> arguments;
    // function instructions (original, not modified)
    public List<Instruction> instructions;

    int cycles = 0;
    private Label exitLabel;
    private Variable newResultVar;

    private final List<Function> functions;

    public String getName() {
        return name;
    }
    public String getUserString() {
        return userString;
    }
    public List<Function> getFunctions() {
        return functions;
    }
    public String getArgs() {
        StringBuilder sb = new StringBuilder();
        for (Argument arg : arguments) {
            sb.append(arg.toDisplayString());
            sb.append(",");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // remove last comma
        }
        return sb.toString();
    }

    public FunctionArgument(String name, List<Argument> arguments, List<Function> funcs) {
        this.name = name;
        this.arguments = arguments;
        this.functions = funcs;

        for (Function f : funcs) {
            if (f.getName().equals(name)) {
                this.instructions = new ArrayList<>(f.getInstructions());
                this.userString = f.getUserString();
                this.function = f.clone(); // always clone to protect original
                break;
            }
        }
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
                base = func.calculateMaxDegree();
            }
        }

        int best = base + 1; // +1 for the function call itself
        for (Argument arg : arguments) {
            if (arg instanceof FunctionArgument) {
                int sub = 1 + ((FunctionArgument) arg).getMaxLevel();
                if (sub > best) {
                    best = sub;
                }
            }
        }
        return best;
    }

    @Override
    public long evaluate(ExecutionContext context, VariableAndLabelMenger vlm, int cycles) {
        // Evaluate all child arguments in the current context to numeric values
        List<Long> values = new ArrayList<>(arguments.size());
        for (Argument arg : arguments) {
            ExecutionContextImpl argContext = new ExecutionContextImpl(context);
            long v = arg.evaluate(argContext, vlm, cycles);
            values.add(v);
        }

        // Create a clean execution context for this function evaluation
        ExecutionContextImpl functionContext = new ExecutionContextImpl(values, functions);

        // Execute the function with the evaluated arguments
        FunctionExecutor currentExecutor = new FunctionExecutor(function, functions, functionContext);
        return currentExecutor.run(values, cycles);
    }

    public List<Instruction> cloneBody() {
        List<Instruction> copy = new ArrayList<>();
        for (Instruction i : function.getInstructions()) {
            copy.add(i.clone());
        }
        return copy;
    }

    public static List<Variable> collectInputsInOrder(List<Instruction> body) {
        TreeMap<Integer, Variable> byIdx = new TreeMap<>();
        for (Instruction ins : body) {
            List<Variable> vars = ins.getAllVariables();
            if (vars == null) continue;
            for (Variable v : vars) {
                if (v != null && v.getType() == VariableType.INPUT) {
                    byIdx.putIfAbsent(v.getIndex(), v);
                }
            }
        }
        return new ArrayList<>(byIdx.values());
    }

    public static Variable findResultVariable(List<Instruction> body) {
        Variable best = null;
        for (Instruction ins : body) {
            List<Variable> vars = ins.getAllVariables();
            if (vars == null) continue;
            for (Variable v : vars) {
                if (v != null && v.getType() == VariableType.RESULT) {
                    if (best == null || v.getIndex() < best.getIndex()) {
                        best = v;
                    }
                }
            }
        }
        return best;
    }

    @Override
    public List<String> getAllInfo() {
        return List.of();
    }

    @Override
    public List<Variable> getAllVariables() {
        List<Variable> vars = new ArrayList<>();
        for (Argument arg : arguments) {
            vars.addAll(arg.getAllVariables());
        }
        return vars;
    }

    // Expand this function-argument into instructions.
    // Works on a fresh clone of the function body — never mutates the original.
    @Override
    public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {
        if (extensionLevel <= 0) {
            return List.of();
        }

        // Work on a fresh clone of the function body — never mutate the original.
        Function cloned = function.clone();
        List<Instruction> body = cloned.getInstructions();

        List<Instruction> result = new ArrayList<>();

        // (1+2) Assign arguments to fresh work variables and remap inputs in the cloned body.
        List<Variable> newInputs = new ArrayList<>();
        for (int i = 0; i < arguments.size(); i++) {
            Variable newVar = vlm.newZVariable();
            newInputs.add(newVar);
            result.add(new AssignmentInstruction(newVar, new VariableImpl(VariableType.INPUT, i + 1)));
        }
        for (int i = 0; i < arguments.size(); i++) {
            for (Instruction inst : body) {
                inst.replace(new VariableImpl(VariableType.INPUT, i + 1), newInputs.get(i));
            }
        }

        // (3) Replace work variables with fresh ones.
        Set<Variable> workVars = new HashSet<>();
        for (Instruction inst : body) {
            inst.getAllVariables().stream()
                    .filter(v -> v.getType() == VariableType.WORK)
                    .forEach(workVars::add);
        }
        for (Variable oldVar : workVars) {
            Variable newVar = vlm.newZVariable();
            for (Instruction inst : body) {
                inst.replace(oldVar, newVar);
            }
        }

        // (4) Replace labels with fresh ones.
        Set<Label> labelsInFunction = new HashSet<>();
        for (Instruction inst : body) {
            inst.getAllLabels().stream()
                    .filter(l -> l != null && !l.equals(FixedLabel.EMPTY))
                    .forEach(labelsInFunction::add);
        }
        for (Label oldLabel : labelsInFunction) {
            Label newLabel = vlm.newLabel();
            if (oldLabel.equals(new LabelImpl(FixedLabel.EXIT.getLabelRepresentation()))) {
                exitLabel = newLabel;
            }
            for (Instruction inst : body) {
                inst.replace(oldLabel, newLabel);
            }
        }

        // (5) Replace result variable with a fresh work variable.
        newResultVar = vlm.newZVariable();
        for (Instruction inst : body) {
            inst.replace(new VariableImpl(VariableType.RESULT, 1), newResultVar);
        }

        result.addAll(body);

        // Expand further if requested.
        int remaining = extensionLevel - 1;
        if (remaining > 0) {
            List<Instruction> extended = new ArrayList<>();
            for (Instruction inst : result) {
                extended.addAll(inst.extend(remaining, vlm));
            }
            return extended;
        }

        return result;
    }
}
