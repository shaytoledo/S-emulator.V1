package core;

import dto.*;
import adapter.translate.JaxbLoader;
import adapter.translate.ProgramTranslator;
import logic.exception.LoadProgramException;
import logic.exception.NotXMLException;
import logic.exception.ProgramFileNotFoundException;
import logic.execution.ProgramExecutorImpl;
import logic.program.Program;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// implementation of the Engine interface (methods of the engine)
public class EngineImpl implements Engine {

    private static Program cuurentProgram = null;
    private int expandLevel = 0;
    private final List<RunResult> history = new ArrayList<>();
    private int runCounter = 0;

    public List<RunSummary> summaries = new ArrayList<>();

    public void checkXML(String fileName) throws NotXMLException {
        if (!fileName.endsWith(".xml")) {
            throw new NotXMLException("Invalid extension (must be .xml): " + fileName);
        }
    }

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
                return new LoadReport(false, errors, 0);
            }

            // Load the XML file into SProgram representation
            var sprogram = JaxbLoader.load(xmlPath);




            //Translate the loaded SProgram to internal Program representation
            var cuurentProgram = ProgramTranslator.translate(sprogram);

            // If there are errors during translation, return them in the LoadReport (errors that define in the specification document)
            if (!cuurentProgram.errors.isEmpty()) {
                // return the errors
                return new LoadReport(false, cuurentProgram.errors, 0);
            }

            history.clear();;
            expandLevel = 0;
            this.cuurentProgram = cuurentProgram.program;
            expandLevel = this.cuurentProgram.maxLevel();

            return new LoadReport(true, List.of(),expandLevel);

        }
        catch (Exception e) {
            return new LoadReport(false, List.of(e), 0);
        }
    }

    @Override
    public ProgramSummary getProgramSummary() {

        // there isn't valid load program
        if (cuurentProgram == null) {
            throw new LoadProgramException("No program is loaded. Please load a file to display a program.");
        }

        if (cuurentProgram != null) {
            return new ProgramSummary(
                    cuurentProgram.getName(),
                    cuurentProgram.getVariablesPeek(),
                    cuurentProgram.getLabelsPeek(),
                    cuurentProgram.getInstructionsPeek(),
                    expandLevel

            );
        }
        return null;
    }

    @Override
    public RunResult run(int level, List<Long> inputs, List<String> varsNames) {
        ProgramExecutorImpl exe = new ProgramExecutorImpl(cuurentProgram); //, level, inputs);

        long y = exe.run(inputs);

        var summary = new RunSummary(
                ++runCounter,
                level,
                inputs,
                y,
                exe.getTotalCycles()
        );
        summaries.add(summary);

        if (exe != null) {
            var res = new RunResult(
                    y,
                    exe.variablesState(),
                    exe.getTotalCycles()
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


    // need to implement
    @Override
    public void expandToLevel(int level) { }
}
