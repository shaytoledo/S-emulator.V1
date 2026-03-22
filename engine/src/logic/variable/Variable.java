package logic.variable;

public interface Variable {
    VariableType getType();
    int getIndex();
    String getRepresentation();

    Variable RESULT = new VariableImpl(VariableType.RESULT, 0);
}
