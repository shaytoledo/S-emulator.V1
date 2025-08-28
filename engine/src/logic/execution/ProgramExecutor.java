package logic.execution;

import logic.variable.Variable;

import java.util.List;
import java.util.Map;

public interface ProgramExecutor {

    long run(List<Long> inputs, List<String> varsNames);
    Map<String, Long> variablesState();
    long getTotalCycles();

}
