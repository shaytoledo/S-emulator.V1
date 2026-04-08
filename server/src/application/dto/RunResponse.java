package application.dto;

import dto.RunResult;

public record RunResponse(String status, String runId, RunResult result) {

    public static RunResponse done(RunResult r) {
        return new RunResponse("done", null, r);
    }

    public static RunResponse pending(String id) {
        return new RunResponse("pending", id, null);
    }
}
