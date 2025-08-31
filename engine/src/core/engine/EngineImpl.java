package core.engine;

import dto.*;
import adapter.translate.JaxbLoader;
import adapter.translate.ProgramTranslator;
import logic.exception.LoadProgramException;
import logic.exception.NotXMLException;
import logic.exception.ProgramFileNotFoundException;
import logic.execution.ProgramExecutorImpl;
import core.program.Program;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// implementation of the Engine interface (methods of the engine)
public class EngineImpl implements Engine {

    private static Program cuurentProgram = null;
    private final List<RunResult> history = new ArrayList<>();
    private int runCounter = 0;

    public List<RunSummary> summaries = new ArrayList<>();


    // Use EngineJaxbLoader to load the XML file and ProgramTranslator to translate it to internal representation
    @Override
    public LoadReport loadProgram(Path xmlPath) {
        List<Exception> errors = new ArrayList<>();
        try {
            try {
                String fileName = xmlPath.getFileName().toString().toLowerCase();
                if (!fileName.endsWith(".xml")) {
                    throw new NotXMLException("Invalid extension (must be .xml): " + fileName);
                }
            } catch (NotXMLException e) {
                errors.add(e);
            }
            try {
                String fileName = xmlPath.getFileName().toString().toLowerCase();
                if (!Files.exists(xmlPath) || !Files.isRegularFile(xmlPath)) {
                    throw new ProgramFileNotFoundException("File does not exist: " + xmlPath);
                }
            } catch (ProgramFileNotFoundException e) {
                errors.add(e);
            }

            if (!errors.isEmpty()) {
                return new LoadReport(false, errors);
            }

            // Load the XML file into SProgram representation
            var sprogram = JaxbLoader.load(xmlPath);


            //Translate the loaded SProgram to internal Program representation
            var cuurentProgram = ProgramTranslator.translate(sprogram);

            // If there are errors during translation, return them in the LoadReport (errors that define in the specification document)
            if (!cuurentProgram.errors.isEmpty()) {
                // return the errors
                return new LoadReport(false, cuurentProgram.errors);
            }

            history.clear();
            ;
            this.cuurentProgram = cuurentProgram.program;
            return new LoadReport(true, List.of());

        } catch (Exception e) {
            return new LoadReport(false, List.of(e));
        }
    }

    @Override
    public ProgramSummary getProgramSummaryForShow() {

        // there isn't valid load program
        if (cuurentProgram == null) {
            throw new LoadProgramException("No program is loaded. Please load a file to display a program.");
        }

        if (cuurentProgram != null) {
            return new ProgramSummary(
                    cuurentProgram.getName(),
                    cuurentProgram.getVariablesPeek(),
                    cuurentProgram.getLabelsPeek(),
                    cuurentProgram.getInstructionsPeek()
            );
        }
        return null;
    }


    public static int sumCyclesExceptFirst(List<List<InstructionView>> extendInstructions) {
        int total = 0;
        for (List<InstructionView> chain : extendInstructions) {
            for (int i = 1; i < chain.size(); i++) {
                total += chain.get(i).cycles();
            }
        }
        return total;
    }


    @Override
    public RunResult run(int level, List<Long> inputs, List<String> varsNames) {
        ProgramExecutorImpl exe = new ProgramExecutorImpl(cuurentProgram); //, level, inputs);

        long y = exe.run(inputs);

        //need to extend the program to the level
        // calculate cycles from extend program (not from execution)
        List<List<InstructionView>> extendCommend = expandProgramToLevelForExtend(level);
        int totalCycels = sumCyclesExceptFirst(extendCommend);

        RunSummary summary = new RunSummary(
                ++runCounter,
                level,
                inputs,
                y,
                totalCycels
        );
        summaries.add(summary);

        if (exe != null) {
            var res = new RunResult(
                    y,
                    exe.variablesState(),
                    totalCycels
            );
            history.add(res);
            return res;
        }
        return null;
    }

    @Override
    public List<RunSummary> getHistory() {
        return summaries;
    }

    @Override
    public List<List<InstructionView>> expandProgramToLevelForExtend(int level) {
        return cuurentProgram.expendToLevelForExtend(level);
    }



//        // --------------------------------------------
//        // התוצאה הסופית – רשימה של רשימות InstructionView
//        // --------------------------------------------
//        List<List<InstructionView>> filteredChains = new ArrayList<>();
//
//        // שולף את כל השרשראות המורחבות מהרמה המבוקשת
//        List<List<InstructionView>> allInstructions = cuurentProgram.expendToLevelForRun(level);
//
//        // מונה רץ עבור מספור מחודש של כל ההוראות
//        int counter = 1;
//
//        // --------------------------------------------
//        // עובר על כל שרשרת הוראות
//        // --------------------------------------------
//        for (List<InstructionView> chain : allInstructions) {
//            if (chain == null || chain.isEmpty()) {
//                continue; // מדלג על שרשראות ריקות
//            }
//
//            InstructionView first = chain.get(0);
//
//            // אם הפקודה הראשונה סינתטית (לא "B") -> מדלג על כל השרשרת
//            if (Objects.equals(first.type(), "B")) {
//                continue;
//            }
//
//            // אם הפקודה הראשונה בסיסית:
//            // נבנה שרשרת חדשה עם מספור מחודש לכל ההוראות שבה
//            List<InstructionView> newChain = new ArrayList<>();
//
//            for (InstructionView instr : chain) {
//                InstructionView numbered = new InstructionView(
//                        counter++,          // מספר רץ חדש
//                        instr.type(),
//                        instr.label(),
//                        instr.command(),
//                        instr.cycles()
//                );
//                newChain.add(numbered);
//            }
//
//            // מוסיפים את השרשרת החדשה לרשימה הסופית
//            filteredChains.add(newChain);
//        }
//
//        return filteredChains;


//    @Override
//    public List<List<InstructionView>> expandProgramToLevelForExtend(int level) {
//      //  return cuurentProgram.expendToLevelForExtend(level);
//
//
//    }

    @Override
    public List<InstructionView> expandProgramToLevelForRun(int level) {
        List<InstructionView> newInstructions = new ArrayList<>();
        List<List<InstructionView>> allInstructions = cuurentProgram.expendToLevelForRun(level);
        return allInstructions.stream().flatMap(List::stream).toList();
//        int counter = 1;
//
//        for (List<InstructionView> chain : allInstructions) {
//            for (int i = 0; i < chain.size(); i++) {
//                InstructionView instr = chain.get(i);
//                if (Objects.equals(instr.type(), "B") && i == 0) {
//                    InstructionView numbered = new InstructionView(
//                            counter++,                     // מספר חדש
//                            instr.type(),
//                            instr.label(),
//                            instr.command(),
//                            instr.cycles()
//                    );
//                    newInstructions.add(numbered);
//                }
//                else if (i >= 1) {
//                    InstructionView numbered = new InstructionView(
//                            counter++,                     // מספר חדש
//                            instr.type(),
//                            instr.label(),
//                            instr.command(),
//                            instr.cycles()
//                    );
//                    newInstructions.add(numbered);
//                }
//                else {
//                    // Skip this instruction (do not add to newInstructions)
//                    continue;
//                }
//            }
//        }
//        return newInstructions;
    }

    public int getMaxExpandLevel() {
        if (cuurentProgram == null) {
            throw new LoadProgramException("No program is loaded. Please load a file to display a program.");
        }

        return cuurentProgram.calculateMaxDegree()  ;
    }
}
