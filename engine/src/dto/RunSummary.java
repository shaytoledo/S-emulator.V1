package dto;

import java.util.List;

public record RunSummary(
        int runNumber,
        int level,
        List<Long> inputs,
        long y,
        long cycles
) {}