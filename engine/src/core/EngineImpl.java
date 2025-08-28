package core;

import adapter.translate.JaxbLoader;
import adapter.translate.ProgramTranslator;
import logic.exception.NoProgramLoadedException;
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

    // Use EngineJaxbLoader to load the XML file and ProgramTranslator to translate it to internal representation
    @Override
    public LoadReport loadProgram(Path xmlPath) {
        List<String> errors = new ArrayList<>();
        try {
            String fileName = xmlPath.getFileName().toString().toLowerCase();
            if (!fileName.endsWith(".xml")) {
                errors.add("Invalid extension (must be .xml): " + fileName);
            }

            if (!Files.exists(xmlPath) || !Files.isRegularFile(xmlPath)) {
                errors.add("File does not exist: " + xmlPath);
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

            this.cuurentProgram = cuurentProgram.program;
            return new LoadReport(true, List.of());

        }
        catch (Exception e) {
            return new LoadReport(false, List.of("Failed to load: " + e.getMessage()));
        }
    }

    @Override
    public ProgramSummary getProgramSummary() {

        // there isn't valid load program
        if (cuurentProgram == null) {
            throw new NoProgramLoadedException("No program is loaded. Please load a file to display a program.");
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



    @Override
    public RunResult run(int level, List<Long> inputs, List<String> varsNames) {
        ProgramExecutorImpl exe = new ProgramExecutorImpl(cuurentProgram); //, level, inputs);


        // need to asiign the input to the right variable by order from kitell to big 1 4 7 ...
        long y = exe.run(inputs, varsNames);

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
