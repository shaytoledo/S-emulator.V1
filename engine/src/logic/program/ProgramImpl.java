package logic.program;

import logic.instruction.Instruction;
import logic.label.Label;
import logic.variable.Variable;

import java.util.*;

import static java.util.Comparator.comparingInt;

public class ProgramImpl implements Program {

    private final String name;
    private final List<Instruction> instructions;
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

        public Builder addLabels(Label... lbls) {
            if (lbls != null) {
                this.labels.addAll(Arrays.asList(lbls));
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
    public int calculateCycles() {
        return instructions.stream()
                .mapToInt(Instruction::cycles)
                .sum();
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
        Set<String> acc = new LinkedHashSet<>();

        for (Instruction instr : instructions) {
            Variable v = instr.getVariable();
            addIfStartsWithX(acc, v != null ? v.getRepresentation() : null);

            Map<String, String> args = instr.args();
            if (args != null && !args.isEmpty()) {
                for (String val : args.values()) {
                    addIfStartsWithX(acc, val);
                }
            }
        }

        return new ArrayList<>(acc);
    }

    private static void addIfStartsWithX(Set<String> acc, String s) {
        if (s == null) return;
        String t = s.trim();
        if (!t.isEmpty() && t.startsWith("x")) {
            acc.add(t);
        }
    }

    private void addIfX(Set<String> acc, String candidate) {
        if (candidate == null) return;
        String s = candidate.trim();
        if (s.isEmpty()) return;

        char c0 = s.charAt(0);
        if (c0 == 'x' || c0 == 'X') {
            for (int i = 1; i < s.length(); i++) {
                if (!Character.isDigit(s.charAt(i))) return; // לא מספרי → דילוג
            }
            acc.add(s.toLowerCase());
        }
    }

    private int safeTrailingNumber(String name) {
        if (name == null || name.length() < 2) return Integer.MAX_VALUE;
        try {
            return Integer.parseInt(name.substring(1));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

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

    @Override
    public List<String> getInstructionsPeek() {
        List<String> lines = new ArrayList<>(instructions.size());

        for (int i = 0; i < instructions.size(); i++) {
            Instruction inst = instructions.get(i);

            int number = i + 1;

            String type = inst.isBasic() ? "B" : "S";

            String lbl = (inst.getLabel() != null && inst.getLabel().getLabelRepresentation() != null)
                    ? inst.getLabel().getLabelRepresentation()
                    : "";
            String labelFormatted = String.format("[%1$-5s]", lbl);


            String command = inst.toDisplayString();

            command = command
                    .replaceAll("\\bX(\\d+)\\b", "x$1")
                    .replaceAll("\\bY\\b", "y");

            int cycles = inst.cycles();

            String line = String.format("#%d (%s) %s %s (%d)", number, type, labelFormatted, command, cycles);
            lines.add(line);
        }
        return lines;
    }



    // need to implement later
    @Override
    public boolean validate() {
        return false;
    }

    // need to implement later
    @Override
    public int calculateMaxDegree() {
        // traverse all commands and find maximum degree
        return 0;
    }





    public Variable getVariableByName(String name) {
        return variables.stream()
                .filter(v -> v.getRepresentation().equals(name))
                .findFirst()
                .orElse(null);
    }


}