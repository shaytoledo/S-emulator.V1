package adapter.translate;



import adapter.xml.generated.SInstruction;
import adapter.xml.generated.SProgram;
import logic.exception.*;
import logic.instruction.*;
import logic.instruction.basic.*;
import logic.instruction.synthetic.*;
import logic.label.Label;
import logic.label.LabelImpl;
import logic.program.Program;
import logic.program.ProgramImpl;
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

        List<SInstruction> sInstructions = Optional.ofNullable(sProgram.getInstructions()).orElse(List.of());

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
                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel, TranslatorHelper.nn(args));
                        } else {
                            instruction = new IncreaseInstruction(variable, lineLabel, TranslatorHelper.nn(args));
                        }
                    }

                    case "DECREASE" -> {
                        if (variable == null) {
                            errors.add(new MissingVariableException(errorMessageByLine(idx, "DECREASE missing variable")));
                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel, TranslatorHelper.nn(args));
                        } else {
                            instruction = new DecreaseInstruction(variable, lineLabel, TranslatorHelper.nn(args));
                        }
                    }

                    case "JUMP_NOT_ZERO" -> {
                        if (variable == null) {
                            errors.add(new MissingVariableException(errorMessageByLine(idx, "JUMP_NOT_ZERO missing variable")));
                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel, TranslatorHelper.nn(args));
                        } else {
                            String targetLabelName = args.getOrDefault("JNZLabel", "");
                            if (targetLabelName.isBlank()) {
                                errors.add(new MissingVariableException(errorMessageByLine(idx, "JUMP_NOT_ZERO missing JNZLabel argument")));
                                instruction = new NoOpInstruction(variable, lineLabel, TranslatorHelper.nn(args));
                            } else if (!targetLabelName.equals("EXIT") && !labels.contains(targetLabelName)) {
                                errors.add(new LabelException(errorMessageByLine(idx, "Unknown JNZ label: " + targetLabelName)));
                                instruction = new NoOpInstruction(variable, lineLabel, TranslatorHelper.nn(args));
                            } else {

                                Label targetLabel = labelsByName.computeIfAbsent(targetLabelName, LabelImpl::new);
                                instruction = new JumpNotZeroInstruction(variable, targetLabel, lineLabel, TranslatorHelper.nn(args));
                            }
                        }
                    }

                    case "NEUTRAL" -> {
                        instruction = new NoOpInstruction(
                                variable != null ? variable : TranslatorHelper.newVarStrict("y"),
                                lineLabel,
                                TranslatorHelper.nn(args)
                        );
                    }

                    default -> {
                        errors.add(new UnknownInstruction(errorMessageByLine(idx, "Unknown basic instruction: " + name)));
                        instruction = new NoOpInstruction(
                                variable != null ? variable : TranslatorHelper.newVarStrict("y"),
                                lineLabel,
                                TranslatorHelper.nn(args)
                        );
                    }
                }
            } else {
                // synthetic instructions
                switch (name) {

                    case "ASSIGNMENT" -> {
                        if (variable == null) {
                            errors.add(new MissingVariableException(errorMessageByLine(idx, "ASSIGNMENT missing target variable")));
                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel, TranslatorHelper.nn(args));
                        } else {
                            String otherVarName = args.getOrDefault("assignedVariable", "");
                            if (otherVarName.isBlank()) {
                                errors.add(new ArgsException(errorMessageByLine(idx, "ASSIGNMENT missing 'assignedVariable'")));
                                instruction = new NoOpInstruction(variable, lineLabel, TranslatorHelper.nn(args));
                            } else {
                                try {
                                    Variable otherVar = varsByName.computeIfAbsent(otherVarName,TranslatorHelper::parseStrictVariable);
                                    instruction = new AssignmentInstruction(variable, otherVar, lineLabel, TranslatorHelper.nn(args));
                                } catch (IllegalArgumentException ex) {
                                    errors.add(new ArgsException(errorMessageByLine(idx, "ASSIGNMENT bad 'otherVariable': " + otherVarName)));
                                    instruction = new NoOpInstruction(variable, lineLabel, TranslatorHelper.nn(args));
                                }
                            }
                        }
                    }

                    case "CONSTANT_ASSIGNMENT" -> {
                        if (variable == null) {
                            errors.add(new MissingVariableException(errorMessageByLine(idx, "CONSTANT_ASSIGNMENT missing target variable")));
                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel, TranslatorHelper.nn(args));
                        } else {
                            String valStr = args.getOrDefault("constantValue", "");
                            try {
                                long value = Long.parseLong(valStr);
                                instruction = new ConstantAssignmentInstruction(variable, value, lineLabel, TranslatorHelper.nn(args));
                            } catch (NumberFormatException ex) {
                                errors.add(new ArgsException(errorMessageByLine(idx, "CONSTANT_ASSIGNMENT bad 'constantValue': " + valStr)));
                                instruction = new NoOpInstruction(variable, lineLabel, TranslatorHelper.nn(args));
                            }
                        }
                    }

                    case "ZERO_VARIABLE" -> {
                        if (variable == null) {
                            errors.add(new MissingVariableException(errorMessageByLine(idx, "ZERO_VARIABLE missing variable")));
                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel, TranslatorHelper.nn(args));
                        } else {
                            instruction = new ZeroVariableInstruction(variable, lineLabel, TranslatorHelper.nn(args));
                        }
                    }

                    case "GOTO_LABEL" -> {
                        String toName = args.getOrDefault("gotoLabel", "");
                        if (toName.isBlank()) {
                            errors.add (new ArgsException(errorMessageByLine(idx, "GOTO_LABEL missing 'gotoLabel'")));
                            instruction = new NoOpInstruction(variable != null ? variable : TranslatorHelper.newVarStrict("y"), lineLabel, TranslatorHelper.nn(args));
                        } else if (!labels.contains(toName) && !toName.equals("EXIT")) {
                            errors.add(new LabelException(errorMessageByLine(idx, "Unknown goto label: " + toName)));
                        } else {
                            Label to = labelsByName.computeIfAbsent(toName, LabelImpl::new);
                            instruction = new GoToInstruction(variable != null ? variable : TranslatorHelper.newVarStrict("y"), to, lineLabel, TranslatorHelper.nn(args));
                        }
                    }

                    case "JUMP_ZERO" -> {
                        if (variable == null) {
                            errors.add(new MissingVariableException(errorMessageByLine(idx, "JUMP_ZERO missing variable")));
                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel, TranslatorHelper.nn(args));
                        } else {
                            String targetLabelName = args.getOrDefault("JZLabel", "");
                            if (targetLabelName.isBlank()) {
                                errors.add(new ArgsException(errorMessageByLine(idx, "JUMP_ZERO missing 'JZLabel'")));
                                instruction = new NoOpInstruction(variable, lineLabel, TranslatorHelper.nn(args));
                            } else if (!targetLabelName.equals("EXIT") && !labels.contains(targetLabelName)) {
                                errors.add(new LabelException(errorMessageByLine(idx, "Unknown JZ label: " + targetLabelName)));
                            } else {
                                Label target = labelsByName.computeIfAbsent(targetLabelName, LabelImpl::new);
                                instruction = new JumpZeroInstruction(variable, target, lineLabel, TranslatorHelper.nn(args));
                            }
                        }
                    }

                    case "JUMP_EQUAL_CONSTANT" -> {
                        if (variable == null) {
                            errors.add(new MissingVariableException(errorMessageByLine(idx, "JUMP_EQUAL_CONSTANT missing variable")));
                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel, TranslatorHelper.nn(args));
                        } else {
                            String targetLabelName = args.getOrDefault("JEConstantLabel", "");
                            String constantValue = args.getOrDefault("constantValue", "");
                            if (targetLabelName.isBlank()) {
                                errors.add(new LabelException(errorMessageByLine(idx, "JUMP_EQUAL_CONSTANT missing 'JEQLabel'")));
                                instruction = new NoOpInstruction(variable, lineLabel, TranslatorHelper.nn(args));
                            } else if (!targetLabelName.equals("EXIT") && !labels.contains(targetLabelName)) {
                                errors.add(new LabelException(errorMessageByLine(idx, "Unknown JEConstantLabel label: " + targetLabelName)));
                                instruction = new NoOpInstruction(variable, lineLabel, TranslatorHelper.nn(args));
                            } else {
                                if (constantValue == null || constantValue.isBlank()) {
                                    errors.add(new ArgsException((errorMessageByLine(idx, "Missing constantValue for JUMP_EQUAL_CONSTANT"))));
                                } else {
                                    try {
                                        long num = Long.parseLong(constantValue);
                                        Label target = labelsByName.computeIfAbsent(targetLabelName, LabelImpl::new);
                                        instruction = new JumpEqualConstantInstruction(variable, target, num, lineLabel, TranslatorHelper.nn(args));
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
                            instruction = new NoOpInstruction(TranslatorHelper.newVarStrict("y"), lineLabel, TranslatorHelper.nn(args));
                        } else {
                            String targetLabelName = args.getOrDefault("JEVariableLabel", "");
                            String otherVarName = args.getOrDefault("variableName", "");
                            if (targetLabelName.isBlank()) {
                                errors.add(new ArgsException(errorMessageByLine(idx, "JUMP_EQUAL_VARIABLE missing 'JEQLabel'")));
                                instruction = new NoOpInstruction(variable, lineLabel, TranslatorHelper.nn(args));
                            } else if (!targetLabelName.equals("EXIT") && !labels.contains(targetLabelName)) {
                                errors.add(new LabelException(errorMessageByLine(idx, "Unknown JEVariableLabel label: " + targetLabelName)));
                            } else {
                                if (otherVarName.isBlank()) {
                                    errors.add(new ArgsException(errorMessageByLine(idx, "JUMP_EQUAL_VARIABLE missing 'otherVariable'")));
                                    instruction = new NoOpInstruction(variable, lineLabel, TranslatorHelper.nn(args));
                                } else {
                                    try {
                                        Variable other = varsByName.computeIfAbsent(otherVarName, TranslatorHelper::parseStrictVariable);
                                        Label target = labelsByName.computeIfAbsent(targetLabelName, LabelImpl::new);
                                        instruction = new JumpEqualVariableInstruction(variable, target, other, lineLabel, TranslatorHelper.nn(args));
                                    } catch (IllegalArgumentException ex) {
                                        errors.add(new ArgsException(errorMessageByLine(idx, "JUMP_EQUAL_VARIABLE bad 'otherVariable': " + otherVarName)));
                                        instruction = new NoOpInstruction(variable, lineLabel, TranslatorHelper.nn(args));
                                    }
                                }
                            }
                        }
                    }

                    default -> {
                        errors.add(new UnknownInstruction(errorMessageByLine(idx, "Unknown synthetic instruction: " + name)));
                        instruction = new NoOpInstruction(
                                (variable != null ? variable : TranslatorHelper.newVarStrict("y")),
                                lineLabel,
                                TranslatorHelper.nn(args)
                        );
                    }
                }

            }
            code.add(instruction);
        }

        ProgramImpl program = new ProgramImpl.Builder()
                .withName(programName)
                .withInstructions(code)
                .withVariables(varsByName.values())
                .withLabels(labelsByName.values())
                .withExitLabel(labelsByName.get("EXIT"))
                .build();

        return new Result(program, errors);
    }

    private static String errorMessageByLine(int line, String msg) {
        return String.format("Error in line %d: %s", line + 1, msg);
    }


}
