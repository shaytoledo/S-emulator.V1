package core.program;

import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.instruction.Instruction;
import logic.label.Label;

import java.util.ArrayList;
import java.util.List;

public class Function {

    private String name;
    private String userString;
    private List<Instruction> instructions;
    private List<Instruction> extendedInstructions;


    public Function(String name, String userString, List<Instruction> instructions) {
        this.name = name;
        this.userString = userString;
        this.instructions = instructions;
        this.extendedInstructions = new ArrayList<>(instructions);
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public List<Instruction> getExtendedInstructions() {
        return extendedInstructions;
    }
    public String getUserString() {
        return userString;
    }
    public String getName() {
        return name;
    }

    public long evaluate() {
        long result = 0;

        return result;

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



}
