package core.program;

import dto.InstructionView;
import dto.RunSummary;
import dto.functionView;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.instruction.Instruction;
import logic.instruction.synthetic.QuoteInstruction;
import logic.label.Label;

import java.util.ArrayList;
import java.util.List;

public class Function {

    private String name;
    private String userString;
    private List<Instruction> instructions;
    private List<Instruction> extendedInstructions;
    private List<String> args;
    public List<RunSummary> summaries = new ArrayList<>();


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
}

