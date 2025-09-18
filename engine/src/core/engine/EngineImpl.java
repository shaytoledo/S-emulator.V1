package core.engine;

import adapter.translate.JaxbLoader;
import adapter.translate.ProgramTranslator;
import core.program.Program;
import core.program.VariableAndLabelMenger;
import dto.*;
import javafx.util.Pair;
import logic.exception.LoadProgramException;
import logic.exception.NotXMLException;
import logic.exception.ProgramFileNotFoundException;
import logic.execution.ProgramExecutorImpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


// implementation of the Engine interface (methods of the engine)
public class EngineImpl implements Engine {

    private static Program cuurentProgram = null;
    ProgramExecutorImpl exe;
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
            var currentProgram = ProgramTranslator.translate(sprogram);

            // If there are errors during translation, return them in the LoadReport (errors that define in the specification document)
            if (!currentProgram.errors.isEmpty()) {
                // return the errors
                return new LoadReport(false, currentProgram.errors);
            }

            summaries.clear();
            EngineImpl.cuurentProgram = currentProgram.program;
            return new LoadReport(true, List.of());

        } catch (Exception e) {
            return new LoadReport(false, List.of(e));
        }
    }

    @Override
    public ProgramSummary getProgramSummaryForShow() {
        List<InstructionView> instructionViews = cuurentProgram
                .instructionViewsAfterExtendRunShow(0);
        // there isn't valid load program
        if (cuurentProgram == null) {
            throw new LoadProgramException("No program is loaded. Please load a file to display a program.");
        }

        //note gets the extend info
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

    @Override
    public RunResult run(int level, List<Long> inputs) {

        List<InstructionView> instructionViews = cuurentProgram
                .instructionViewsAfterExtendRunShow(level);
        exe = new ProgramExecutorImpl(cuurentProgram);
        long y = exe.run(inputs);
        int cycles = exe.cycleCount;

        RunSummary summary = new RunSummary(++runCounter, level, inputs, y, cycles);
        summaries.add(summary);

        if (exe != null) {
            var res = new RunResult(y, exe.variablesState(), cycles);
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

    @Override
    public List<InstructionView> expandProgramToLevelForRun(int level) {
        List<InstructionView> allInstructions = cuurentProgram.instructionViewsAfterExtendRunShow(level);
        return allInstructions;
    }

    public int getMaxExpandLevel() {
        if (cuurentProgram == null) {
            throw new LoadProgramException("No program is loaded. Please load a file to display a program.");
        }

        return cuurentProgram.calculateMaxDegree()  ;
    }

    public VariableAndLabelMenger getVlm(){
        return cuurentProgram.getvlm();
    }

    @Override
    public List<List<String>> getInfoForEachInstruction(int level) {
        return cuurentProgram.getInfo(level);
    }

    @Override
    public  Pair<Map<String, Long>,Integer> startDebug(int level, List<Long> inputs) {
        // expend the program to the specified level
        List<InstructionView> instructionViews = cuurentProgram
                .instructionViewsAfterExtendRunShow(level);

        //create the executor
        exe = new ProgramExecutorImpl(cuurentProgram);

        // intialize all the variables iin context
        exe.init(inputs);

        // get the state of all variables
        Map<String, Long> variablesState = exe.variablesState();
        Pair<Map<String, Long>,Integer> info = new Pair<>(variablesState, exe.debugIndexCounter);
        return info;
    }

    @Override
    public Pair<Map<String, Long>,Integer> oneStepInDebug() {
        //run one step
        int index = exe.runOneStep();

        if (index == -1) {
            // program finished in one step
            Pair<Map<String, Long>,Integer> info = new Pair<>(null, index);
            return info;
        }

        int cycles = exe.cycleCount;

        // get the state of all variables
        Map<String, Long> variablesState = exe.variablesState();
        Pair<Map<String, Long>,Integer> info = new Pair<>(variablesState, index);

        return info;
    }

    @Override
    public void endDebug() {
        exe = null;
    }

    @Override
    public Map<String, Long> resumeDebug() {
        exe.resume();
        return exe.variablesState();
    }

    @Override
    public int getCycels() {
        return exe.cycleCount;
    }

}