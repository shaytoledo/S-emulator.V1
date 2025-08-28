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

    private static int intAfterFirstChar(String s) {
        if (s == null || s.length() < 2) {
            throw new IllegalArgumentException("String must have at least 2 characters");
        }
        String tail = s.substring(1).trim(); // Get substring after the first character and trim whitespace
        return Integer.parseInt(tail);
    }


    public EexecutionContextImpl(List<Long> inputs, List<String> varsNames) {
        // Initialize the variable state with the input values to the right variables by order
        variableState = new HashMap<>();
        if (inputs == null) {
            inputs = emptyList();
        }
        for (int i = 0; i < inputs.size(); i++) {
            Variable v = new VariableImpl(VariableType.INPUT, intAfterFirstChar(varsNames.get(i))); // Assuming a constructor that takes a name
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



