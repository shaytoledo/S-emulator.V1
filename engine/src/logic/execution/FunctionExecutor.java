package logic.execution;

import core.program.Function;
import core.program.VariableAndLabelMenger;
import logic.instruction.Instruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

public class FunctionExecutor implements ProgramExecutor {

    private Function function;
    private ExecutionContext context;
    public int cycleCount = 0;
    public List<Function> functions;
    public int debugIndexCounter = 0;

public FunctionExecutor(Function function, List<Function> functions, ExecutionContext context) {
    this.functions = functions;
    this.function = function;
    this.context = context;

}

    public long run(List<Long> inputs, int cycels) {
        long res =  run(inputs);
        cycleCount = this.cycleCount;
        return res;
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

        context = new ExecutionContextImpl(safeInputs, functions); // create the context with inputs.
        Variable res = new VariableImpl(VariableType.RESULT, 1);
        context.updateVariable(res, 0); // initialize the result variable to 0.

        enterAllVariabalesInContext();

        // get the first instruction
        Instruction currentInstruction = function.getInstructions().isEmpty() ? null : function.getInstructions().getFirst();

        // if null, return result
        if (currentInstruction == null) {
            return context.getVariableValue(res);
        }

        Label nextLabel;
        do {
            nextLabel = currentInstruction.execute(context, new VariableAndLabelMenger());
            // sum cycles
            cycleCount += currentInstruction.cycles();

            if (isEmpty(nextLabel)) {
                currentInstruction = function.getNextInstructionLabel(currentInstruction);
                if (currentInstruction == null) {
                    nextLabel = FixedLabel.EXIT;
                }
            } else if (!isExit(nextLabel)) {
                currentInstruction = function.getInstructionByLabel(nextLabel);
            }
        } while (!isExit(nextLabel));

        // return result
        return context.getVariableValue(res);
    }

    @Override
    public Map<String, Long> variablesState() {
        return Map.of();
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
    if (function == null) return;
        List<Instruction> instructions = function.getInstructions();
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


}
