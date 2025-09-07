package core.program;

import dto.InstructionView;
import logic.instruction.Instruction;
import logic.instruction.synthetic.AssignmentInstruction;
import logic.instruction.synthetic.JumpEqualVariableInstruction;
import logic.label.Label;
import logic.variable.Variable;

import java.util.*;


public class ProgramImpl implements Program {

    private final String name;
    private List<Instruction> instructions;
    private final List<Variable> variables;
    private final List<Label> labels;
    private final Label exitLabel;


    public static final class Builder {
        private String name;
        private final List<Instruction> instructions = new ArrayList<>();
        private final List<Variable> variables = new ArrayList<>();
        private final List<Label> labels = new ArrayList<>();
        private Label exitLabel;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        //------Instructions------
        public Builder withInstructions(Collection<? extends Instruction> newInstructions) {
            this.instructions.clear();
            if (newInstructions != null) {
                this.instructions.addAll(newInstructions);
            }
            return this;
        }

        public Builder addInstruction(Instruction instruction) {
            if (instruction != null) {
                this.instructions.add(instruction);
            }
            return this;
        }

        public Builder addInstructions(Instruction... instruction) {
            if (instruction != null) {
                this.instructions.addAll(Arrays.asList(instruction));
            }
            return this;
        }

        //------Variables------
        public Builder withVariables(Collection<? extends Variable> newVariables) {
            this.variables.clear();
            if (newVariables != null) {
                this.variables.addAll(newVariables);
            }
            return this;
        }

        public Builder addVariable(Variable variable) {
            if (variable != null) {
                this.variables.add(variable);
            }
            return this;
        }

        public Builder addVariables(Variable... vars) {
            if (vars != null) {
                this.variables.addAll(Arrays.asList(vars));
            }
            return this;
        }

        //-----Labels------
        public Builder withLabels(Collection<? extends Label> newLabels) {
            this.labels.clear();
            if (newLabels != null) {
                this.labels.addAll(newLabels);
            }
            return this;
        }

        public Builder addLabel(Label label) {
            if (label != null) {
                this.labels.add(label);
            }
            return this;
        }

        public Builder addLabels(Label... lbs) {
            if (lbs != null) {
                this.labels.addAll(Arrays.asList(lbs));
            }
            return this;
        }

        public Builder withExitLabel(Label exitLabel) {
            this.exitLabel = exitLabel;
            return this;
        }


        public ProgramImpl build() {
            return new ProgramImpl(this);
        }

    }

    public static Builder from(Program program) {
        Builder b = new Builder();
        if (program == null) return b;
        b.withName(program.getName());
        b.withInstructions(program.getInstructions());
        b.withVariables(program.getVariables());
        b.withLabels(program.getLabels());
        b.withExitLabel(program.getExitLabel());
        return b;
    }

    private ProgramImpl(Builder builder) {
        this.name = (builder.name == null || builder.name.isBlank()) ? "Unnamed Program" : builder.name;

        this.instructions = List.copyOf(builder.instructions);

        //Add all variables to map (from X1 to the real variable)
        Map<String, Variable> variablesByName = new LinkedHashMap<>();
        for (Variable variable : builder.variables) {
            if (variable != null && variable.getRepresentation() != null) {
                variablesByName.put(variable.getRepresentation(), variable);
            }
        }
        this.variables = List.copyOf(variablesByName.values());

        //Add all labels to map (from L1 to the real label)
        Map<String, Label> labelsByName = new LinkedHashMap<>();
        for (Label label : builder.labels) {
            if (label != null && label.getLabelRepresentation() != null) {
                labelsByName.put(label.getLabelRepresentation(), label);
            }
        }
        this.labels = List.copyOf(labelsByName.values());

        this.exitLabel = builder.exitLabel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addInstruction(Instruction instruction) { instructions.add(instruction); }

    @Override
    public List<Instruction> getInstructions() {
        return instructions;
    }

    @Override
    public List<Label> getLabels() {
        return labels;
    }

    @Override
    public Label getExitLabel() {
        return exitLabel;
    }

    @Override
    public List<Variable> getVariables() {
        return variables;
    }

    @Override
    public Instruction getNextInstructionLabel(Instruction currentInstruction) {
        int currentIndex = instructions.indexOf(currentInstruction);
        if (currentIndex >= 0 && currentIndex + 1 < instructions.size()) {
            return instructions.get(currentIndex + 1);
        }
        else {
            return null;
        }
    }

    @Override
    public Instruction getInstructionByLabel(Label nextLabel) {
        for (Instruction instruction : instructions) {
            if(instruction.getLabel() != null) {
                if (instruction.getLabel().equals(nextLabel)) {
                    return instruction;
                }
            }

        }
        //It always finds the instruction by label because in load program it checks the label jumps
        return null; // or throw an exception if label not found

    }

    @Override
    public List<String> getVariablesPeek() {
        Set<String> variables = new LinkedHashSet<>();

        for (Instruction instr : instructions) {
            Variable v = instr.getVariable();
            addIfStartsWithX(variables, v != null ? v.getRepresentation() : null);

            if (instr instanceof AssignmentInstruction) {
                addIfStartsWithX(variables, ((AssignmentInstruction) instr).getAssignedVariable().getRepresentation());
            } else if (instr instanceof JumpEqualVariableInstruction) {
                addIfStartsWithX(variables, ((JumpEqualVariableInstruction) instr).getOther().getRepresentation());
            }
        }

        return new ArrayList<>(variables);
    }

    private static void addIfStartsWithX(Set<String> acc, String s) {
        if (s == null) return;
        String t = s.trim();
        if (t.startsWith("x")) {
            acc.add(t);
        }
    }

    // convert labels to a String list for display in table
    @Override
    public List<String> getLabelsPeek() {
        return labels.stream()
                .map(Label::getLabelRepresentation)
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .distinct()
                .sorted((a, b) -> {
                    if (a.equals("EXIT")) return 1; // EXIT in the end
                    if (b.equals("EXIT")) return -1;
                    try {
                        return Integer.compare(
                                Integer.parseInt(a.substring(1)),
                                Integer.parseInt(b.substring(1))
                        );
                    } catch (NumberFormatException e) {
                        return a.compareTo(b);
                    }
                })
                .toList();
    }


//     expand Instructions to the given level (without the original instructions) fit fo run extend
//     notice: this method replaces the original instructions with the expanded ones



    @Override
    public List<List<InstructionView>> expendToLevelForExtend(int level) {
    VariableAndLabelMenger vlm = new VariableAndLabelMenger(variables, labels);

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

    // expand Instructions to the given level (with the original instructions) fit fo extend command
    // notice: this method add to the original instruction the expanded ones
    // return a list of (list of InstructionView) for display in table
    @Override
    public List<List<InstructionView>> expendToLevelForRun(int level) {
        VariableAndLabelMenger vlm = new VariableAndLabelMenger(variables, labels);
        List<List<InstructionView>> result = new ArrayList<>();

        int number = 1;
        for (Instruction inst : instructions) {
            List<Instruction> curExtendInstruction = new ArrayList<>(inst.extend(level, vlm));

            List<InstructionView> views = new ArrayList<>(curExtendInstruction.size());
            for (Instruction e : curExtendInstruction) {
                views.add(toView(e, number));
                number++;
            }

            result.add(views);
        }

        return result;
    }

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
    @Override
    public List<InstructionView> getInstructionsPeek() {
        List<InstructionView> instructionViews = new ArrayList<>(instructions.size());

        int size = instructions.size();

        for (int i = 0; i < size; i++) {
            Instruction inst = instructions.get(i);

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
}