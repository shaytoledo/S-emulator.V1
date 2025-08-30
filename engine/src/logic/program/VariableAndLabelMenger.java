package logic.program;

import logic.label.Label;
import logic.label.LabelImpl;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VariableAndLabelMenger {

    // all the used variable names and label names
    private final Set<String> usedVars;
    private final Set<String> usedLabels;

    //The counters start from the next largest index
    private int zCounter;
    private int lCounter;

    VariableAndLabelMenger(List<Variable> variables, List<Label> labels) {
        this.usedVars = variables.stream()
                .filter(v -> v != null && v.getRepresentation() != null)
                .map(Variable::getRepresentation)
                .collect(Collectors.toSet());

        this.usedLabels = labels.stream()
                .filter(l -> l != null && l.getLabelRepresentation() != null)
                .map(Label::getLabelRepresentation)
                .collect(Collectors.toSet());

        this.zCounter = nextIndexStartingFrom("z", usedVars);
        this.lCounter = nextIndexStartingFrom("L", usedLabels);
    }



    // find the largest index used with the given prefix, and return next
    static int nextIndexStartingFrom(String prefix, Set<String> used) {
        int max = 0;
        for (String s : used) {
            if (s != null && s.startsWith(prefix)) {
                try {
                    int n = Integer.parseInt(s.substring(prefix.length()));
                    if (n > max) max = n;
                } catch (NumberFormatException ignore) {}
            }
        }
        return max + 1;
    }


    public Variable newZVariable() {
        while (true) {
            final String name = "z" + zCounter;
            zCounter++;
            if (usedVars.add(name)) {
                return new VariableImpl(VariableType.WORK, zCounter - 1);
            }
        }
    }

    public Label newLabel() {
        while (true) {
            final String name = "L" + lCounter;
            lCounter++;
            if (usedLabels.add(name)) {
                return new LabelImpl(name);
            }
        }
    }


}
