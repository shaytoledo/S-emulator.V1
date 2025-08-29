package logic.execution;

import logic.variable.Variable;

import java.util.List;
import java.util.Map;

public interface ProgramExecutor {

    public long run(List<Long> inputs);
    Map<String, Long> variablesState();
    long getTotalCycles();

}
