package logic.argument;

import core.program.Function;
import core.program.VariableAndLabelMenger;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.execution.FunctionExecutor;
import logic.exception.FunctionNotExist;
import logic.instruction.Instruction;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;
import java.util.ArrayList;
import java.util.List;

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

    private final List<Function> functions;


        public FunctionArgument(String name, List<Argument> arguments, List<Function> funcs) {
        this.name = name;
        this.arguments = arguments;
        this.functions = funcs;

        for (Function f : funcs) {
            if (f.getName().equals(name)) {
                this.instructions = new ArrayList<>(f.getInstructions());
                this.userString = f.getUserString();
                this.function = f;
                break;
            }
        }

    }



    @Override
    public long evaluate(ExecutionContext context, VariableAndLabelMenger vlm) {
        // Evaluate all child arguments in the *current* context to numeric values
        List<Long> values = new ArrayList<>(arguments.size());
        for (Argument arg : arguments) {
            // Use the original context as base to ensure proper variable resolution
            ExecutionContextImpl argContext = new ExecutionContextImpl(context);
            long v = arg.evaluate(argContext, vlm);
            values.add(v);
        }

        // Create a clean execution context for this function evaluation
        ExecutionContextImpl functionContext = new ExecutionContextImpl(values, functions);

        // Ensure function is not null
        if (function == null) {
            throw new RuntimeException("Function not found: " + name);
        }

        // Execute the function with the evaluated arguments
        FunctionExecutor currentExecutor = new FunctionExecutor(function, functions, functionContext);
        long result = currentExecutor.run(values);

//        Variable res = new VariableImpl(VariableType.RESULT, 1);
//        context.updateVariable(res,result);


        return result;
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











    @Override
    public  List<Exception> validate(ExecutionContext context) {
        // for each function need to check if exists\

        return List.of();
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
        switch (extensionLevel) {
            case 0:
                return List.of();
                case 1:
                    return List.of();
        }

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
}
