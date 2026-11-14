package com.polyglotmesh.model;

/** What the IDE console shows after a run. */
public record ExecutionResult(
        ExecutionStatus status,
        Language language,
        String output,
        String error,
        String stackTrace,
        long durationMs) {

    public static ExecutionResult success(Language language, String output, long durationMs) {
        return new ExecutionResult(ExecutionStatus.SUCCESS, language, output, "", "", durationMs);
    }

    public static ExecutionResult failure(ExecutionStatus status, Language language, String output,
                                          String error, String stackTrace, long durationMs) {
        return new ExecutionResult(status, language, output, error, stackTrace, durationMs);
    }
}
