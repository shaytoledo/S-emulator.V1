package logic.execution;

import logic.instruction.Instruction;
import logic.label.FixedLabel;
import logic.label.Label;
import core.program.Program;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;

import java.util.*;
import java.util.stream.Collectors;
import static java.util.Collections.emptyList;

public class ProgramExecutorImpl implements ProgramExecutor {

    private final Program program;
    private ExecutionContext context;


    public ProgramExecutorImpl(Program program) {
        this.program = program;
    }

    @Override
    public long run(List<Long> inputs) {

        final List<Long> safeInputs;
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

        Instruction currentInstruction = program.getInstructions().isEmpty()
                ? null
                : program.getInstructions().getFirst();

        if (currentInstruction == null) {
            return context.getVariableValue(res);
        }

        Label nextLabel;
        do {
            nextLabel = currentInstruction.execute(context);

            if (isEmpty(nextLabel)) {
                currentInstruction = program.getNextInstructionLabel(currentInstruction);
                if (currentInstruction == null) {
                    nextLabel = FixedLabel.EXIT;
                }
            } else if (!isExit(nextLabel)) {
                currentInstruction = program.getInstructionByLabel(nextLabel);

            }
        } while (!isExit(nextLabel));


        return context.getVariableValue(res);


    }

    @Override
    public Map<String, Long> variablesState() {

        Map<String, Long> onlyXSorted = context.getVariablesState().entrySet().stream()
                .filter(e -> e.getKey() != null && e.getKey().getRepresentation().startsWith("x"))
                .sorted(Comparator.comparingInt(e -> Integer.parseInt(e.getKey().getRepresentation().substring(1))))
                .collect(Collectors.toMap(e -> e.getKey().getRepresentation(),
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        return onlyXSorted;
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
}
