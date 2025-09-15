package logic.execution;

import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

public class ExecutionContextImpl implements ExecutionContext {

    Map<Variable, Long> variableState;


    public ExecutionContextImpl(List<Long> inputs) {
        // Initialize the variable state with the input values to the right variables by order
        variableState = new HashMap<Variable, Long>();
        if (inputs == null) {
            inputs = emptyList();
        }
        for (int i = 0; i < inputs.size(); i++) {
            Variable v = new VariableImpl(VariableType.INPUT, i + 1); // Assuming a constructor that takes a name
            variableState.put(v, inputs.get(i));
        }
    }

    @Override
    public long getVariableValue(Variable v) {
        Long val = variableState.get(v);
        if (val == null) {
            variableState.put(v, 0L); // init-on-read
            return 0L;
        }
        return val;
    }

    @Override
    public void updateVariable(Variable v, long value) {
        variableState.put(v, value);
    }

    @Override
    public Map<Variable, Long> getVariablesState() {
        return variableState;
    }
}



