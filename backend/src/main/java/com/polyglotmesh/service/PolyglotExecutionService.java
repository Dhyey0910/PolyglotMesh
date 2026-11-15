package com.polyglotmesh.service;

import com.polyglotmesh.model.ExecutionRequest;
import com.polyglotmesh.model.ExecutionResult;
import com.polyglotmesh.model.ExecutionStatus;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.springframework.stereotype.Service;

/** Runs user code inside the same JVM through the GraalVM Polyglot API. */
@Service
public class PolyglotExecutionService {

    public ExecutionResult execute(ExecutionRequest request) {
        long start = System.nanoTime();
        String lang = request.language().id();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();

        try (Context ctx = Context.newBuilder(lang)
                .out(out)
                .err(err)
                .option("engine.WarnInterpreterOnly", "false")
                .build()) {

            ctx.eval(lang, request.code());
            return ExecutionResult.success(request.language(), text(out), millisSince(start));

        } catch (PolyglotException e) {
            return ExecutionResult.failure(ExecutionStatus.ERROR, request.language(),
                    text(out), e.getMessage(), "", millisSince(start));
        }
    }

    private static String text(ByteArrayOutputStream stream) {
        return stream.toString(StandardCharsets.UTF_8);
    }

    private static long millisSince(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
