package dto;

import java.util.Map;

public record RunResult(
        long y,
        Map<String, Long> variables,
        long totalCycles
) {}