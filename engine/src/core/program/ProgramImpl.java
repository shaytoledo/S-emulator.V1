package core.program;

import dto.InstructionView;
import logic.instruction.Instruction;
import logic.label.Label;
import logic.variable.Variable;

import java.util.*;


public class ProgramImpl implements Program {

    private final String name;
    private List<Function> functions;
    private List<Instruction> instructions;
    private List<Instruction> extendedInstructions;
    private final List<Variable> variables;
    private final List<Label> labels;

    public VariableAndLabelMenger vlm;

    public ProgramImpl(String programName, List<Instruction> instructions,List<Function> funcs, Map<String, Variable> varsByName, Map<String, Label> labelsByName) {
        this.name = programName;
        this.instructions = new ArrayList<>(instructions);
        this.variables = new ArrayList<>(varsByName.values());
        this.labels = new ArrayList<>(labelsByName.values());
        this.extendedInstructions = new ArrayList<>(instructions);
        this.functions = new ArrayList<>(funcs);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Instruction> getInstructions() {
        return extendedInstructions;
    }

    // return the next instruction after the current one, or null if at the end
    @Override
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
    @Override
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

    // convert variables to a String list for display in table
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

        return new ArrayList<>(variables);
    }

    private static void addIfStartsWithX(Set<String> variables, String curr) {
        if (curr == null) return;
        String t = curr.trim().toLowerCase();
        if (t.startsWith("x")) {
            variables.add(t);
        }
    }

    // convert labels to a String list for display in table
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

        return uniqueInfo;
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

    // convert Instruction to InstructionView for display in table
    private static InstructionView toView(Instruction ins, int index) {
        int number = index;
        String type = ins.isBasic() ? "B" : "S";
        String label = (ins.getLabel() == null)
                ? ""
                : ins.getLabel().getLabelRepresentation();
        String command = ins.toDisplayString();
        int cycles = ins.cycles();

        return new InstructionView(number, type, label, command, cycles);
    }

    // convert instructions to InstructionView list for display in table
    // note that this method use the extended instructions
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
            int cycles = inst.cycles();

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

    @Override
    public int calculateMaxDegree() {
        int max = 0;
        for (Instruction inst : instructions) {
            int level = inst.getMaxLevel();
            if (level > max) {
                max = level;
            }
        }
        return max;
    }

    // expand Instructions to the given level (with the original instructions) fit fo run extend
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
    public List<List<String>> getInfo(int level) {
        extend(level); // extend the program to the given level
        List<List<String>> info = new ArrayList<>();
        for (Instruction inst : extendedInstructions) {
            List<String> currInfo = inst.getAllInfo();
            info.add(currInfo);
        }
        return info;
    }
}