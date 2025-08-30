package ui;


import core.Engine;
import core.EngineImpl;
import dto.*;
import logic.exception.LoadProgramException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


public class ConsoleUI {

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
            LoadReport r = engine.loadProgram(Path.of(path));
            if (r.ok()) {
                System.out.println("Program loaded successfully.");
            }
            else {
                System.out.println("Failed to load program:");
                r.errors().forEach(e -> System.out.println(" - " + e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        } catch (Exception e) {
            System.out.println("Failed to load program: " + e.getMessage());
        }
    }

    private static void history() {
        List<RunSummary> history = engine.getHistory();
        ConsoleUtils.printRunSummaries(history);
    }


    private static void showProgram() {
        try {
            ProgramSummary summary = engine.getProgramSummaryForShow();
            System.out.println(summary);
        } catch (LoadProgramException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void runOnce() {
        try {
            int maxDegree = engine.getMaxExpandLevel();
            System.out.println("Max expansion degree available: " + maxDegree);
            System.out.print("Choose expansion degree (0.." + maxDegree + "): ");
            int degree = ConsoleUtils.readIntInRange(0, maxDegree);

            ProgramSummary summary = engine.getProgramSummaryForShow();

            // get inputs from user
            List<Long> inputs = ConsoleUtils.readInputs(summary);

            // Tell the engine to run the program with the chosen degree and inputs in fit to the names of variables
            RunResult res  = engine.run(degree,inputs,summary.getInputs());

            System.out.println();
            System.out.println("===== Program Executed (after expansion) =====");



            // func that run the program with the chosen degree and in run mode and change the instruction by the level
            List<InstructionView> extendInstructions = engine.expandProgramToLevelForRun(degree);
            printInstructionViews(extendInstructions);
            //showProgram();

            System.out.println();
            System.out.println("===== Formal Result =====");
            System.out.println("y = " + res.y());

            System.out.println("===== Final Variable Values =====");
            ConsoleUtils.printVariablesOrdered(res.variables());
            System.out.println("y = " + res.y());


            System.out.println("===== Cycles =====");
            System.out.println("Total cycles: " + res.totalCycles());


        } catch (LoadProgramException e) {
            System.out.println("No program is loaded. Please load a file to run a program.");
        }
    }


    private static void expand() {

        try {
            int maxDegree = engine.getMaxExpandLevel();
            System.out.println("Max expansion degree available: " + maxDegree);
            System.out.print("Choose expansion degree (0.." + maxDegree + "): ");
            int degree = ConsoleUtils.readIntInRange(0, maxDegree);

            // func that expand the program to the chosen degree
            List<List<InstructionView>> extendInstructions = engine.expandProgramToLevelForExtend(degree);

            // print the list of (list of instructions)
            printExtendedInstructions(extendInstructions);

        } catch (LoadProgramException e) {
            System.out.println("No program is loaded. Please load a file to expand a program.");
            return;
        }
    }

    public static void printExtendedInstructions(List<List<InstructionView>> extendInstructions) {
        for (List<InstructionView> chain : extendInstructions) {
            List<InstructionView> reversed = new ArrayList<>(chain);
            Collections.reverse(reversed);

            String line = reversed.stream()
                    .map(InstructionView::toString)
                    .collect(Collectors.joining(" >>> "));

            System.out.println(line);
        }
    }

    public static void printInstructionViews(List<InstructionView> instructions) {
        for (InstructionView instr : instructions) {
            System.out.println(instr);
        }
    }



}
