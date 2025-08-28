package logic.variable;

import logic.label.Label;

public class VariableImpl implements Variable {

    private final VariableType type;
    private final int number;

    public VariableImpl(VariableType type, int number) {
        this.type = type;
        this.number = number;
    }

    @Override
    public VariableType getType() {
        return type;
    }

    @Override
    public String getRepresentation() {
        return type.getVariableRepresentation(number);
    }

    @Override
    public String toString() {
        return type.toString() + number;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Label other)) return false;
        return java.util.Objects.equals(
                this.getRepresentation(),
                other.getLabelRepresentation()
        );
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(getRepresentation());
    }

}
