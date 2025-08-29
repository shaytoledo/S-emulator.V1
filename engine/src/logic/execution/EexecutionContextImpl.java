package logic.execution;

import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Collections.emptyList;

public class EexecutionContextImpl implements ExecutionContext {

    Map<String, Long> variableState;
    //Map<Variable, Long> variableState;


    public EexecutionContextImpl(List<Long> inputs) {
        // Initialize the variable state with the input values to the right variables by order
        variableState = new HashMap<>();
        if (inputs == null) {
            inputs = emptyList();
        }
        for (int i = 0; i < inputs.size(); i++) {
            Variable v = new VariableImpl(VariableType.INPUT, i + 1); // Assuming a constructor that takes a name
            variableState.put(v.getRepresentation(), inputs.get(i));
        }
    }

    private static String keyOf(String v) {
        return v.toString().toLowerCase(Locale.ROOT);
    }


    @Override
    public long getVariableValue(String v) {
        Long val = variableState.get(v);
        if (val == null) {
            variableState.put(v, 0L); // init-on-read
            return 0L;
        }
        return val;
    }

    @Override
    public void updateVariable(String v, long value) {
        variableState.put(v, value);
    }



    @Override
    public Map<String, Long> getVariablesState() {
        return variableState;
    }
}



