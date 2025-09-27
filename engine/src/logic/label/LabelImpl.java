package logic.label;

import java.util.Objects;

public class LabelImpl implements Label{

    private final String label;

    public LabelImpl(String _label) {label = _label;}

    public LabelImpl(int number) {
        label = "L" + number;
    }

    public String getLabelRepresentation() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LabelImpl label1 = (LabelImpl) o;
        return Objects.equals(label, label1.label);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(label);
    }
}