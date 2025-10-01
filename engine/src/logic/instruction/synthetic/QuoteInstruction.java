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

//    public QuoteInstruction(String name, String functionArguments,Variable var,  List<Function> funcs, Label lineLabel) {
//        super(InstructionData.QUOTE, var, lineLabel);
//        this.argumentList = new ArrayList<>(toArguments(functionArguments ,funcs));
//        this.functionArguments = functionArguments;
//        this.variable = var;
//        this.arguments = new FunctionArgument(name, argumentList, funcs);
//        this.allFunctions = funcs;
//
//
//        for (Function f : funcs) {
//            if (f.getName().equals(name)) {
//                this.function = f;
//                break;
//            }
//        }
//    }
//    public QuoteInstruction(String name, String functionArguments,Variable var,  List<Function> funcs) {
//        super(InstructionData.QUOTE, var);
//        List<Argument> arguments = new ArrayList<>(toArguments(functionArguments ,funcs));
//        this.arguments = new FunctionArgument(name, arguments, funcs);
//        this.allFunctions = funcs;
//
//        for (Function f : funcs) {
//            if (f.getName().equals(name)) {
//                this.function = f;
//                break;
//            }
//        }
//    }


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
        // הפוך את הרשימה ל-"x1,(Foo,x2),z3" וכו' לפי אותו פורמט שמצפה לו toArguments()
        // דוגמה עקרונית – צריך לממש לפי הטיפוסים שיש לך:
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
        if(getLabel() == null) {
            return new QuoteInstruction(function.getName(), functionArguments, getVariable(), allFunctions);
        } else {
            return new QuoteInstruction(function.getName(), functionArguments, getVariable(), allFunctions, getLabel());
        }
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
        if(getVariable() != null) {
            return List.of(getVariable());
        }
        return List.of();
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

        // 1) clone function body
        List<Instruction> body = new ArrayList<>();
        for (Instruction ins : function.getInstructions()) body.add(ins.clone());

        // 2) build local mapping in this scope
        // 2.1 inputs -> fresh WORK
        List<Variable> inputs = FunctionArgument.collectInputsInOrder(body);
        for (Variable xin : inputs) {
            Variable w = vlm.newZVariable();     // use your factory for WORK (z)
            vlm.mapVar(xin, w);
        }

        // 2.2 result -> fresh RESULT
        Variable resultVar = FunctionArgument.findResultVariable(body);
        Variable mappedResult = vlm.newZVariable(); // use your factory for RESULT (y)
        if (resultVar != null) vlm.mapVar(resultVar, mappedResult);

        // 2.3 internal WORKs -> fresh WORKs (skip already-mapped inputs)
        Set<Variable> internalWorks = new HashSet<>();
        for (Instruction ins : body) {
            for (Variable v : ins.getAllVariables()) {
                if (v.getType() == VariableType.WORK && !vlm.getLocalVarMap().containsKey(v)) {
                    internalWorks.add(v);
                }
            }
        }
        for (Variable w0 : internalWorks) {
            vlm.mapVar(w0, vlm.newZVariable());
        }

        // 2.4 labels -> fresh labels
        // Only map labels that are referenced by functions (i.e., labels that are jump targets)
        Set<Label> referencedLabels = new HashSet<>();
        // First identify which labels are referenced by functions in the body
        for (Instruction ins : body) {
            // Add logic to identify referenced labels here
            // For now, we'll treat all existing labels as potentially referenced
            if (ins.getLabel() != null && ins.getLabel() != FixedLabel.EMPTY) {
                referencedLabels.add(ins.getLabel());
            }
        }

        // Only map labels that are referenced
        for (Label l : referencedLabels) {
            if (!vlm.getLocalLabelMap().containsKey(l)) {
                vlm.mapLabel(l, vlm.newLabel());
            }
        }

        // 3) prologue: write arguments into mapped(INPUT_i)
        List<Instruction> prologue = new ArrayList<>();
        Label label = getLabel() == null ? FixedLabel.EMPTY : getLabel();
        Instruction noOp = new NoOpInstruction(new VariableImpl(VariableType.RESULT, 1), label);
        prologue.add(noOp);
        for (int i = 0; i < Math.min(argumentList.size(), inputs.size()); i++) {
            Variable xiTarget = vlm.applyVar(inputs.get(i));
            Argument arg = argumentList.get(i);

            if (arg instanceof VariableArgument v) {
                // if your AssignmentInstruction is (target <- source): AssignmentInstruction(source, target, [label])
                // NOTE: If your constructor order is (target, source) – flip the params accordingly.
                prologue.add(new AssignmentInstruction(v.getVariable(), xiTarget, FixedLabel.EMPTY));
            } else if (arg instanceof FunctionArgument f) {
                // Nested function: inline it to level-1 into xiTarget using a forked scope
                QuoteInstruction nested = new QuoteInstruction(
                        f.getName(),            // function name
                        f.getArgs(),       // build user string of args if you keep it
                        xiTarget,
                        f.getFunctions(),
                        FixedLabel.EMPTY
                );
                prologue.addAll(nested.extend(extensionLevel - 1, vlm)); // uses same vlm (counters shared), maps are local within nested because extend() will call beginLocalScope at Program-level; if not, we can wrap:
                // Alternative safe local mapping:
                // vlm.beginLocalScope();
                // try { prologue.addAll(nested.extend(extensionLevel - 1, vlm)); }
                // finally { vlm.endLocalScope(); }
            }
        }

        // 4) remap variables & labels in the cloned body
        List<Instruction> mappedBody = new ArrayList<>(body.size());
        for (Instruction ins : body) {
            Instruction c = ins.clone();
            for (Variable v : ins.getAllVariables()) {
                Variable to = vlm.applyVar(v);
                if (to != v) c.replace(v, to);
            }

            // Only remap labels that are referenced by functions
            // If the instruction has a label but it's not referenced, set it to EMPTY
            Label currentLabel = ins.getLabel();
            if (currentLabel != null && currentLabel != FixedLabel.EMPTY) {
                if (referencedLabels.contains(currentLabel)) {
                    // This label is referenced, so apply the mapping
                    Label to = vlm.applyLabel(currentLabel);
                    if (to != currentLabel) c.replace(currentLabel, to);
                } else {
                    // This label is not referenced, so remove it by setting to EMPTY
                    c.replace(currentLabel, FixedLabel.EMPTY);
                }
            }

            mappedBody.add(c);
        }

        // 5) epilogue: mappedResult -> this.variable  (y <- RESULT)
        List<Instruction> epilogue = new ArrayList<>();
        if (mappedResult != null) {
            // If ctor is (source, target): AssignmentInstruction(mappedResult, this.getVariable(), null)
            epilogue.add(new AssignmentInstruction( this.getVariable(),mappedResult, FixedLabel.EMPTY));
        }

        // 6) return full sequence
        List<Instruction> out = new ArrayList<>(prologue.size() + mappedBody.size() + epilogue.size());
        out.addAll(prologue);
        out.addAll(mappedBody);
        out.addAll(epilogue);
        return out;
    }





    public void nothing () {
        // to avoid warning
    }

//    @Override
//    public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {
//        // Defensive: never mutate caller state
//        if (extensionLevel <= 0) {
//            return List.of(this.clone());
//        }
//
//        // --- Scope isolation for local var/label mappings (void signature) ---
//        boolean openedScope = false;
//        try {
//            if (vlm != null) {
//                vlm.beginLocalScope();  // void
//                openedScope = true;
//            }
//
//            // ... rest of extend() logic ...
//
//        } finally {
//            if (openedScope) {
//                try { vlm.endLocalScope(); } catch (Throwable ignored) {}
//            }
//        }
//
//
//        try {
//            // -------- 0) Compute base, bodyBudget, argBudget --------
//            int base = function.getInstructions().stream()
//                    .mapToInt(Instruction::getMaxLevel)
//                    .max().orElse(0);
//
//            int bodyBudget = Math.min(extensionLevel, 1 + base) - 1;
//            if (bodyBudget < 0) bodyBudget = 0;
//
//            int argBudget = Math.max(0, extensionLevel - (1 + base));
//
//            // -------- 1) Clone body --------
//            List<Instruction> originalBody = function.getInstructions();
//            List<Instruction> body = new ArrayList<>(originalBody.size());
//            for (Instruction ins : originalBody) body.add(ins.clone());
//
//            // -------- 2) Build local mapping in this scope --------
//
//            // 2.1 inputs (x_i) -> fresh WORK (z_i)
//            List<Variable> inputs = FunctionArgument.collectInputsInOrder(body);
//            for (Variable xin : inputs) {
//                Variable w = vlm.newZVariable(); // keep Z factory as per your current API
//                vlm.mapVar(xin, w);
//            }
//
//            // 2.2 result (y) -> fresh (currently also Z per your API; swap to newYVariable if you have it)
//            Variable resultVar = FunctionArgument.findResultVariable(body);
//            Variable mappedResult = null;
//            if (resultVar != null) {
//                mappedResult = vlm.newZVariable(); // prefer vlm.newYVariable() if available
//                vlm.mapVar(resultVar, mappedResult);
//            }
//
//            // 2.3 internal WORKs -> fresh WORKs (skip already-mapped inputs)
//            Set<Variable> internalWorks = new HashSet<>();
//            for (Instruction ins : body) {
//                for (Variable v : ins.getAllVariables()) {
//                    if (v.getType() == VariableType.WORK && !vlm.getLocalVarMap().containsKey(v)) {
//                        internalWorks.add(v);
//                    }
//                }
//            }
//            for (Variable w0 : internalWorks) {
//                vlm.mapVar(w0, vlm.newZVariable());
//            }
//
//            // 2.4 Labels mapping — only for labels that are actually referenced as jump targets
//            Set<Label> referencedTargets = new HashSet<>();
//            for (Instruction ins : body) {
//                // By convention, getAllLabels() returns labels referenced by the instruction (e.g., jump targets)
//                List<Label> used = ins.getAllLabels();
//                if (used != null) referencedTargets.addAll(used);
//            }
//            for (Label l : referencedTargets) {
//                if (!vlm.getLocalLabelMap().containsKey(l)) {
//                    vlm.mapLabel(l, vlm.newLabel());
//                }
//            }
//
//            // -------- 3) Prologue: place arguments into mapped inputs (xi) --------
//            List<Instruction> prologue = new ArrayList<>();
//
//            // Keep the original line label of this QUOTE by anchoring a harmless NoOp first.
//            Label anchor = (getLabel() == null ? FixedLabel.EMPTY : getLabel());
//            Instruction noOp = new NoOpInstruction(new VariableImpl(VariableType.RESULT, 1), anchor);
//            prologue.add(noOp);
//
//            int arity = Math.min(argumentList.size(), inputs.size());
//
//            for (int i = 0; i < arity; i++) {
//                Variable xiTarget = vlm.applyVar(inputs.get(i));
//                Argument arg = argumentList.get(i);
//
//                if (arg instanceof VariableArgument vArg) {
//                    // Assignment: xi <- var
//                    // IMPORTANT: verify constructor order in your AssignmentInstruction.
//                    // Here we use (source, target, label)
//                    prologue.add(new AssignmentInstruction(vArg.getVariable(), xiTarget, FixedLabel.EMPTY));
//
//                } else if (arg instanceof FunctionArgument fArg) {
//                String nestedArgs = /* עדיף */ fArg.getUserString() /* ואם אין: */ /* toArgsString(fArg.arguments) */;
//                QuoteInstruction nested = new QuoteInstruction(
//                        fArg.getName(),
//                        nestedArgs,
//                        xiTarget,
//                        fArg.getFunctions(),
//                        FixedLabel.EMPTY
//                );
//                prologue.addAll(nested.extend(argBudget, vlm));
//            }
//
//            // If you have ConstantArgument and an immediate-load instruction, add it here.
//                // else if (arg instanceof ConstantArgument cArg) {
//                //     prologue.add(new SetImmediateInstruction(xiTarget, cArg.getValue(), FixedLabel.EMPTY));
//                // }
//            }
//
//            // -------- 4) Remap + expand body to bodyBudget --------
//            List<Instruction> mappedBody = new ArrayList<>(body.size());
//
//            for (Instruction ins : body) {
//                Instruction c = ins.clone();
//
//                // replace variables
//                for (Variable v : ins.getAllVariables()) {
//                    Variable to = vlm.applyVar(v);
//                    if (to != v) c.replace(v, to);
//                }
//
//                // replace referenced labels inside the instruction (jump targets)
//                for (Label used : ins.getAllLabels()) {
//                    if (referencedTargets.contains(used)) {
//                        Label to = vlm.applyLabel(used);
//                        if (to != used) c.replace(used, to);
//                    }
//                }
//
//                // handle the declared label on the instruction line itself
//                Label declared = ins.getLabel();
//                if (declared != null && declared != FixedLabel.EMPTY) {
//                    if (referencedTargets.contains(declared)) {
//                        Label to = vlm.applyLabel(declared);
//                        if (to != declared) c.replace(declared, to);
//                    } else {
//                        // not referenced → strip it
//                        c.replace(declared, FixedLabel.EMPTY);
//                    }
//                }
//
//                // Expand nested quotes inside the body to bodyBudget
//                if (c instanceof QuoteInstruction qi && bodyBudget > 0) {
//                    mappedBody.addAll(qi.extend(bodyBudget, vlm));
//                } else {
//                    mappedBody.add(c);
//                }
//            }
//
//            // -------- 5) Epilogue: mappedResult -> this.variable (y <- RESULT) --------
//            List<Instruction> epilogue = new ArrayList<>();
//            if (mappedResult != null) {
//                // IMPORTANT: keep constructor order consistent across your project.
//                // Here we use (source, target, label): target is this.getVariable()
//                epilogue.add(new AssignmentInstruction(this.getVariable(),mappedResult, FixedLabel.EMPTY));
//            }
//
//            // -------- 6) glue all together --------
//            List<Instruction> out = new ArrayList<>(prologue.size() + mappedBody.size() + epilogue.size());
//            out.addAll(prologue);
//            out.addAll(mappedBody);
//            out.addAll(epilogue);
//            return out;
//
//        } finally {
//            // close local scope if opened
//            if (openedScope) {
//                try { vlm.endLocalScope(); } catch (Throwable ignored) {}
//            }
//        }
//    }


}
