package logic.execution;

import java.util.List;
import java.util.Map;

public interface ProgramExecutor {

    long run(List<Long> inputs);
    Map<String, Long> variablesState();
}
