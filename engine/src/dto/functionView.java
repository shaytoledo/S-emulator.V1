package dto;

import java.util.List;

public record functionView(
        String name,
        List<String> args,
        List<InstructionView> instructions
        ) {

        public functionView(String name, List<String> args, List<InstructionView> instructions) {
                this.name = name;
                this.args = List.copyOf(args);
                this.instructions = List.copyOf(instructions);
        }


        @Override
        public String toString() {
                return name;
        }

        public List<InstructionView> getInstructions() {
                return instructions;
        }

}
