package ui;


import core.engine.Engine;
import core.engine.EngineImpl;
import dto.*;
import logic.exception.LoadProgramException;
import java.nio.file.Path;
import java.util.*;


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
        ConsoleUiHelper.printRunSummaries(history);
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
            int degree = ConsoleUiHelper.readIntInRange(0, maxDegree);

            ProgramSummary summary = engine.getProgramSummaryForShow();

            // get inputs from user
            List<Long> inputs = ConsoleUiHelper.readInputs(summary);

            // Tell the engine to run the program with the chosen degree and inputs in fit to the names of variables
            RunResult res  = engine.run(degree,inputs,summary.getInputs());

            System.out.println();
            System.out.println("===== Program Executed (after expansion) =====");



            // func that run the program with the chosen degree and in run mode and change the instruction by the level
            List<InstructionView> extendInstructions = engine.expandProgramToLevelForRun(degree);
            printInstructionViews(extendInstructions);

            System.out.println();
            System.out.println("===== Formal Result =====");
            System.out.println("y = " + res.y());

            System.out.println("===== Final Variable Values =====");
            ConsoleUiHelper.printVariablesOrdered(res.variables());
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
            int degree = ConsoleUiHelper.readIntInRange(0, maxDegree);

            // func that expand the program to the chosen degree
            List<List<InstructionView>> extendInstructions = engine.expandProgramToLevelForExtend(degree);

            // print the list of (list of instructions)
            printExpandedPaths(extendInstructions);

        } catch (LoadProgramException e) {
            System.out.println("No program is loaded. Please load a file to expand a program.");
        }
    }



    /**
     * Prints instruction chains, according to the following rules:
     * - Each chain is printed on a single line, with instructions separated by ">>> ".
     * - Lines are numbered sequentially (1, 2, 3, ...).
     * - Skips an entire chain if it contains at least one duplicate instruction
     *   (ignoring the internal numbering field in InstructionView).
     *   In other words: if the same instruction content appears more than once inside
     *   the same chain -> the whole chain is not printed.
     * - Does not perform deduplication between different chains
     *   (two different chains will both be printed even if similar).
     * - If the display should be reversed (leaf → root), we keep the reverse operation.
     */
    public static void printExpandedPaths(List<List<InstructionView>> extendInstructions) {
        if (extendInstructions == null || extendInstructions.isEmpty()) {
            return;
        }

        int lineNumber = 1;

        for (List<InstructionView> chain : extendInstructions) {
            if (chain == null || chain.isEmpty()) {
                continue; // Skip empty chains
            }

            // Reverse the chain so that leaf → root order is printed
            List<InstructionView> reversed = new ArrayList<>(chain);
            Collections.reverse(reversed);

            // Check for duplicates inside the chain:
            // If the same canonical key (type/label/command/cycles) appears more than once,
            // skip the entire chain.
            Set<String> seenInChain = new HashSet<>();
            boolean hasDuplicateInside = false;

            if (reversed.size() > 1) { // No need to check for duplicates if a chain has only one instruction
                for (InstructionView iv : reversed) {
                    if (iv == null) continue;
                    String key = canonicalKey(iv); // ignores internal numbering
                    if (!seenInChain.add(key)) {
                        // Found a duplicate → mark this chain as invalid
                        hasDuplicateInside = true;
                        break;
                    }
                }
            }


                if (hasDuplicateInside) {
                    // Skip the entire chain
                    continue;
                }


                // Build the output line with the original toString() (including internal numbers if any)
                String line = reversed.stream()
                        .map(InstructionView::toString)
                        .collect(java.util.stream.Collectors.joining(" >>> "));

                System.out.println(lineNumber + ") " + line);
                lineNumber++;

        }
    }

    /**
     * Builds a canonical key for an instruction, ignoring the internal numbering.
     * Two InstructionView objects with the same type, label, command, and cycles
     * will produce the same key regardless of numbering.
     */
    private static String canonicalKey(InstructionView iv) {
        String type    = String.valueOf(iv.type());
        String label   = String.valueOf(iv.label());
        String command = String.valueOf(iv.command());
        long cycles    = iv.cycles();
        // Use a rare separator character to avoid accidental collisions
        return type + "\u001F" + label + "\u001F" + command + "\u001F" + cycles;
    }

    public static void printInstructionViews(List<InstructionView> instructions) {
        for (InstructionView instr : instructions) {
            System.out.println(instr);
        }
    }



}
