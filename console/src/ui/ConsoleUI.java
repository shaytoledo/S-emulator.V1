package ui;


import core.Engine;
import core.EngineImpl;
import core.Engine.ProgramSummary;
import core.Engine.RunResult;
import core.Engine.RunSummary;
import logic.exception.NoProgramLoadedException;


import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class ConsoleUI {

    //ProgramExecutorImpl executor;
    private static final Scanner in = new Scanner(System.in);
    private static final Engine engine = new EngineImpl();

    public void run() {
        while (true) {
            printMenu();
            String choice = in.nextLine().trim();
            switch (choice) {
                case "1" -> loadXml();
                case "2" -> showProgram();
                case "3" -> expand();
                case "4" -> runOnce();
                case "5" -> history();
                case "6" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid option. Please choose 1-6.");
            }
            System.out.println();
        }
    }

    private static void printMenu() {
        System.out.println("""
                1) Load XML
                2) Show Program
                3) Expand
                4) Run
                5) History
                6) Exit
                """);
        System.out.print("Choose an option: ");
    }

    private static void loadXml() {
        System.out.print("Enter XML path: ");
        String path = in.nextLine().trim();

        if(path.isEmpty()) {
            System.out.println("Empty line entered, couldn't load file");
            return;
        }

        try {
            Engine.LoadReport r = engine.loadProgram(Path.of(path));
            if (r.ok()) {
                System.out.println("Program loaded successfully.");
            }
            else {
                System.out.println("Failed to load program:");
                r.errors().forEach(err -> System.out.println(" - " + err));
            }
        } catch (Exception e) {
            System.out.println("Failed to load program: " + e.getMessage());
        }
    }

    private static void showProgram() {
        try {
            ProgramSummary summary = engine.getProgramSummary();
            System.out.println(summary);
        } catch (NoProgramLoadedException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void runOnce() {
        try {
            ProgramSummary summary = engine.getProgramSummary();

            // need to implement
            int maxDegree = 0; // = getMaxExpansionDegree(loadedProgram);
            System.out.println("Max expansion degree available: " + maxDegree);
            System.out.print("Choose expansion degree (0.." + maxDegree + "): ");
            int degree = readIntInRange(0, maxDegree);

            // get inputs from user
            List<Long> inputs = readInputs(summary);
            // name of inputs by order
            RunResult res  = engine.run(degree,inputs,summary.getInputs());

            System.out.println();
            System.out.println("===== Program Executed (after expansion) =====");
            showProgram();

            System.out.println();
            System.out.println("===== Formal Result =====");
            System.out.println("y = " + res.y());

            System.out.println("===== Final Variable Values =====");
            printVariablesOrdered(res.variables());

            System.out.println("===== Cycles =====");
            System.out.println("Total cycles: " + res.totalCycles());


        } catch (NoProgramLoadedException e) {
            System.out.println("No program is loaded. Please load a file to run a program.");
            return;
        }
        return;
    }


    private static void printVariablesOrdered(Map<String, Long> variables) {
        if (variables == null || variables.isEmpty()) {
            System.out.println("(no variables to display)");
            return;
        }

        List<Map.Entry<String, Long>> xs = new ArrayList<>();
        List<Map.Entry<String, Long>> zs = new ArrayList<>();

        for (Map.Entry<String, Long> entry : variables.entrySet()) {
            String name = entry.getKey();
            if (name.equalsIgnoreCase("y")) {
                continue; // דילוג על y
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

    private static int readIntInRange(int min, int max) {
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

    private static List<Long> readInputs(ProgramSummary summary) {

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
                    // inputs.add(null);
                }
            }
        }

        return inputs;
    }






    private static void expand() { }


    private static void history() {
        List<Engine.RunSummary> history = engine.getHistory();
        printRunSummaries(history);


    }


    public static void printRunSummaries(List<RunSummary> summaries) {
        if (summaries == null || summaries.isEmpty()) {
            System.out.println("[no summaries]");
            return;
        }

        // כותרת
        System.out.printf("%-8s %-6s %-40s %-10s %-10s%n",
                "run#", "level", "inputs", "y", "cycles");
        System.out.println("=".repeat(78));

        for (RunSummary rs : summaries) {
            if (rs == null) {
                System.out.println("<null summary>");
                continue;
            }
            String inputsStr = fmtInputs(rs.inputs(), 40); // חותך אם ארוך
            System.out.printf("%-8d %-6d %-40s %-10d %-10d%n",
                    rs.runNumber(), rs.level(), inputsStr, rs.y(), rs.cycles());
        }
    }

    private static String fmtInputs(List<Long> inputs, int maxWidth) {
        String s = (inputs == null)
                ? "[]"
                : inputs.stream().map(String::valueOf).collect(Collectors.joining(", ", "[", "]"));
        return s.length() <= maxWidth ? s : s.substring(0, Math.max(0, maxWidth - 3)) + "...";
    }



}
