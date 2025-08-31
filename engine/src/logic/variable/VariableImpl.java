package logic.variable;

import java.util.Objects;

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
        if (o == null || getClass() != o.getClass()) return false;
        VariableImpl variable = (VariableImpl) o;
        return number == variable.number && type == variable.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, number);
    }
}
