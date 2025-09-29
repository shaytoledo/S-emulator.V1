package logic.argument;

import core.program.Function;
import core.program.VariableAndLabelMenger;
import javafx.util.Pair;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.execution.FunctionExecutor;
import logic.instruction.Instruction;
import logic.instruction.synthetic.AssignmentInstruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;

import java.util.*;
import java.util.stream.Collectors;

public class FunctionArgument implements Argument {

    // The name of the function to call
    private String name;
    // The function
    private Function function;
    // The user string of the function (for display purposes)
    private String userString;
    // The list of arguments to pass to the function
    public List<Argument> arguments;
    // function instruction
    public List<Instruction> instructions;

    int cycles = 0;
    Label exitLabel;

    private final List<Function> functions;


    public FunctionArgument(String name, List<Argument> arguments, List<Function> funcs) {
        this.name = name;
        this.arguments = arguments;
        this.functions = funcs;

        for (Function f : funcs) {
            if (f.getName().equals(name)) {
                this.instructions = new ArrayList<>(f.getInstructions());
                this.userString = f.getUserString();
                this.function = f.clone();
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
                base = func.calculateMaxDegree(); // ensure its calculated
            }
        }

        int best = base + 1; // +1 for the function call itself
        for (Argument arg : arguments) {
            if (arg instanceof FunctionArgument) {
                int sub = 1 + ((FunctionArgument) arg).getMaxLevel(); // +1 for the argument function call itself
                if (sub > best) {
                    best = sub;
                }
            }
        }
        return best;
    }


    @Override
    public long evaluate(ExecutionContext context, VariableAndLabelMenger vlm, int cycles) {
        // Evaluate all child arguments in the *current* context to numeric values
        List<Long> values = new ArrayList<>(arguments.size());
        for (Argument arg : arguments) {
            // Use the original context as base to ensure proper variable resolution
            ExecutionContextImpl argContext = new ExecutionContextImpl(context);
            long v = arg.evaluate(argContext, vlm, cycles);
            values.add(v);
        }

        // Create a clean execution context for this function evaluation
        ExecutionContextImpl functionContext = new ExecutionContextImpl(values, functions);

        // Execute the function with the evaluated arguments
        FunctionExecutor currentExecutor = new FunctionExecutor(function, functions, functionContext);
        long result = currentExecutor.run(values, cycles);

        return result;
    }





    // 1. Change inputs of instructions to new work variables
    // 2. Change all old input variables to the new work variables
    // 3. Replace all old work variables to new work variables
    // 4. Replace all old labels to new labels

    @Override
    //public Pair<List<Instruction>, Label> extend(int extensionLevel, VariableAndLabelMenger vlm) {
    public List<Instruction> extend(int extensionLevel, VariableAndLabelMenger vlm) {

        if (extensionLevel <= 0) {
            //return new Pair<>(List.of(), null);\
            return List.of();
        }
        else {
            extensionLevel--;
        }


        List<Instruction> instructions = new ArrayList<>();

        // (1+2) Replace all input variables with new work variables and add assignment instructions from input to new work variables
        for (int i = 0; i < arguments.size(); i++) {
            Variable newVar = vlm.newZVariable();
            //assign new work variable to the input variable
            instructions.add(new AssignmentInstruction(newVar, new VariableImpl(VariableType.INPUT, i + 1)));
            // replace all input variables with new work variables
            for (Instruction inst : function.getInstructions()) {
                inst.replace(new VariableImpl(VariableType.INPUT, i + 1), newVar);
            }
        }

        // Use Set to avoid duplicates
        Set<Label> labelsInFunction = new HashSet<>();
        Set<Variable> workVariablesInFunction = new HashSet<>();

        // Get all Labels and Variables in the function
        for (Instruction inst : function.getInstructions()) {
            // add all labels (duplicates automatically ignored in the Set)
            labelsInFunction.addAll(inst.getAllLabels());

            // add only work variables
            workVariablesInFunction.addAll(
                    inst.getAllVariables().stream()
                            .filter(v -> v.getType() == VariableType.WORK)
                            .collect(Collectors.toSet())
            );
        }

        // (3) Replace all old work variables with new work variables
        List<Pair<Variable, Variable>> oldNewWorkVars = new ArrayList<>();
        for (Variable oldVar : workVariablesInFunction) {
            Variable newVar = vlm.newZVariable();
            oldNewWorkVars.add(new Pair<>(oldVar, newVar));
            // replace all old work variables with new work variables
            for (Instruction inst : function.getInstructions()) {
                inst.replace(oldVar, newVar);
            }
        }

        // (4) Replace all old labels with new labels
        List<Pair<Label, Label>> oldNewLabels = new ArrayList<>();
        for (Label oldLabel : labelsInFunction) {
            Label newLabel = vlm.newLabel();
            if (oldLabel.equals(FixedLabel.EXIT)) {
                oldNewLabels.add(new Pair<>(oldLabel, newLabel));
                exitLabel = newLabel;
            } else {
                oldNewLabels.add(new Pair<>(oldLabel, newLabel));
            }
            // replace all old labels with new labels
            for (Instruction inst : function.getInstructions()) {
                inst.replace(oldLabel, newLabel);
            }
        }

        // now in instructions we have the new assignment instructions of the arguments to new work variables,
        // function instructions with replaced variables and labels
        instructions.addAll(function.getInstructions());

        // update function instructions
        function.setInstructions(instructions);


        // Get the maximum instruction level of the function after all changes
        int maxInstructionLevel = function.calculateMaxDegree();

        // Check if we need to extend further
        if (extensionLevel > 0) {

            // need to extend only the instructions
            List<Instruction> extended = new ArrayList<>();
            if (extensionLevel <= maxInstructionLevel) {
                for (Instruction inst : function.getInstructions()) {
                    extended.addAll(inst.extend(extensionLevel, vlm));
                }
            }
            // need to extend the arguments too
            else {
                // extend of the instructions to the max level
                for (Instruction inst : function.getInstructions()) {
                    extended.addAll(inst.extend(extensionLevel, vlm));
                }
                extensionLevel = extensionLevel - maxInstructionLevel;

                // extend the arguments
                for (Argument arg : arguments) {
                    extended.addAll(arg.extend(extensionLevel, vlm));
                }
            }
            function.setInstructions(extended);
            return extended;
        } else {
            // just return the instructions as they are now
            return function.getInstructions();
        }
    }

    public List<Instruction> addEndInstrucrion(Variable var) {
        instructions.add(new AssignmentInstruction(var, new VariableImpl(VariableType.RESULT, 1), exitLabel));
        return instructions;
    }








































//
//        List<Instruction> instructions = new ArrayList<>();
//        Label exitLabel = vlm.newLabel();
//
//        // 1. Create new variables for function arguments and map them
//        // and extend the instructions for each argument
//        List<Pair<Variable, Variable>> variableMapping = new ArrayList<>();
//        for (int i = 0; i < arguments.size(); i++) {
//            Variable oldVar = new VariableImpl(VariableType.INPUT, i + 1);
//            Variable newVar = vlm.newZVariable();
//            variableMapping.add(new Pair<>(oldVar, newVar));
//            // 1) Prepare fresh Z-vars for each function argument (x1..xk -> zArg[i])
//            List<Pair<Variable, Variable>> xToZArg = new ArrayList<>();
//
//
//
//            // 2. Replace variables in function instructions with our new variables
//            Map<Label, Label> labelMapping = new HashMap<>();
//            List<Instruction> functionInstructions = new ArrayList<>();
//
//            // Clone all function instructions
//            for (Instruction instr : function.getInstructions()) {
//                Instruction clonedInstr = instr;
//                functionInstructions.add(clonedInstr);
//            }
//
//            // 3. Replace all labels in the function with new labels
//            for (Instruction instr : functionInstructions) {
//                for (Label label : instr.getAllLabels()) {
//                    if (label != null && !label.equals(FixedLabel.EMPTY)) {
//                        if (!labelMapping.containsKey(label)) {
//                            labelMapping.put(label, vlm.newLabel());
//                        }
//                        instr.replace(label, labelMapping.get(label));
//                    }
//                }
//            }
//
//            // 4. Replace all variables in the function instructions
//            for (Instruction instr : functionInstructions) {
//                // Replace input variables with our new variables
//                for (Pair<Variable, Variable> mapping : variableMapping) {
//                    instr.replace(mapping.getKey(), mapping.getValue());
//                }
//            }
//
//            // Add function's instructions to our list
//            instructions.addAll(functionInstructions);
//
//            // 5. Add an instruction at the end to assign the result to y
//            Variable resultVar = new VariableImpl(VariableType.RESULT, 1);
//            instructions.add(new AssignmentInstruction(resultVar, resultVar, exitLabel));
//
//            return new Pair<>(instructions, exitLabel);
//        }
//    }


    @Override
    public List<String> getAllInfo() {
        return List.of();
    }
}















//    private  List<Instruction> inIt(List<Instruction> instructions, VariableAndLabelMenger vlm, List<Long> values, ExecutionContext context) {
//        //in all pairs <old, new>
//
//        // what inputs variable to change to what work variable
//        List<Pair<Variable, Variable>> xWhatToChange = replaceX(instructions, vlm);
//        // what works variable to change to what work variable because they already used
//        List<Pair<Variable, Variable>> zWhatToChange = replaceZ(instructions, vlm);
//        // what labels to change to what labels
//        List<Pair<Label, Label>> labelWhatToChange = replaceL(instructions, vlm);
//        // the new result variable
//        List<Pair<Variable, Variable>> yWhatToChange = replaceY(instructions, vlm);
//
//        List<Instruction> result = new ArrayList<>(instructions.size());
//
//        result.add(new NoOpInstruction(Variable.RESULT, instructions.get(0).getLabel()));
//
//        for (Pair<Variable, Variable> pair : xWhatToChange) {
//            result.add(new AssignmentInstruction(pair.getValue(), pair.getKey()));
//        }
//
//        for (Pair<Variable, Variable> pair : zWhatToChange) {
//            result.add(new AssignmentInstruction(pair.getValue(), pair.getKey()));
//        }
//
//
//
//        // replace all the necessary parameters
//        for (Instruction instr : instructions) {
//            final List<Variable> varsInInstr = instr.getAllVariables();
//            final List<Label> labelsInInstr = instr.getAllLabels();
//
//            for (Pair<Variable, Variable> p : xWhatToChange) {
//                final Variable oldVar = p.getKey();
//                final Variable newVar = p.getValue();
//                if (varsInInstr.contains(oldVar)) {
//                    instr.replace(oldVar, newVar); // assume overloaded replace(Variable, Variable)
//                }
//            }
//
//            // Apply Z replacements
//            for (Pair<Variable, Variable> p : zWhatToChange) {
//                final Variable oldVar = p.getKey();
//                final Variable newVar = p.getValue();
//                if (varsInInstr.contains(oldVar)) {
//                    instr.replace(oldVar, newVar);
//                }
//            }
//
//            // Apply label replacements (if any)
//            for (Pair<Label, Label> p : labelWhatToChange) {
//                final Label oldLabel = p.getKey();
//                final Label newLabel = p.getValue();
//                if (labelsInInstr.contains(oldLabel)) {
//                    instr.replace(oldLabel, newLabel); // assume overloaded replace(Label, Label)
//                }
//            }
//
//
//            // Apply Y/result replacement (if any)
//            for (Pair<Variable, Variable> p : yWhatToChange) {
//                final Variable oldVar = p.getKey();
//                final Variable newVar = p.getValue();
//                if (varsInInstr.contains(oldVar)) {
//                    instr.replace(oldVar, newVar);
//                }
//
//
//                result.add(instr);
//            }
//            // add instructions that take the args into the new works variable
//
//        }
//        return result;
//    }
//
//
//    private List<Pair<Variable,Variable>> replaceX(List<Instruction> instructions, VariableAndLabelMenger vlm) {
//
//        // get all the variable
//        List<Variable> x = new ArrayList<>();
//        for (Instruction i : instructions) {
//            List<Variable> currVariables = i.getAllVariables();
//            x.addAll(currVariables);
//        }
//        // sort and get all the input variable
//        x = x.stream()
//                .filter(v -> v.getType() == VariableType.INPUT)
//                .sorted()
//                .collect(Collectors.toList()
//                )
//        ;
//        int size = x.size();
//        // create new work variable to change x
//        List<Pair<Variable,Variable>> whatToChange = new ArrayList<>();
//        for (int i = 0; i < size; i++) {
//            whatToChange.add(new Pair<>(vlm.newZVariable(),x.get(i)));
//        }
//        return whatToChange;
//
//
//    }
//    private List<Pair<Variable,Variable>> replaceZ(List<Instruction> instructions, VariableAndLabelMenger vlm) {
//
//        // get all the variable
//        List<Variable> z = new ArrayList<>();
//        for (Instruction i : instructions) {
//            List<Variable> currVariables = i.getAllVariables();
//            z.addAll(currVariables);
//        }
//        // sort and get all the input variable
//        z = z.stream()
//                .filter(v -> v.getType() == VariableType.WORK)
//                .sorted()
//                .collect(Collectors.toList()
//                )
//        ;
//        int size = z.size();
//        // create new work variable to change z
//        List<Pair<Variable,Variable>> whatToChange = new ArrayList<>();
//        for (int i = 0; i < size; i++) {
//            if (!vlm.existsVar(z.get(i))) {
//                whatToChange.add(new Pair<>(vlm.newZVariable(), z.get(i)));
//            }
//        }
//        return whatToChange;
//
//
//
//    }
//    private List<Pair<Label,Label>> replaceL(List<Instruction> instructions, VariableAndLabelMenger vlm) {
//        List<Label> L = new ArrayList<>();
//        for (Instruction i : instructions) {
//            List<Label> currVariables = i.getAllLabels();
//            L.addAll(currVariables);
//        }
//
//        // create new labels to change from the old labels
//        int size = L.size();
//        List<Pair<Label,Label>> whatToChange = new ArrayList<>();
//        for (int i = 0; i < size; i++) {
//            if (!vlm.existsLabel(L.get(i))) {
//                whatToChange.add(new Pair<>(vlm.newLabel(), L.get(i)));
//            }
//        }
//        return whatToChange;
//    }
//    private  List<Pair<Variable,Variable>> replaceY(List<Instruction> instructions, VariableAndLabelMenger vlm) {
//        // get all the variable
//        List<Variable> y = new ArrayList<>();
//        for (Instruction i : instructions) {
//            List<Variable> currVariables = i.getAllVariables();
//            y.addAll(currVariables);
//        }
//        // sort and get all the input variable
//        y = y.stream()
//                .filter(v -> v.getType() == VariableType.RESULT)
//                .sorted()
//                .collect(Collectors.toList()
//                )
//        ;
//        int size = y.size();
//        // create new work variable to change z
//        List<Pair<Variable,Variable>> whatToChange = new ArrayList<>();
//        for (int i = 0; i < size; i++) {
//            if (!vlm.existsVar(y.get(i))) {
//                whatToChange.add(new Pair<>(vlm.newZVariable(), y.get(i)));
//            }
//        }
//        return whatToChange;    }
//}