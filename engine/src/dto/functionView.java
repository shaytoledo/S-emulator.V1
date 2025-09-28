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
                StringBuilder str = new StringBuilder();
                str.append("(").append(name).append(",");
                for (String arg : args) {
                        str.append(arg).append(",");
                }
                str.deleteCharAt(str.length() - 1);
                str.append(")");
                return str.toString();
        }

        public List<InstructionView> getInstructions() {
                return instructions;
        }

}
