package logic.execution;

import core.program.Function;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

public class ExecutionContextImpl implements ExecutionContext {

    Map<Variable, Long> variableState;
    Map<String , Function> functions;


    public ExecutionContextImpl(List<Long> inputs, List<Function> functions) {
        // Initialize the variable state with the input values to the right variables by order
        variableState = new HashMap<Variable, Long>();
        if (inputs == null) {
            inputs = emptyList();
        }
        for (int i = 0; i < inputs.size(); i++) {
            Variable v = new VariableImpl(VariableType.INPUT, i + 1); // Assuming a constructor that takes a name
            variableState.put(v, inputs.get(i));
        }

        this.functions = new HashMap<>();
        for (Function f : functions) {
            this.functions.put(f.getName(), f);
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

    @Override
    public  Map<String , Function> getFunctions() {
        return functions;
    }
}



