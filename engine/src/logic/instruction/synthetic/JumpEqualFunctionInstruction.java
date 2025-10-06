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
        return arguments.getMaxLevel() + 2;
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
        if (extensionLevel <= 0) {
            return List.of(this.clone());
        }

        boolean openedScope = false;
        try {
            if (vlm != null) {
                vlm.beginLocalScope();
                openedScope = true;
            }

            // -------- 0) Budgets --------
            int base = function.getInstructions().stream()
                    .mapToInt(Instruction::getMaxLevel)
                    .max().orElse(0);

            int bodyBudget = Math.min(extensionLevel, 1 + base) - 1;
            if (bodyBudget < 0) bodyBudget = 0;

            int argBudget = Math.max(0, extensionLevel - (1 + base));

            // -------- 1) Clone body --------
            List<Instruction> originalBody = function.getInstructions();
            List<Instruction> body = new ArrayList<>(originalBody.size());
            for (Instruction ins : originalBody) body.add(ins.clone());

            // -------- 2) Build local mappings --------

            // 2.1 inputs (x_i) -> fresh WORK (z_i)
            List<Variable> inputs = FunctionArgument.collectInputsInOrder(body);
            for (Variable xin : inputs) {
                Variable w = vlm.newZVariable();
                vlm.mapVar(xin, w);
            }

            // 2.2 result (y) -> fresh WORK (mappedResult)
            Variable resultVar = FunctionArgument.findResultVariable(body);
            Variable mappedResult = null;
            if (resultVar != null) {
                mappedResult = vlm.newZVariable();          // container for function result
                vlm.mapVar(resultVar, mappedResult);
            }

            // 2.3 internal WORKs -> fresh WORKs (skip already-mapped)
            java.util.Set<Variable> internalWorks = new java.util.HashSet<>();
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

            // 2.4 labels: only referenced jump targets
            java.util.Set<Label> referencedTargets = new java.util.HashSet<>();
            for (Instruction ins : body) {
                List<Label> used = ins.getAllLabels();
                if (used != null) referencedTargets.addAll(used);
            }
            for (Label l : referencedTargets) {
                if (!vlm.getLocalLabelMap().containsKey(l)) {
                    vlm.mapLabel(l, vlm.newLabel());
                }
            }

            // -------- 3) Prologue: NoOp(anchor) + write args into mapped x_i --------
            List<Instruction> prologue = new ArrayList<>();
            Label anchor = (getLabel() == null ? FixedLabel.EMPTY : getLabel());
            // keep same signature you use elsewhere for NoOp
            Instruction noOp = new logic.instruction.basic.NoOpInstruction(
                    new VariableImpl(VariableType.RESULT, 1), anchor);
            prologue.add(noOp);

            List<Argument> argumentList = this.arguments.arguments; // use getter if you have
            int arity = Math.min(argumentList.size(), inputs.size());
            for (int i = 0; i < arity; i++) {
                Variable xiTarget = vlm.applyVar(inputs.get(i));
                Argument arg = argumentList.get(i);

                if (arg instanceof VariableArgument vArg) {
                    // Assignment: xi <- sourceVar   (target, source, label)
                    prologue.add(new AssignmentInstruction(
                            xiTarget, vArg.getVariable(), FixedLabel.EMPTY));

                } else if (arg instanceof FunctionArgument fArg) {
                    // Inline nested function into xiTarget
                    QuoteInstruction nested = new QuoteInstruction(
                            fArg.getName(),
                            fArg.getArgs(),
                            xiTarget,
                            fArg.getFunctions(),
                            FixedLabel.EMPTY
                    );
                    prologue.addAll(nested.extend(argBudget, vlm));
                }
                // If you support ConstantArgument -> add SetImmediateInstruction here
            }

            // -------- 4) Remap + expand body to bodyBudget --------
            List<Instruction> mappedBody = new ArrayList<>(body.size());
            for (Instruction ins : body) {
                Instruction c = ins.clone();

                // variables
                for (Variable v : ins.getAllVariables()) {
                    Variable to = vlm.applyVar(v);
                    if (to != v) c.replace(v, to);
                }

                // referenced labels inside instruction
                for (Label used : ins.getAllLabels()) {
                    if (referencedTargets.contains(used)) {
                        Label to = vlm.applyLabel(used);
                        if (to != used) c.replace(used, to);
                    }
                }

                // declared line label: keep only if referenced
                Label declared = ins.getLabel();
                if (declared != null && declared != FixedLabel.EMPTY) {
                    if (referencedTargets.contains(declared)) {
                        Label to = vlm.applyLabel(declared);
                        if (to != declared) c.replace(declared, to);
                    } else {
                        c.replace(declared, FixedLabel.EMPTY);
                    }
                }

                // expand nested quotes to bodyBudget
                if (c instanceof QuoteInstruction qi && bodyBudget > 0) {
                    mappedBody.addAll(qi.extend(bodyBudget, vlm));
                } else {
                    mappedBody.add(c);
                }
            }

            // -------- 5) Epilogue: assign function result to a NEW var, then JUMP EQUAL --------
            List<Instruction> epilogue = new ArrayList<>();

            // safety: if function had no explicit result var, still create one to compare
            if (mappedResult == null) {
                mappedResult = vlm.newZVariable();
            }

            // new variable to hold the (final) function result explicitly, as you requested
            Variable compareVar = vlm.newZVariable();

            // explicit assignment: compareVar <- mappedResult     (target, source, label)
            epilogue.add(new AssignmentInstruction(
                    compareVar, mappedResult, FixedLabel.EMPTY));

            // Jump if EQUAL: IF getVariable() == compareVar GOTO jnzLabel
            epilogue.add(new logic.instruction.synthetic.JumpEqualVariableInstruction(
                    this.getVariable(),
                    jnzLabel,
                    compareVar
                    ));

            // -------- 6) Glue --------
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



//    @Override
//    public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {
//        if (extensionLevel <= 0) {
//            return List.of(this.clone());
//        }
//
//        boolean openedScope = false;
//        try {
//            if (vlm != null) {
//                vlm.beginLocalScope();
//                openedScope = true;
//            }
//
//            // -------- 0) Budgets --------
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
//            // -------- 2) Build local mappings --------
//
//            // 2.1 inputs (x_i) -> fresh WORK (z_i)
//            List<Variable> inputs = FunctionArgument.collectInputsInOrder(body);
//            for (Variable xin : inputs) {
//                Variable w = vlm.newZVariable();
//                vlm.mapVar(xin, w);
//            }
//
//            // 2.2 result (y) -> fresh WORK (mappedResult)  **THIS replaces Y**
//            Variable resultVar = FunctionArgument.findResultVariable(body);
//            Variable mappedResult = null;
//            if (resultVar != null) {
//                mappedResult = vlm.newZVariable();  // או vlm.newYVariable() אם קיים אצלך
//                vlm.mapVar(resultVar, mappedResult);
//            }
//
//            // 2.3 internal WORKs -> fresh WORKs (skip already-mapped inputs)
//            java.util.Set<Variable> internalWorks = new java.util.HashSet<>();
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
//            // 2.4 labels: only referenced jump targets
//            java.util.Set<Label> referencedTargets = new java.util.HashSet<>();
//            for (Instruction ins : body) {
//                List<Label> used = ins.getAllLabels();
//                if (used != null) referencedTargets.addAll(used);
//            }
//            for (Label l : referencedTargets) {
//                if (!vlm.getLocalLabelMap().containsKey(l)) {
//                    vlm.mapLabel(l, vlm.newLabel());
//                }
//            }
//
//            // -------- 3) Prologue: anchor + write args into mapped x_i --------
//            List<Instruction> prologue = new ArrayList<>();
//            Label anchor = (getLabel() == null ? FixedLabel.EMPTY : getLabel());
//            Instruction noOp = new logic.instruction.basic.NoOpInstruction(
//                    new VariableImpl(VariableType.RESULT, 1), anchor);
//            prologue.add(noOp);
//
//            // רשימת הארגומנטים של הפונקציה הזו (מתוך FunctionArgument)
//            List<Argument> argumentList = this.arguments.arguments; // השתמש ב-getter אם יש
//
//            int arity = Math.min(argumentList.size(), inputs.size());
//            for (int i = 0; i < arity; i++) {
//                Variable xiTarget = vlm.applyVar(inputs.get(i));
//                Argument arg = argumentList.get(i);
//
//                if (arg instanceof VariableArgument vArg) {
//                    // (source, target, label) – ודא סדר פרמטרים תואם למחלקה שלך
//                    prologue.add(new AssignmentInstruction(
//                             xiTarget,vArg.getVariable(), FixedLabel.EMPTY));
//
//                } else if (arg instanceof FunctionArgument fArg) {
//                    // inline nested function into xiTarget
//                    QuoteInstruction nested = new QuoteInstruction(
//                            fArg.getName(),
//                            fArg.getArgs(),
//                            xiTarget,
//                            fArg.getFunctions(),
//                            FixedLabel.EMPTY
//                    );
//                    prologue.addAll(nested.extend(argBudget, vlm));
//                }
//                // TODO: ConstantArgument -> SetImmediateInstruction אם קיים אצלך
//            }
//
//            // -------- 4) Remap + expand body to bodyBudget --------
//            List<Instruction> mappedBody = new ArrayList<>(body.size());
//            for (Instruction ins : body) {
//                Instruction c = ins.clone();
//
//                // variables
//                for (Variable v : ins.getAllVariables()) {
//                    Variable to = vlm.applyVar(v);
//                    if (to != v) c.replace(v, to);
//                }
//
//                // referenced labels inside instruction
//                for (Label used : ins.getAllLabels()) {
//                    if (referencedTargets.contains(used)) {
//                        Label to = vlm.applyLabel(used);
//                        if (to != used) c.replace(used, to);
//                    }
//                }
//
//                // declared line label: keep only if referenced
//                Label declared = ins.getLabel();
//                if (declared != null && declared != FixedLabel.EMPTY) {
//                    if (referencedTargets.contains(declared)) {
//                        Label to = vlm.applyLabel(declared);
//                        if (to != declared) c.replace(declared, to);
//                    } else {
//                        c.replace(declared, FixedLabel.EMPTY);
//                    }
//                }
//
//                // expand nested quotes inside body
//                if (c instanceof QuoteInstruction qi && bodyBudget > 0) {
//                    mappedBody.addAll(qi.extend(bodyBudget, vlm));
//                } else {
//                    mappedBody.add(c);
//                }
//            }
//
//            // -------- 5) Epilogue: JUMP if NOT EQUAL to jnzLabel --------
//            List<Instruction> epilogue = new ArrayList<>();
//            if (mappedResult == null) {
//                // מקרה קצה: אם לפונקציה אין Y מוצהר, עדיין נשווה מול z חדש
//                mappedResult = vlm.newZVariable();
//            }
//
//            // כאן הקפיצה היא "אם לא שווה" כפי שביקשת:
//            epilogue.add(new logic.instruction.synthetic.JumpEqualVariableInstruction(
//                    this.getVariable(),   // current instruction's variable
//                    jnzLabel,              // label to jump if NOT equal
//                    mappedResult          // the (remapped) function output
//
//            ));
//
//            // -------- 6) Glue --------
//            List<Instruction> out = new ArrayList<>(prologue.size() + mappedBody.size() + epilogue.size());
//            out.addAll(prologue);
//            out.addAll(mappedBody);
//            out.addAll(epilogue);
//            return out;
//
//        } finally {
//            if (openedScope) {
//                try { vlm.endLocalScope(); } catch (Throwable ignored) {}
//            }
//        }
//    }



    private boolean isValidFunction(String name, List<Function> funcs) {
        for (Function func : funcs) {
            if (func.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
