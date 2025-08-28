package core;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;


// The engine API, determines which methods the engine has

public interface Engine {

    LoadReport loadProgram(Path xmlPath);
    ProgramSummary getProgramSummary();
    void expandToLevel(int level);
    RunResult run(int level, List<Long> inputs, List<String> varsNames);
    List<RunSummary> getHistory();

    //The reports about how the loading went and the errors if there were any
    record LoadReport(boolean ok, List<String> errors) {}

    record ProgramSummary(
            String name,
            List<String> inputs,
            List<String> labels,
            List<String> instructions

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

    }






    record InstructionView(
            int number,
            String typeBS,
            String label,
            String command,
            int cycles
    ) {}

    record RunResult(
            long y,
            Map<String, Long> variables,
            long totalCycles
    ) {}

    record RunSummary(
            int runNumber,
            int level,
            List<Long> inputs,
            long y,
            long cycles
    ) {}
}

