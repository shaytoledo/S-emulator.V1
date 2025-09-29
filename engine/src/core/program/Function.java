package core.program;

import dto.InstructionView;
import dto.RunSummary;
import dto.functionView;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.instruction.Instruction;
import logic.instruction.synthetic.JumpEqualFunctionInstruction;
import logic.instruction.synthetic.QuoteInstruction;
import logic.label.Label;
import logic.variable.Variable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static core.program.ProgramImpl.addIfStartsWithX;

public class Function implements Program {

    private final String name;
    private final String userString;
    private List<Instruction> instructions;
    private List<Instruction> extendedInstructions;
    private final List<String> args;
    public List<RunSummary> summaries = new ArrayList<>();


    public List<Label> labels = new ArrayList<>();
    public List<Variable> variables = new ArrayList<>();
    public List<Function> myFuncs = new ArrayList<>();
    public VariableAndLabelMenger vlm ;


    public Function(String name, String userString, List<Instruction> instructions) {
        this.name = name;
        this.userString = userString;
        this.instructions = instructions;
        this.args = getArgs(instructions);
        this.extendedInstructions = new ArrayList<>(instructions);
    }

    private List<String> getArgs(List<Instruction> instructions) {

        List<String> args = new ArrayList<>();
        for (Instruction instruction : instructions) {
            if (instruction.getVariable() != null) {
                String varName = instruction.getVariable().getRepresentation();
                if (!args.contains(varName)) {
                    args.add(varName);
                }
            }
        }
        return args;
    }

    public int calculateMaxDegree() {
        int maxDegree = 0;
        for (Instruction instruction : instructions) {
            int degree = instruction.getMaxLevel();
            if (degree > maxDegree) {
                maxDegree = degree;
            }
        }
        return maxDegree;
    }
    public List<Instruction> getInstructions() {
        return instructions;
    }

    public String getUserString() {
        return userString;
    }
    public String getName() {
        return name;
    }

    // return the next instruction after the current one, or null if at the end
    public Instruction getNextInstructionLabel(Instruction currentInstruction) {
        int currentIndex = extendedInstructions.indexOf(currentInstruction);
        if (currentIndex >= 0 && currentIndex + 1 < extendedInstructions.size()) {
            return extendedInstructions.get(currentIndex + 1);
        }
        else {
            return null;
        }
    }

    // return the instruction with the given label
    public Instruction getInstructionByLabel(Label nextLabel) {
        for (Instruction instruction : extendedInstructions) {
            if(instruction.getLabel() != null) {
                if (instruction.getLabel().equals(nextLabel)) {
                    return instruction;
                }
            }
        }
        //It always finds the instruction by label because in load program it checks the label jumps
        return null; // or throw an exception if label not found

    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
        this.extendedInstructions = new ArrayList<>(instructions);
    }

    public functionView toView(){
        List<InstructionView> instructionViews = new ArrayList<>();
        int number = 1;
        for (Instruction instruction : instructions) {

            String cyclesText = (instruction instanceof QuoteInstruction)
                    ? instruction.cycles() + "+"
                    : Integer.toString(instruction.cycles());

            InstructionView iv = new InstructionView(
                    number,
                    instruction.getName()
                    , instruction.getLabel() == null ? null : instruction.getLabel().getLabelRepresentation(),
                    cyclesText,
                    instruction.isBasic() ? "basic" : "composite");
            instructionViews.add(iv);
            number++;
        }
        return new functionView(name, args, instructionViews);
    }




    public List<Instruction> getExtendedInstructions(int extensionLevel, VariableAndLabelMenger vlm) {
        List<Instruction> result = new ArrayList<>();
        for (Instruction instruction : instructions) {
            result.addAll(instruction.extend(extensionLevel, vlm));
        }

        return result;
    }
    public long evaluate() {
        long result = 0;

        return result;

    }







    /// No have really variables labels and vlm

    @Override
    public List<String> getVariablesPeek() {
        Set<String> variables = new LinkedHashSet<>();

        for (Instruction instr : extendedInstructions) {
            List<String> info = instr.getAllInfo();
            for (String s : info) {
                //addIfStartsWithXOrZOry(variables, s);
                addIfStartsWithX(variables, s);
            }
        }

        return new ArrayList<>(variables);    }

    @Override
    public List<String> getLabelsPeek() {
        List<String> info = new ArrayList<>();
        for (Instruction instr : extendedInstructions) {
            List<String> curr = instr.getAllInfo();
            for (String s : curr) {
                if (s != null && !s.isBlank() && !s.equalsIgnoreCase("EXIT") &&s.startsWith("L")) {
                    info.add(s.toUpperCase());
                }
            }
        }
        info.sort(String::compareTo);

        List<String> uniqueInfo = info.stream()
                .distinct()
                .toList();

        return uniqueInfo;    }

    @Override
    public List<Function> getFunctions() {
        return myFuncs;
    }

    @Override
    public List<functionView> getAllFunctionViews() {
        List<functionView> result = new ArrayList<>();
        for (Function function : myFuncs) {
            result.add(function.toView());
        }
        return result;    }

    // convert Instruction to InstructionView for display in table
    public static InstructionView toView(Instruction ins, int index) {
        int number = index;
        String type = ins.isBasic() ? "B" : "S";
        String label = (ins.getLabel() == null)
                ? ""
                : ins.getLabel().getLabelRepresentation();
        String command = ins.toDisplayString();
        int midCycles = ins.cycles();
        String cycles = ((ins instanceof QuoteInstruction) || (ins instanceof JumpEqualFunctionInstruction))  ? String.valueOf(midCycles) + "+": String.valueOf(midCycles);
        return new InstructionView(number, type, label, command, cycles);
    }

    @Override
    public List<InstructionView> getInstructionsPeek() {
        List<InstructionView> instructionViews = new ArrayList<>(extendedInstructions.size());

        int size = extendedInstructions.size();

        for (int i = 0; i < size; i++) {
            Instruction inst = extendedInstructions.get(i);
            int number = i + 1;

            String type = inst.isBasic() ? "B" : "S";

            String lbl = (inst.getLabel() != null && inst.getLabel().getLabelRepresentation() != null)
                    ? inst.getLabel().getLabelRepresentation()
                    : "";

            String command = inst.toDisplayString();
            int midCycles = inst.cycles();
            String cycles = ((inst instanceof QuoteInstruction) || (inst instanceof JumpEqualFunctionInstruction))  ? String.valueOf(midCycles) + "+": String.valueOf(midCycles);

            InstructionView curr = new InstructionView(
                    number,
                    type,
                    lbl,
                    command,
                    cycles
            );

            instructionViews.add(curr);
        }
        return instructionViews;
    }

    @Override
    public VariableAndLabelMenger getvlm() {
        return vlm;
    }

    // extend Instructions to the given level (with the original instructions)
    public void extend (int level) {
        vlm = new VariableAndLabelMenger(variables, labels);

        extendedInstructions.clear();
        for (Instruction inst : instructions) {
            List<Instruction> extended = inst.extend(level, vlm);
            extendedInstructions.addAll(extended);
        }
    }

    // recursive method to expand paths
    private List<List<Instruction>> expandPaths(Instruction inst, int level, VariableAndLabelMenger vlm) {
        List<List<Instruction>> out = new ArrayList<>();

        if (level <= 0 || inst.getMaxLevel() <= 0) {
            out.add(List.of(inst));
            return out;
        }
        List<Instruction> children = inst.extend(1, vlm);

        if (children == null || children.isEmpty()) {
            out.add(List.of(inst));
            return out;
        }


        for (Instruction child : children) {
            List<List<Instruction>> tails = expandPaths(child, level - 1, vlm);

            for (List<Instruction> tail : tails) {
                ArrayList<Instruction> path = new ArrayList<>(1 + tail.size());
                path.add(inst);
                path.addAll(tail);
                out.add(path);
            }
        }

        return out;
    }

    @Override
    public List<List<InstructionView>> expendToLevelForExtend(int level) {
        vlm = new VariableAndLabelMenger(variables, labels);
        List<List<InstructionView>> result = new ArrayList<>();

        for (Instruction root : instructions) {

            List<List<Instruction>> paths = expandPaths(root, level, vlm);
            for (List<Instruction> path : paths) {
                int number = 1;

                List<InstructionView> views = new ArrayList<>(path.size());
                for (Instruction inst : path) {
                    views.add(toView(inst, number));
                    number++;
                }
                result.add(views);
            }
        }
        return result;
    }

    // expand Instructions to the given level and return InstructionView list
    @Override
    public List<InstructionView> instructionViewsAfterExtendRunShow(int level) {
        extend(level); // extend the program to the given level

        List<InstructionView> result = new ArrayList<>();

        int number = 1;
        for (Instruction inst : extendedInstructions) {
            result.add(toView(inst, number));
            number++;
        }
        return result;
    }

    @Override
    public List<List<String>> getInfo(int level) {
        extend(level); // extend the program to the given level
        List<List<String>> info = new ArrayList<>();
        for (Instruction inst : extendedInstructions) {
            List<String> currInfo = inst.getAllInfo();
            info.add(currInfo);
        }
        return info;
    }


    public Function clone () {
        List<Instruction> clonedInstructions = instructions
                .stream()
                .map(Instruction::clone)
                .collect(java.util.stream.Collectors.toList());;
        return new Function(name, userString,clonedInstructions);
    }
}

