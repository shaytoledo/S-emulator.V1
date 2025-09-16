package logic.execution;

import core.program.Program;
import logic.instruction.Instruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class ProgramExecutorImpl implements ProgramExecutor {

    private Program program;
    private ExecutionContext context;
    public int cycleCount = 0;
    public int debugIndexCounter = 0;


    public ProgramExecutorImpl(Program program) {
        this.program = program;
    }

    @Override
    public long run(List<Long> inputs) {

        // safe inputs
        List<Long> safeInputs;
        if (inputs == null || inputs.isEmpty()) {
            safeInputs = emptyList();
        } else {
            safeInputs = new ArrayList<>(inputs.size());
            for (Long v : inputs) {
                safeInputs.add(v != null ? v : 0L);
            }
        }

        context = new ExecutionContextImpl(safeInputs); // create the context with inputs.
        Variable res = new VariableImpl(VariableType.RESULT, 1);
        context.updateVariable(res, 0); // initialize the result variable to 0.

        enterAllVariabalesInContext();

        // get the first instruction
        Instruction currentInstruction = program.getInstructions().isEmpty() ? null : program.getInstructions().getFirst();

        // if null, return result
        if (currentInstruction == null) {
            return context.getVariableValue(res);
        }

        Label nextLabel;
        do {
            nextLabel = currentInstruction.execute(context);
            // sum cycles
            cycleCount += currentInstruction.cycles();

            if (isEmpty(nextLabel)) {
                currentInstruction = program.getNextInstructionLabel(currentInstruction);
                if (currentInstruction == null) {
                    nextLabel = FixedLabel.EXIT;
                }
            } else if (!isExit(nextLabel)) {
                currentInstruction = program.getInstructionByLabel(nextLabel);

            }
        } while (!isExit(nextLabel));

        // return result
        return context.getVariableValue(res);
    }

    @Override
    public Map<String, Long> variablesState() {
        Map<String, Long> allVariables = context.getVariablesState().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().getRepresentation(),Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new
                ));
        return allVariables;
    }

    private static boolean isExit(Label l) {
        if (l == null) return false;
        if (l == FixedLabel.EXIT) return true;
        String rep = l.getLabelRepresentation();
        return rep != null && rep.equalsIgnoreCase("EXIT");
    }

    private static boolean isEmpty(Label l) {
        if (l == null) return false;
        if (l == FixedLabel.EMPTY) return true;
        String rep = l.getLabelRepresentation();
        return rep != null && rep.equalsIgnoreCase("EMPTY");
    }

    private void enterAllVariabalesInContext() {
        List<Instruction> instructions = program.getInstructions();
        for (Instruction instruction : instructions) {
            List<String> infos = instruction.getAllInfo();
            for (String info : infos) {
                if (info != null && info.startsWith("z")) {
                    VariableImpl var = new VariableImpl(VariableType.WORK, Integer.parseInt(info.substring(1)));
                    context.updateVariable(var,0L);
                }
            }

        }
    }

    public void init(List<Long> inputs) {
        // safe inputs
        List<Long> safeInputs;
        if (inputs == null || inputs.isEmpty()) {
            safeInputs = emptyList();
        } else {
            safeInputs = new ArrayList<>(inputs.size());
            for (Long v : inputs) {
                safeInputs.add(v != null ? v : 0L);
            }
        }
        context = new ExecutionContextImpl(inputs); // create the context with inputs.

        Variable res = new VariableImpl(VariableType.RESULT, 1);
        context.updateVariable(res, 0); // initialize the result variable to 0.

        enterAllVariabalesInContext();
    }

    public int runOneStep() {
        // get the first instruction
        Instruction currentInstruction = program.getInstructions().get(debugIndexCounter);

        Label nextLabel;

        // get the instruction next label if take you there
        nextLabel = currentInstruction.execute(context);

        // sum cycles
        cycleCount += currentInstruction.cycles();

        // if not take you there, go to the next instruction
        if (isEmpty(nextLabel)) {
            currentInstruction = program.getNextInstructionLabel(currentInstruction);
            debugIndexCounter = program.getInstructions().indexOf(currentInstruction);
            // return the next index of the next instruction
            return debugIndexCounter;

            // if take you there and there is no exit, go to that instruction
        } else if (!isExit(nextLabel)) {
            currentInstruction = program.getInstructionByLabel(nextLabel);
            debugIndexCounter = program.getInstructions().indexOf(currentInstruction);
            // return the index of the instruction that it took you to
            return debugIndexCounter;
        }
        else { // if (isExit(nextLabel))
            // -1 signifies the end of the program
            debugIndexCounter = -1;
            return debugIndexCounter;
        }
    }

    public void resume() {
        Instruction currentInstruction = program.getInstructions().get(debugIndexCounter);

        // if null, return result
        if (currentInstruction == null) {
            return;
        }

        Label nextLabel;
        do {
            nextLabel = currentInstruction.execute(context);
            // sum cycles
            cycleCount += currentInstruction.cycles();

            if (isEmpty(nextLabel)) {
                currentInstruction = program.getNextInstructionLabel(currentInstruction);
                if (currentInstruction == null) {
                    nextLabel = FixedLabel.EXIT;
                }
            } else if (!isExit(nextLabel)) {
                currentInstruction = program.getInstructionByLabel(nextLabel);

            }
        } while (!isExit(nextLabel));

        // end of program
        return;
    }
}
