package core.program;

import logic.label.Label;
import logic.label.LabelImpl;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;

import java.util.*;
import java.util.stream.Collectors;

public class VariableAndLabelMenger {

    // ---------- add these fields ----------
    private final Map<Variable, Variable> localVarMap = new HashMap<>();
    private final Map<Label, Label> localLabelMap = new HashMap<>();

    private final Deque<Map<Variable, Variable>> varMapStack = new ArrayDeque<>();
    private final Deque<Map<Label, Label>> labelMapStack = new ArrayDeque<>();



    // ---------- mapping API ----------
    public void mapVar(Variable from, Variable to) { localVarMap.put(from, to); }
    public Variable applyVar(Variable v) { return localVarMap.getOrDefault(v, v); }

    public void mapLabel(Label from, Label to) { localLabelMap.put(from, to); }
    public Label applyLabel(Label l) { return localLabelMap.getOrDefault(l, l); }

    public Map<Variable, Variable> getLocalVarMap() { return localVarMap; }
    public Map<Label, Label> getLocalLabelMap() { return localLabelMap; }


    // ---------- per-instruction local scope ----------
    public void beginLocalScope() {
        varMapStack.push(new HashMap<>(localVarMap));
        labelMapStack.push(new HashMap<>(localLabelMap));
        localVarMap.clear();
        localLabelMap.clear();
    }

    public void endLocalScope() {
        localVarMap.clear();
        localLabelMap.clear();
        if (!varMapStack.isEmpty()) localVarMap.putAll(varMapStack.pop());
        if (!labelMapStack.isEmpty()) localLabelMap.putAll(labelMapStack.pop());
    }

    public void clearLocal() {
        localVarMap.clear();
        localLabelMap.clear();
        varMapStack.clear();
        labelMapStack.clear();
    }











    // all the used variable names and label names
    private  Set<String> usedVars;
    private  Set<String> usedLabels;

    //The counters start from the next largest index
    private int zCounter;
    private int lCounter;

    VariableAndLabelMenger(List<Variable> variables, List<Label> labels) {
        zCounter = 0;
        lCounter = 0;
//        usedVars.clear();
//        usedLabels.clear();

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

    public VariableAndLabelMenger() {
        this.usedVars = new HashSet<>();
        this.usedLabels = new HashSet<>();
        this.zCounter = 0;
        this.lCounter = 0;

    }

    // find the largest index used with the given prefix, and return next
    private static int nextIndexStartingFrom(String prefix, Set<String> used) {
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

    public List<String> getAll() {
        List<String> all = new ArrayList<>();
        all.addAll(usedVars);
        all.addAll(usedLabels);
        return all;
    }

    public boolean existsVar(Variable v) {
        if (usedVars.contains(v.getRepresentation())) {
            return true;
        }
        return false;
    }

    public boolean existsLabel(Label L) {
        if (usedLabels.contains(L.getLabelRepresentation())) {
            return true;
        }
        return false;
    }
}




