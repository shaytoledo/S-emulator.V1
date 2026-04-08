package application.service;

import adapter.translate.JaxbLoader;
import adapter.translate.ProgramTranslator;
import application.model.ProgramEntry;
import core.program.Function;
import core.program.Program;
import dto.LoadReport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProgramRegistry {

    /** main programs, keyed by name */
    private final ConcurrentHashMap<String, ProgramEntry> programs = new ConcurrentHashMap<>();

    /** helper functions, keyed by name */
    private final ConcurrentHashMap<String, ProgramEntry> functions = new ConcurrentHashMap<>();

    /**
     * Validates and registers a program from raw XML content.
     * @return LoadReport with ok=true, or errors if validation failed.
     */
    public LoadReport register(String xmlContent, String uploaderName) {
        List<Exception> errors = new ArrayList<>();

        try {
            var sProgram = JaxbLoader.loadFromContent(xmlContent);
            var result = ProgramTranslator.translate(sProgram);

            if (!result.errors.isEmpty()) {
                return new LoadReport(false, result.errors);
            }

            Program program = result.program;
            String programName = program.getName();

            // Rule 1: main program name must be unique
            if (programs.containsKey(programName)) {
                errors.add(new IllegalArgumentException(
                        "A program named '" + programName + "' already exists in the system."));
                return new LoadReport(false, errors);
            }

            // Rule 2: functions defined in this file must not already exist
            List<Function> funcs = program.getFunctions();
            for (Function f : funcs) {
                if (functions.containsKey(f.getName())) {
                    errors.add(new IllegalArgumentException(
                            "A function named '" + f.getName() + "' already exists in the system."));
                    return new LoadReport(false, errors);
                }
            }

            // Rule 3: functions referenced by the program must already exist OR be defined in this file
            // (ProgramTranslator already checks this — if translation succeeded, references are valid
            //  within the file. Cross-file references are validated via FunctionNotExist exceptions.)

            // Register the main program
            programs.put(programName, new ProgramEntry(program, xmlContent, uploaderName, true, null));

            // Register helper functions — they share the parent XML so the engine can rebuild them
            for (Function f : funcs) {
                functions.put(f.getName(), new ProgramEntry(f, xmlContent, uploaderName, false, programName));
            }

            return new LoadReport(true, List.of());

        } catch (Exception e) {
            return new LoadReport(false, List.of(e));
        }
    }

    public ProgramEntry getProgram(String name) {
        return programs.get(name);
    }

    public ProgramEntry getFunction(String name) {
        return functions.get(name);
    }

    public boolean hasFunction(String name) {
        return functions.containsKey(name);
    }

    public List<ProgramEntry> getAllPrograms() {
        return new ArrayList<>(programs.values());
    }

    public List<ProgramEntry> getAllFunctions() {
        return new ArrayList<>(functions.values());
    }
}
