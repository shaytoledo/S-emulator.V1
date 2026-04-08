package application.model;

import java.util.Set;

/**
 * Architecture tiers — each successive generation includes all prior instructions.
 * Cost in credits is charged once per execution start.
 * Each cycle consumed during execution also costs 1 credit.
 */
public enum Architecture {

    I(5, Set.of(
            "NO_OP", "INCREASE", "DECREASE", "JNZ"
    )),
    II(100, Set.of(
            "NO_OP", "INCREASE", "DECREASE", "JNZ",
            "ZERO_VARIABLE", "CONSTANT_ASSIGNMENT", "GOTO_LABEL"
    )),
    III(500, Set.of(
            "NO_OP", "INCREASE", "DECREASE", "JNZ",
            "ZERO_VARIABLE", "CONSTANT_ASSIGNMENT", "GOTO_LABEL",
            "ASSIGNMENT", "JUMP_ZERO", "JUMP_EQUAL_CONSTANT", "JUMP_EQUAL_VARIABLE"
    )),
    IV(1000, Set.of(
            "NO_OP", "INCREASE", "DECREASE", "JNZ",
            "ZERO_VARIABLE", "CONSTANT_ASSIGNMENT", "GOTO_LABEL",
            "ASSIGNMENT", "JUMP_ZERO", "JUMP_EQUAL_CONSTANT", "JUMP_EQUAL_VARIABLE",
            "QUOTE", "JUMP_EQUAL_FUNCTION"
    ));

    public final int cost;
    public final Set<String> supportedCommands;

    Architecture(int cost, Set<String> supportedCommands) {
        this.cost = cost;
        this.supportedCommands = supportedCommands;
    }

    public boolean supports(String commandName) {
        return supportedCommands.contains(commandName);
    }

    /**
     * Computes the minimum architecture required to run a program given
     * a list of instruction names (from {@code Instruction.getName()}).
     * Returns the highest-tier architecture that any single instruction demands.
     */
    public static Architecture computeMinFor(java.util.List<String> instrNames) {
        Architecture min = I;
        for (String name : instrNames) {
            if (name == null) continue;
            for (Architecture a : values()) { // ordered I → IV
                if (a.supports(name)) {
                    if (a.ordinal() > min.ordinal()) min = a;
                    break; // found lowest arch that supports this instruction
                }
            }
        }
        return min;
    }

    public static Architecture fromString(String s) {
        return switch (s.toUpperCase()) {
            case "I"   -> I;
            case "II"  -> II;
            case "III" -> III;
            case "IV"  -> IV;
            default -> throw new IllegalArgumentException("Unknown architecture: " + s);
        };
    }
}
