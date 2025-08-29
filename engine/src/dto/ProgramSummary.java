package dto;

import java.util.List;

public record ProgramSummary(
        String name,
        List<String> inputs,
        List<String> labels,
        List<String> instructions,
        int maxDegree

        // List<InstructionView> instructions
) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Program name: ").append(name).append("\n");

        sb.append("Inputs: ");
        if (inputs == null || inputs.isEmpty()) {
            sb.append("None");
        } else {
            sb.append(String.join(", ", inputs));
        }
        sb.append("\n");

        sb.append("Labels: ");
        if (labels == null || labels.isEmpty()) {
            sb.append("None");
        } else {
            sb.append(String.join(", ", labels));
        }
        sb.append("\n");

        sb.append("Instructions:\n");
        if (instructions == null || instructions.isEmpty()) {
            sb.append("  None");
        } else {
            for (String instr : instructions) {
                sb.append("  ").append(instr).append("\n");
            }
        }

        return sb.toString();
    }


    public List<String> getInputs() {
        return inputs != null ? inputs : List.of();
    }

    public int getMaxDegree() {
        return maxDegree;
    }

}
