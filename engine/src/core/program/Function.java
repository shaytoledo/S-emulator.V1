package core.program;

import logic.instruction.Instruction;

import java.util.List;

public class Function {

    protected String name;
    protected String userString;
    private List<Instruction> instructions;


    public Function(String name, String userString, List<Instruction> instructions) {
        this.name = name;
        this.userString = userString;
        this.instructions = instructions;
    }

    public List<Instruction> getSInstructions() {
        return instructions;
    }
    public String getUserString() {
        return userString;
    }
    public String getName() {
        return name;
    }

}
