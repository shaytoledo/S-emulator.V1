package logic.execution;

import logic.variable.Variable;

import java.util.Map;

public interface ExecutionContext {

    public long getVariableValue(String v);
    public void updateVariable(String v, long value);
    //Map<Variable, Long> getVariablesState();
    public Map<String, Long> getVariablesState();


    }
