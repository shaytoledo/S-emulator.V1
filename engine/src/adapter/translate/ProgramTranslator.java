package adapter.translate;


import adapter.xml.generated.SFunction;
import adapter.xml.generated.SInstruction;
import adapter.xml.generated.SProgram;
import core.program.Function;
import core.program.Program;
import core.program.ProgramImpl;
import logic.exception.*;
import logic.instruction.Instruction;
import logic.instruction.basic.DecreaseInstruction;
import logic.instruction.basic.IncreaseInstruction;
import logic.instruction.basic.JumpNotZeroInstruction;
import logic.instruction.basic.NoOpInstruction;
import logic.instruction.synthetic.*;
import logic.label.Label;
import logic.label.LabelImpl;
import logic.variable.Variable;

import java.util.*;


public final class ProgramTranslator {

    private final static TranslatorHelper helper = new TranslatorHelper();
    private ProgramTranslator() { }

    public static final class Result {
        public final Program program;
        public final List<Exception> errors;

        Result(Program program, List<Exception> errors) {
            this.program = program;
            this.errors = errors;
        }
    }

    public static Result translate(SProgram sProgram) {

        List<Exception> errors = new ArrayList<>();
        String programName = TranslatorHelper.safe(sProgram.getName(), "Unnamed Program");

        Map<String, Variable> varsByName = new LinkedHashMap<>();
        Map<String, Label> labelsByName = new LinkedHashMap<>();

        List<Instruction> code = new ArrayList<>();
        List<Function> funcs = new ArrayList<>();


        List<SInstruction> sInstructions = Optional.ofNullable(sProgram.getInstructions()).orElse(List.of());
        List<SFunction> SFunctions = Optional.ofNullable(sProgram.getSFunctions()).orElse(List.of());

        // convert SFunctions to Functions, collecting errors
        for (SFunction func : SFunctions) {
            String funcName = func.getName();
            if (funcName == null || funcName.isBlank()) {
                errors.add(new ArgsException("Function with missing name"));
                continue;
            }
            String userString = TranslatorHelper.safe(func.getUserString(), "");
            if (userString == null || userString.isBlank()) {
                errors.add(new ArgsException("Function '" + funcName + "' missing user-string"));
                continue;
            }
            List<SInstruction> a = func.getSInstructions();

            List<SInstruction> funcInstructions = Optional.ofNullable(a).orElse(List.of());
            // convert function instructions to Instructions, collecting errors
            funcs.add(new Function(funcName, userString, extractInstructions(funcInstructions, errors, funcs, varsByName, labelsByName)));
        }

        // convert SInstructions to Instructions, collecting errors
        code.addAll(extractInstructions(sInstructions, errors, funcs,varsByName, labelsByName));

        ProgramImpl program = new ProgramImpl(programName, code, funcs, varsByName, labelsByName);
        return new Result(program, errors);
    }

    private static String errorMessageByLine(int line, String msg) {
        return String.format("Error in line %d: %s", line + 1, msg);
    }


    // get SInstructions and convert them to Instructions, collecting errors
    private static  List<Instruction> extractInstructions(
            List<SInstruction> sInstructions,
            List<Exception> errors,
            List<Function> funcs, Map<String, Variable> varsByName, Map<String, Label> labelsByName) {

        List<Instruction> code = new ArrayList<>();



        // First pass: collect all labels
        Set<String> labels = new HashSet<>();
        for (SInstruction instr : sInstructions) {
            String lbl = instr.getLabel();
            if (lbl != null && !lbl.isBlank()) {
                labels.add(lbl);
            }
        }

        for (int idx = 0; idx < sInstructions.size(); idx++) {
            SInstruction si = sInstructions.get(idx);

            // get label if exists or null
            Label lineLabel = null;
            String lineLabelName = helper.trimToNull(si.getLabel());
            if (lineLabelName != null) {
                lineLabel = labelsByName.computeIfAbsent(lineLabelName, LabelImpl::new);
            }


            // get variable if exists or null
            Variable variable = null;
            String variableName = helper.trimToNull(si.getVariable());
            if (variableName != null) {
                variable = varsByName.computeIfAbsent(variableName, TranslatorHelper::newVarStrict);
            }

            //get all the variables from arguments
            Map<String, String> args = TranslatorHelper.argumentsMap(si.getArguments());

            String type = TranslatorHelper.safe(si.getType(), "basic");
            String name = TranslatorHelper.safe(si.getName(), "NEUTRAL");

            Instruction instruction = null;

            if (type.equalsIgnoreCase("basic")) {
                switch (name) {
                    case "INCREASE" -> {
                        if (variable == null) {
                            errors.add(new MissingVariableException(errorMessageByLine(idx , "INCREASE missing variable")));
                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel);
                        } else {
                            instruction = new IncreaseInstruction(variable, lineLabel);
                        }
                    }

                    case "DECREASE" -> {
                        if (variable == null) {
                            errors.add(new MissingVariableException(errorMessageByLine(idx, "DECREASE missing variable")));
                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel);
                        } else {
                            instruction = new DecreaseInstruction(variable, lineLabel);
                        }
                    }

                    case "JUMP_NOT_ZERO" -> {
                        if (variable == null) {
                            errors.add(new MissingVariableException(errorMessageByLine(idx, "JUMP_NOT_ZERO missing variable")));
                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel);
                        } else {
                            String targetLabelName = args.getOrDefault("JNZLabel", "");
                            if (targetLabelName.isBlank()) {
                                errors.add(new MissingVariableException(errorMessageByLine(idx, "JUMP_NOT_ZERO missing JNZLabel argument")));
                                instruction = new NoOpInstruction(variable, lineLabel);
                            } else if (!targetLabelName.equals("EXIT") && !labels.contains(targetLabelName)) {
                                errors.add(new LabelException(errorMessageByLine(idx, "Unknown JNZ label: " + targetLabelName)));
                                instruction = new NoOpInstruction(variable, lineLabel);
                            } else {

                                Label targetLabel = labelsByName.computeIfAbsent(targetLabelName, LabelImpl::new);
                                instruction = new JumpNotZeroInstruction(variable, targetLabel, lineLabel);
                            }
                        }
                    }

                    case "NEUTRAL" -> {
                        instruction = new NoOpInstruction(
                                variable != null ? variable : TranslatorHelper.newVarStrict("y"),
                                lineLabel
                        );
                    }

                    default -> {
                        errors.add(new UnknownInstruction(errorMessageByLine(idx, "Unknown basic instruction: " + name)));
                        instruction = new NoOpInstruction(
                                variable != null ? variable : TranslatorHelper.newVarStrict("y"),
                                lineLabel);
                    }
                }
            } else {
                // synthetic instructions
                switch (name) {

                    case "ASSIGNMENT" -> {
                        if (variable == null) {
                            errors.add(new MissingVariableException(errorMessageByLine(idx, "ASSIGNMENT missing target variable")));
                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel);
                        } else {
                            String otherVarName = args.getOrDefault("assignedVariable", "");
                            if (otherVarName.isBlank()) {
                                errors.add(new ArgsException(errorMessageByLine(idx, "ASSIGNMENT missing 'assignedVariable'")));
                                instruction = new NoOpInstruction(variable, lineLabel);
                            } else {
                                try {
                                    Variable otherVar = varsByName.computeIfAbsent(otherVarName,TranslatorHelper::parseStrictVariable);
                                    instruction = new AssignmentInstruction(variable, otherVar, lineLabel);
                                } catch (IllegalArgumentException ex) {
                                    errors.add(new ArgsException(errorMessageByLine(idx, "ASSIGNMENT bad 'otherVariable': " + otherVarName)));
                                    instruction = new NoOpInstruction(variable, lineLabel);
                                }
                            }
                        }
                    }

                    case "CONSTANT_ASSIGNMENT" -> {
                        if (variable == null) {
                            errors.add(new MissingVariableException(errorMessageByLine(idx, "CONSTANT_ASSIGNMENT missing target variable")));
                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel);
                        } else {
                            String valStr = args.getOrDefault("constantValue", "");
                            try {
                                long value = Long.parseLong(valStr);
                                instruction = new ConstantAssignmentInstruction(variable, value, lineLabel);
                            } catch (NumberFormatException ex) {
                                errors.add(new ArgsException(errorMessageByLine(idx, "CONSTANT_ASSIGNMENT bad 'constantValue': " + valStr)));
                                instruction = new NoOpInstruction(variable, lineLabel);
                            }
                        }
                    }

                    case "ZERO_VARIABLE" -> {
                        if (variable == null) {
                            errors.add(new MissingVariableException(errorMessageByLine(idx, "ZERO_VARIABLE missing variable")));
                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel);
                        } else {
                            instruction = new ZeroVariableInstruction(variable, lineLabel);
                        }
                    }

                    case "GOTO_LABEL" -> {
                        String toName = args.getOrDefault("gotoLabel", "");
                        if (toName.isBlank()) {
                            errors.add (new ArgsException(errorMessageByLine(idx, "GOTO_LABEL missing 'gotoLabel'")));
                            instruction = new NoOpInstruction(variable != null ? variable : TranslatorHelper.newVarStrict("y"), lineLabel);
                        } else if (!labels.contains(toName) && !toName.equals("EXIT")) {
                            errors.add(new LabelException(errorMessageByLine(idx, "Unknown goto label: " + toName)));
                        } else {
                            Label to = labelsByName.computeIfAbsent(toName, LabelImpl::new);
                            instruction = new GoToInstruction(variable != null ? variable : TranslatorHelper.newVarStrict("y"), to, lineLabel);
                        }
                    }

                    case "JUMP_ZERO" -> {
                        if (variable == null) {
                            errors.add(new MissingVariableException(errorMessageByLine(idx, "JUMP_ZERO missing variable")));
                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel);
                        } else {
                            String targetLabelName = args.getOrDefault("JZLabel", "");
                            if (targetLabelName.isBlank()) {
                                errors.add(new ArgsException(errorMessageByLine(idx, "JUMP_ZERO missing 'JZLabel'")));
                                instruction = new NoOpInstruction(variable, lineLabel);
                            } else if (!targetLabelName.equals("EXIT") && !labels.contains(targetLabelName)) {
                                errors.add(new LabelException(errorMessageByLine(idx, "Unknown JZ label: " + targetLabelName)));
                            } else {
                                Label target = labelsByName.computeIfAbsent(targetLabelName, LabelImpl::new);
                                instruction = new JumpZeroInstruction(variable, target, lineLabel);
                            }
                        }
                    }

                    case "JUMP_EQUAL_CONSTANT" -> {
                        if (variable == null) {
                            errors.add(new MissingVariableException(errorMessageByLine(idx, "JUMP_EQUAL_CONSTANT missing variable")));
                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel);
                        } else {
                            String targetLabelName = args.getOrDefault("JEConstantLabel", "");
                            String constantValue = args.getOrDefault("constantValue", "");
                            if (targetLabelName.isBlank()) {
                                errors.add(new LabelException(errorMessageByLine(idx, "JUMP_EQUAL_CONSTANT missing 'JEQLabel'")));
                                instruction = new NoOpInstruction(variable, lineLabel);
                            } else if (!targetLabelName.equals("EXIT") && !labels.contains(targetLabelName)) {
                                errors.add(new LabelException(errorMessageByLine(idx, "Unknown JEConstantLabel label: " + targetLabelName)));
                                instruction = new NoOpInstruction(variable, lineLabel);
                            } else {
                                if (constantValue == null || constantValue.isBlank()) {
                                    errors.add(new ArgsException((errorMessageByLine(idx, "Missing constantValue for JUMP_EQUAL_CONSTANT"))));
                                } else {
                                    try {
                                        long num = Long.parseLong(constantValue);
                                        Label target = labelsByName.computeIfAbsent(targetLabelName, LabelImpl::new);
                                        instruction = new JumpEqualConstantInstruction(variable, target, num, lineLabel);
                                    } catch (NumberFormatException e) {
                                        errors.add(new ArgsException((errorMessageByLine(idx, "Invalid constantValue: " + constantValue))));
                                    }
                                }
                            }
                        }
                    }

                    case "JUMP_EQUAL_VARIABLE" -> {
                        if (variable == null) {
                            errors.add(new MissingVariableException(errorMessageByLine(idx, "JUMP_EQUAL_VARIABLE missing variable")));
                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel);
                        } else {
                            String targetLabelName = args.getOrDefault("JEVariableLabel", "");
                            String otherVarName = args.getOrDefault("variableName", "");
                            if (targetLabelName.isBlank()) {
                                errors.add(new ArgsException(errorMessageByLine(idx, "JUMP_EQUAL_VARIABLE missing 'JEQLabel'")));
                                instruction = new NoOpInstruction(variable, lineLabel);
                            } else if (!targetLabelName.equals("EXIT") && !labels.contains(targetLabelName)) {
                                errors.add(new LabelException(errorMessageByLine(idx, "Unknown JEVariableLabel label: " + targetLabelName)));
                            } else {
                                if (otherVarName.isBlank()) {
                                    errors.add(new ArgsException(errorMessageByLine(idx, "JUMP_EQUAL_VARIABLE missing 'otherVariable'")));
                                    instruction = new NoOpInstruction(variable, lineLabel);
                                } else {
                                    try {
                                        Variable other = varsByName.computeIfAbsent(otherVarName, TranslatorHelper::parseStrictVariable);
                                        Label target = labelsByName.computeIfAbsent(targetLabelName, LabelImpl::new);
                                        instruction = new JumpEqualVariableInstruction(variable, target, other, lineLabel);
                                    } catch (IllegalArgumentException ex) {
                                        errors.add(new ArgsException(errorMessageByLine(idx, "JUMP_EQUAL_VARIABLE bad 'otherVariable': " + otherVarName)));
                                        instruction = new NoOpInstruction(variable, lineLabel);
                                    }
                                }
                            }
                        }
                    }

//                    case "QUOTE" -> {
//                        String targetFunctionName = args.getOrDefault("functionName", "");
//                        if (targetFunctionName.isEmpty()) {
//                            // missing function name
//                            errors.add(new MissingFunctionName(errorMessageByLine(idx, "QUOTE missing 'functionName'")));
//                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel);
//                        }
//                        else {
//                            // function name provided
//                            if (functionExists(targetFunctionName, funcs)) {
//                                // function exists
//                                String functionArguments = args.getOrDefault("functionArguments", "");
//
//                                // need to create new qute instruction and set there the function name and arguments
//                                // for each funcction need to check if exists\
//                                ///
//
//
//                                // create the qute instruction // need to pase all funcs errores
//                                Instruction qute = new QuoteInstruction(functionArguments, funcs, errors, lineLabel);
//
//                                FunctionCallModel fcm = new FunctionCallModel(targetFunctionName + functionArguments);
//
//
//                            } else {
//                                // function does not exist
//                                errors.add(new MissingFunctionName(errorMessageByLine(idx, "QUOTE unknown function name: " + targetFunctionName)));
//                                instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel);
//                            }
//
//
//                        }
//
//
//                    }

                    default -> {
                        errors.add(new UnknownInstruction(errorMessageByLine(idx, "Unknown synthetic instruction: " + name)));
                        instruction = new NoOpInstruction(
                                (variable != null ? variable : TranslatorHelper.newVarStrict("y")),
                                lineLabel
                        );
                    }
                }

            }
            code.add(instruction);
        }
        return code;
    }




    public static boolean functionExists(String functionName, List<Function> funcs) {
        for (Function func : funcs) {
            if (func.getName().equals(functionName)) {
                return true;
            }
        }
        return false;
    }



}
