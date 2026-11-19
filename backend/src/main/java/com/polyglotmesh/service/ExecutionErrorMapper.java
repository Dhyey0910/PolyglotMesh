package com.polyglotmesh.service;

import com.polyglotmesh.model.ExecutionResult;
import com.polyglotmesh.model.ExecutionStatus;
import com.polyglotmesh.model.Language;
import java.util.Locale;
import org.graalvm.polyglot.PolyglotException;
import org.springframework.stereotype.Component;

/** Turns raw PolyglotException failures into friendly results for the IDE console. */
@Component
public class ExecutionErrorMapper {

    private static final int MAX_FRAMES = 25;

    public ExecutionResult map(Language language, PolyglotException e, String partialOutput, long elapsedMs) {
        return ExecutionResult.failure(
                classify(e), language, partialOutput, message(e), stackTrace(e), elapsedMs);
    }

    ExecutionStatus classify(PolyglotException e) {
        if (e.isCancelled()) {
            return ExecutionStatus.TIMEOUT;
        }
        if (e.isResourceExhausted()) {
            return ExecutionStatus.LIMIT_EXCEEDED;
        }
        if (e.isSyntaxError()) {
            return ExecutionStatus.SYNTAX_ERROR;
        }
        String text = String.valueOf(e.getMessage()).toLowerCase(Locale.ROOT);
        if (text.contains("not allowed") || text.contains("denied") || text.contains("permission")) {
            return ExecutionStatus.BLOCKED;
        }
        return ExecutionStatus.ERROR;
    }

    private String message(PolyglotException e) {
        if (e.isCancelled()) {
            return "Execution was cancelled because it exceeded the time limit.";
        }
        if (e.isResourceExhausted()) {
            return "Execution stopped: the statement limit for one run was exceeded.";
        }
        return e.getMessage();
    }

    private String stackTrace(PolyglotException e) {
        StringBuilder trace = new StringBuilder();
        int frames = 0;
        for (PolyglotException.StackFrame frame : e.getPolyglotStackTrace()) {
            if (frames++ >= MAX_FRAMES) {
                trace.append("  ...\n");
                break;
            }
            trace.append("  at ").append(frame).append('\n');
        }
        return trace.toString();
    }
}
