package adapter.translate;

import adapter.xml.gen.SInstructionArgument;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TranslatorHelper {


    public static Variable parseStrictVariable(String name) {

        char c0 = Character.toLowerCase(name.charAt(0));
        switch (c0) {
            case 'y':
                if (name.length() == 1) {
                    return Variable.RESULT;
                }

            case 'x': {
                int idx = parseStrictIndex(name, 1, name); // דורש ספרות בלבד אחרי X
                return new VariableImpl(VariableType.INPUT, idx);
            }

            case 'z': {
                int idx = parseStrictIndex(name, 1, name); // דורש ספרות בלבד אחרי Z
                return new VariableImpl(VariableType.WORK, idx); // אם יש אצלך WORK/INTERMEDIATE – תעדכן כאן
            }

            default:
                throw new IllegalArgumentException(
                        "Unsupported variable '" + name + "'. Only xN, zN, or y are allowed");

        }
    }

    private static int parseStrictIndex(String s, int start, String original) {
        if (s.length() <= start) {
            throw new IllegalArgumentException("Missing index for variable '" + original + "'");
        }
        int i = start;
        while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
        if (i != s.length()) {
            throw new IllegalArgumentException("Invalid characters after index in '" + original + "'");
        }
        try {
            int idx = Integer.parseInt(s.substring(start, i));
            if (idx < 1) throw new IllegalArgumentException("Index must be >= 1 in '" + original + "'");
            return idx;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric index for variable '" + original + "'");
        }
    }

    public static String safe(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    public String trimToNull(String s) {
        return (s == null) ? null : (s.trim().isEmpty() ? null : s.trim());
    }

    public static Map<String, String> argumentsMap(List<SInstructionArgument> arguments) {
        if (arguments == null) {
            return Map.of();
        }
        return arguments.stream()
                .collect(Collectors.toUnmodifiableMap(
                        SInstructionArgument::getName,
                        a -> safe(a.getValue(), "")
                ));
    }

    public static Variable newVarStrict(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Variable name cannot be null");
        }
        VariableType variableType;

        if (name.equals("y")) {
            variableType = VariableType.RESULT;
            return new VariableImpl(variableType, 1);

        } else if (name.startsWith("x")) {
            variableType = VariableType.INPUT;
            try {
                int number = Integer.parseInt(name.substring(1));
                return new VariableImpl(variableType, number);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Variable suffix must be a number");
            }
        } else {
            variableType = VariableType.WORK;
            try {
                int number = Integer.parseInt(name.substring(1));
                return new VariableImpl(variableType, number);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Variable suffix must be a number");
            }
        }


    }

    public static Map<String, String> nn(Map<String, String> m) {
        return (m == null) ? Map.of() : m;
    }

}
