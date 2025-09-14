package core.engine;

import core.program.VariableAndLabelMenger;
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


// implementation of the Engine interface (methods of the engine)
public class EngineImpl implements Engine {

    private static Program cuurentProgram = null;
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
            int place = chain.size() - 1;
            total += chain.get(place).cycles();

        }
        return total;
    }

    @Override
    public RunResult run(int level, List<Long> inputs) {
        ProgramExecutorImpl exe = new ProgramExecutorImpl(cuurentProgram); //, level, inputs);

        long y = exe.run(inputs);

        //need to extend the program to the level
        // calculate cycles from the extent program (not from execution)
        List<List<InstructionView>> extendCommend = expandProgramToLevelForExtend(level);
        int totalCycles = sumCyclesExceptFirst(extendCommend);

        RunSummary summary = new RunSummary(
                ++runCounter,
                level,
                inputs,
                y,
                totalCycles
        );
        summaries.add(summary);

        if (exe != null) {
            var res = new RunResult(
                    y,
                    exe.variablesState(),
                    totalCycles
            );
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
        List<List<InstructionView>> allInstructions = cuurentProgram.expendToLevelForRun(level);
        return allInstructions.stream().flatMap(List::stream).toList();
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

}
