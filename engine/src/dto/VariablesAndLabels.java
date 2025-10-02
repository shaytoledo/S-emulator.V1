package dto;

import logic.label.Label;
import logic.variable.Variable;

import java.util.List;

public record VariablesAndLabels(
        List<String> xVariables,
        List<String> ZVariables,
        List<String> yVariables,
        List<String> labels
) { }
