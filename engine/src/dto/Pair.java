package dto;

public record Pair<A, B>(A key, B value) {
    /** Compatibility alias for {@link #key()} — mirrors javafx.util.Pair API. */
    public A getKey()   { return key; }
    /** Compatibility alias for {@link #value()} — mirrors javafx.util.Pair API. */
    public B getValue() { return value; }
}
