package logic.instruction.synthetic;

import core.program.Function;
import core.program.VariableAndLabelMenger;
import logic.argument.Argument;
import logic.argument.FunctionArgument;
import logic.argument.VariableArgument;
import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.instruction.InstructionData;
import logic.instruction.basic.NoOpInstruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

public class QuoteInstruction extends AbstractInstruction {

    Function function;
    FunctionArgument arguments;
    List<Argument> argumentList;
    List<Function> allFunctions;

    String functionArguments;

    int cycles = 0;

    public QuoteInstruction(String name, String functionArguments, Variable var, List<Function> funcs, Label lineLabel) {
        super(InstructionData.QUOTE, var, lineLabel);
        this.functionArguments = functionArguments == null ? "" : functionArguments;
        this.argumentList = new ArrayList<>(toArguments(this.functionArguments, funcs));
        this.arguments = new FunctionArgument(name, this.argumentList, funcs);
        this.allFunctions = funcs;
        for (Function f : funcs) if (f.getName().equals(name)) { this.function = f; break; }
    }

    public QuoteInstruction(String name, String functionArguments, Variable var, List<Function> funcs) {
        super(InstructionData.QUOTE, var);
        this.functionArguments = functionArguments == null ? "" : functionArguments;
        this.argumentList = new ArrayList<>(toArguments(this.functionArguments, funcs));
        this.arguments = new FunctionArgument(name, this.argumentList, funcs);
        this.allFunctions = funcs;
        for (Function f : funcs) if (f.getName().equals(name)) { this.function = f; break; }
    }


    private static String toArgsString(List<Argument> args) {

        List<String> parts = new ArrayList<>();
        for (Argument a : args) {
            if (a instanceof VariableArgument va) {
                Variable v = va.getVariable();
                String prefix = switch (v.getType()) {
                    case INPUT -> "x";
                    case WORK -> "z";
                    case RESULT -> "y";
                    default -> throw new IllegalArgumentException("Unsupported variable type: " + v.getType());
                };
                parts.add(prefix + v.getIndex());
            } else if (a instanceof FunctionArgument fa) {
                parts.add("(" + fa.getName() + (fa.arguments.isEmpty() ? "" : "," + toArgsString(fa.arguments)) + ")");
            } else {
                throw new IllegalArgumentException("Unsupported argument kind: " + a.getClass());
            }
        }
        return String.join(",", parts);
    }

    @Override
    public Instruction clone() {
        String name = function.getName();
        String args = functionArguments;
        Variable target = getVariable();
        Label lbl = getLabel();
        return (lbl == null)
                ? new QuoteInstruction(name, args, target, allFunctions)
                : new QuoteInstruction(name, args, target, allFunctions, lbl);
    }

    private List<String> splitArguments(String input) {
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

    private List<Argument> toArguments(String input, List<Function> funcs) {

        if(input == null) {
            return List.of();
        }

        List<String> stringArgs = new ArrayList<>(splitArguments(input));

        List<Argument> result = new ArrayList<>();
        for (String arg : stringArgs) {
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

        for(Argument arg : argumentList) {
            List<Variable> vars = arg.getAllVariables();
            if(vars != null) {
                for(Variable v : vars) {
                    if(!all.contains(v)) {
                        all.add(v);
                    }
                }
            }
        }


        if(getVariable() != null) {
            all.add(getVariable());
        }
        return all;
    }

    @Override
    public List<Label> getAllLabels() {
        if (getLabel() != null) {
            return List.of(getLabel());
        }
        return List.of();
    }

    @Override
    public void replace(Variable oldVar, Variable newVar) {
        Variable curr = getVariable();
        if (curr != null && curr.equals(oldVar)) {
            setVariable(newVar);
        }
    }


    @Override
    public void replace(Label oldLabel, Label newLabel) {
        Label curr = getLabel();
        if (curr != null && curr.equals(oldLabel)) {
            setLabel(newLabel);
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
        context.updateVariable(getVariable(), functionResult);

        return FixedLabel.EMPTY;
    }








    @Override
    public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {
        if (extensionLevel <= 0) {
            return List.of(this.clone());
        }

        boolean openedScope = false;
        try {
            if (vlm != null) {
                vlm.beginLocalScope();
                openedScope = true;
            }

            // Base it's the max depth of the function body (only instructions)
            // -------- 0) Compute base, bodyBudget, argBudget --------
            int base = function.getInstructions().stream()
                    .mapToInt(Instruction::getMaxLevel)
                    .max().orElse(0);


            // Base + 1 for the instruction extend and the qute extend (to show the function instructions)\
            // bodyBudget is how deep we can expand the function body because we can expend the body only to level 1 + base maximum, and if extensionLevel is less than that, we can only expand to extensionLevel
            int bodyBudget = Math.min(extensionLevel, 1 + base) - 1;
            if (bodyBudget < 0) bodyBudget = 0;

            // argBudget is how deep we can expand the arguments (nested quotes)
            int argBudget = Math.max(0, extensionLevel - (1 + base));


            // -------- 1) Clone body --------
            // 1.1 get function body (function instructions)
            List<Instruction> originalBody = function.getInstructions();

            List<Instruction> body = new ArrayList<>(originalBody.size());
            // 1.2 clone function body (each instruction)
            for (Instruction ins : originalBody) body.add(ins.clone());


            // -------- 2) Build local mapping --------

            // 2.1 inputs (x_i) -> fresh WORK (z_i) by x index order
            List<Variable> inputs = FunctionArgument.collectInputsInOrder(body);
            for (Variable xin : inputs) {
                Variable w = vlm.newZVariable();
                vlm.mapVar(xin, w);
            }

            // 2.2 result (y*) -> fresh (Z for now; prefer newYVariable if you have)
            Variable resultVar = FunctionArgument.findResultVariable(body);
            Variable mappedResult = null;
            if (resultVar != null) {
                mappedResult = vlm.newZVariable();
                vlm.mapVar(resultVar, mappedResult);
            }

            // 2.3 internal WORKs -> fresh WORKs (skip already-mapped inputs)
            Set<Variable> internalWorks = new HashSet<>();
            for (Instruction ins : body) {
                List<Variable> vs = ins.getAllVariables();
                if (vs == null) continue;
                for (Variable v : vs) {
                    if (v.getType() == VariableType.WORK && !vlm.getLocalVarMap().containsKey(v)) {
                        internalWorks.add(v);
                    }
                }
            }
            for (Variable w0 : internalWorks) {
                vlm.mapVar(w0, vlm.newZVariable());
            }

            // -------- 2.4 Labels mapping — map BOTH declared and referenced labels (non-empty only) --------
            // exitLabel: all "EXIT" labels in the function body remap here so GOTO/JNZ EXIT
            // inside the body jumps to the epilogue instead of terminating the outer program.
            Label exitLabel = vlm.newLabel();

            Set<Label> toMap = new java.util.LinkedHashSet<>();

            for (Instruction ins : body) {
                // declared label (line label)
                Label decl = ins.getLabel();
                if (decl != null && decl != FixedLabel.EMPTY) {
                    toMap.add(decl);
                }

                // referenced labels (jump targets)
                List<Label> used = ins.getAllLabels();
                if (used != null) {
                    for (Label u : used) {
                        if (u != null && u != FixedLabel.EMPTY) {
                            toMap.add(u);
                        }
                    }
                }
            }

            // create mappings only for non-empty labels that don't have a local mapping yet
            for (Label l : toMap) {
                if (!vlm.getLocalLabelMap().containsKey(l)) {
                    // EXIT in function body → jump to epilogue (not outer program exit)
                    if (l.getLabelRepresentation() != null
                            && l.getLabelRepresentation().equalsIgnoreCase("EXIT")) {
                        vlm.mapLabel(l, exitLabel);
                    } else {
                        vlm.mapLabel(l, vlm.newLabel());
                    }
                }
            }


            // -------- 3) Prologue: write arguments into mapped x_i --------
            List<Instruction> prologue = new ArrayList<>();

            // no-op with this instruction's label (if any)
            Label anchor = (getLabel() == null ? FixedLabel.EMPTY : getLabel());
            Instruction noOp = new NoOpInstruction(new VariableImpl(VariableType.RESULT, 1), anchor);
            prologue.add(noOp);

            int arity = Math.min(argumentList.size(), inputs.size());
            for (int i = 0; i < arity; i++) {
                Variable xiTarget = vlm.applyVar(inputs.get(i));
                Argument arg = argumentList.get(i);

                if (arg instanceof VariableArgument vArg) {
                    // IMPORTANT: ensure ctor is (source, target, label)
                    prologue.add(new AssignmentInstruction(xiTarget, vArg.getVariable(), FixedLabel.EMPTY));








//                } else if (arg instanceof FunctionArgument fArg) {
//                    // Build nested QUOTE with serialized call-site args (do NOT use getUserString)
//                    String nestedArgs = toArgsString(fArg.arguments);
//                    QuoteInstruction nested = new QuoteInstruction(
//                            fArg.getName(),
//                            nestedArgs,
//                            xiTarget,
//                            fArg.getFunctions(),
//                            FixedLabel.EMPTY
//                    );
//                    prologue.addAll(nested.extend(argBudget, vlm));
//                }

                } else if (arg instanceof FunctionArgument fArg) {
                    // Evaluate the function-argument into a fresh WORK temp, then assign temp -> xiTarget.
                    // This avoids later dependency on fArg.functions and stabilizes nested expansion.

                    // 1) Allocate a fresh WORK temp (prefer via VLM to keep scoping consistent)
                    Variable temp = vlm.newZVariable();

                    // 2) Serialize nested call-site arguments (do NOT rely on userString)
                    String nestedArgs = toArgsString(fArg.arguments);

                    // 3) Be defensive about the functions list (avoid nulls)
                    List<Function> fns = fArg.getFunctions();

                    // 4) Build QUOTE that writes its result into temp (not into xiTarget!)
                    QuoteInstruction nested = new QuoteInstruction(
                            fArg.getName(),
                            nestedArgs,
                            temp,                    // <<< write result into temp
                            fns,
                            FixedLabel.EMPTY
                    );

                    // 5) Expand the nested quote with the argument budget
                    prologue.addAll(nested.extend(argBudget, vlm));

                    // 6) Explicit assignment: temp -> mapped Xi slot
                    if (temp != xiTarget) {
                        prologue.add(new AssignmentInstruction(xiTarget, temp, FixedLabel.EMPTY));
                    }
                }

            }

            // -------- 4) Remap + expand body to bodyBudget --------
            List<Instruction> mappedBody = new ArrayList<>(body.size());
            for (Instruction ins : body) {
                Instruction c = ins.clone();

                // variables
                List<Variable> vs = ins.getAllVariables();
                if (vs != null) {
                    for (Variable v : vs) {
                        Variable to = vlm.applyVar(v);
                        if (to != v) c.replace(v, to);
                    }
                }

                // referenced labels (jump targets)
                List<Label> used = ins.getAllLabels();
                if (used != null) {
                    for (Label lbl : used) {
                        Label to = vlm.applyLabel(lbl);
                        if (to != lbl) c.replace(lbl, to);
                    }
                }

                // declared label (line label) — ALWAYS remap, never strip
                Label declaredLabel = ins.getLabel();
                if (declaredLabel != null && declaredLabel != FixedLabel.EMPTY) {
                    Label to = vlm.applyLabel(declaredLabel);
                    if (to != declaredLabel) c.replace(declaredLabel, to);
                }

                // expand inner quotes in function body
                if (c instanceof QuoteInstruction qi && bodyBudget > 0) {
                    mappedBody.addAll(qi.extend(bodyBudget, vlm));
                } else {
                    mappedBody.add(c);
                }
            }

            // -------- 5) Epilogue: mappedResult -> this.variable --------
            // exitLabel is placed here so GOTO/JNZ EXIT inside the function body lands here.
            List<Instruction> epilogue = new ArrayList<>();
            if (mappedResult != null && this.getVariable() != null) {
                epilogue.add(new AssignmentInstruction(this.getVariable(), mappedResult, exitLabel));
            } else {
                // No result to assign — still need exitLabel to be reachable
                epilogue.add(new NoOpInstruction(new VariableImpl(VariableType.RESULT, 1), exitLabel));
            }

            // -------- 6) glue --------
            List<Instruction> out = new ArrayList<>(prologue.size() + mappedBody.size() + epilogue.size());
            out.addAll(prologue);
            out.addAll(mappedBody);
            out.addAll(epilogue);
            return out;

        } finally {
            if (openedScope) {
                try { vlm.endLocalScope(); } catch (Throwable ignored) {}
            }
        }
    }







}
