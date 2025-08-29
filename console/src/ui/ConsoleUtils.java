package ui;

import dto.ProgramSummary;
import dto.RunSummary;

import java.util.*;

import static java.util.Collections.emptyList;

public class ConsoleUtils {

    private static final Scanner in = new Scanner(System.in);

    public static void printVariablesOrdered(Map<String, Long> variables) {
        if (variables == null || variables.isEmpty()) {
            System.out.println("(no variables to display)");
            return;
        }

        List<Map.Entry<String, Long>> xs = new ArrayList<>();
        List<Map.Entry<String, Long>> zs = new ArrayList<>();

        for (Map.Entry<String, Long> entry : variables.entrySet()) {
            String name = entry.getKey();
            if (name.equalsIgnoreCase("y")) {
                continue;
            }
            if (isX(name)) {
                xs.add(entry);
            } else if (isZ(name)) {
                zs.add(entry);
            }
        }

        Comparator<Map.Entry<String, Long>> byIndex =
                Comparator.comparingInt(e -> extractTrailingNumberSafe(e.getKey()));

        xs.sort(byIndex);
        zs.sort(byIndex);

        for (Map.Entry<String, Long> e : xs) {
            System.out.println(e.getKey() + " = " + e.getValue());
        }
        for (Map.Entry<String, Long> e : zs) {
            System.out.println(e.getKey() + " = " + e.getValue());
        }
    }

    private static boolean isX(String name) {
        return name != null && name.matches("[xX]\\d+");
    }

    private static boolean isZ(String name) {
        return name != null && name.matches("[zZ]\\d+");
    }

    private static int extractTrailingNumberSafe(String name) {
        if (name == null) return Integer.MAX_VALUE;
        int i = name.length() - 1;
        while (i >= 0 && Character.isDigit(name.charAt(i))) i--;
        if (i == name.length() - 1) return Integer.MAX_VALUE;
        try {
            return Integer.parseInt(name.substring(i + 1));
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    public static int readIntInRange(int min, int max) {
        while (true) {
            String line = in.nextLine().trim();
            try {
                int v = Integer.parseInt(line);
                if (v < min || v > max) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                System.out.print("Please enter an integer in [" + min + "," + max + "]: ");
            }
        }
    }

    public static List<Long> readInputs(ProgramSummary summary) {

        List<String> inputVars;
        try {
            inputVars = summary.getInputs();
        } catch (Throwable t) {
            inputVars = emptyList();
        }


        if (inputVars.isEmpty()) {
            System.out.println("The program does not declare input variables. " +
                    "You may still provide numbers; unused values will be ignored.");
        } else {
            System.out.println("Program input variables: " + String.join(", ", inputVars));
        }

        System.out.print("Enter inputs (comma-separated): ");
        String line = in.nextLine().trim();

        List<Long> inputs = new ArrayList<>();

        if (!line.isEmpty()) {
            String[] parts = line.split("\\s*,\\s*");
            for (int i = 0; i < parts.length; i++) {
                try {
                    inputs.add(Long.parseLong(parts[i]));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input at position " + (i + 1) +
                            ": '" + parts[i] + "' is not a valid number.");
                }
            }
        }

        return inputs;
    }

    public static void printRunSummaries(List<RunSummary> summaries) {
        if (summaries == null || summaries.isEmpty()) {
            System.out.println("[The program has not yet been launched.]");
            return;
        }

        System.out.println("Run summaries:");
        System.out.println("=".repeat(40));

        for (RunSummary rs : summaries) {
            StringBuilder inputsStr = new StringBuilder();
            for (int i = 0; i < rs.inputs().size(); i++) {
                inputsStr.append("X").append(i + 1).append("=").append(rs.inputs().get(i));
                if (i < rs.inputs().size() - 1) {
                    inputsStr.append(" ");
                }
            }

            System.out.println("Run #: " + rs.runNumber());
            System.out.println("Level: " + rs.level());
            System.out.println("Inputs: " + inputsStr);
            System.out.println("Y: " + rs.y());
            System.out.println("Cycles: " + rs.cycles());
            System.out.println("-".repeat(40));
        }
    }


}
