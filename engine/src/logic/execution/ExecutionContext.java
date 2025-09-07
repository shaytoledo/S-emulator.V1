package logic.execution;

import logic.variable.Variable;

import java.util.Map;

public interface ExecutionContext {

    public long getVariableValue(Variable v);
    public void updateVariable(Variable v, long value);
    Map<Variable, Long> getVariablesState();
    }
