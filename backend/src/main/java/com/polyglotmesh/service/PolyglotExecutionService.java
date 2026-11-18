package com.polyglotmesh.service;

import com.polyglotmesh.model.ExecutionRequest;
import com.polyglotmesh.model.ExecutionResult;
import com.polyglotmesh.model.ExecutionStatus;
import com.polyglotmesh.sandbox.BoundedOutputStream;
import com.polyglotmesh.sandbox.SandboxContextFactory;
import com.polyglotmesh.sandbox.SandboxProperties;
import com.polyglotmesh.sandbox.Watchdog;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.springframework.stereotype.Service;

/** Runs user code inside the same JVM through the GraalVM Polyglot API. */
@Service
public class PolyglotExecutionService {

    private final SandboxContextFactory contextFactory;
    private final SandboxProperties properties;
    private final Watchdog watchdog;

    public PolyglotExecutionService(SandboxContextFactory contextFactory,
                                    SandboxProperties properties,
                                    Watchdog watchdog) {
        this.contextFactory = contextFactory;
        this.properties = properties;
        this.watchdog = watchdog;
    }

    public ExecutionResult execute(ExecutionRequest request) {
        long start = System.nanoTime();
        String lang = request.language().id();
        BoundedOutputStream out = new BoundedOutputStream(properties.maxOutputChars());
        BoundedOutputStream err = new BoundedOutputStream(properties.maxOutputChars());

        Context ctx = contextFactory.create(request.language(), out, err);
        Watchdog.Guard guard = watchdog.arm(ctx);

        try (ctx) {
            ctx.eval(lang, request.code());
            return ExecutionResult.success(request.language(), out.text(), millisSince(start));

        } catch (PolyglotException e) {
            ExecutionStatus status = classify(e);
            return ExecutionResult.failure(status, request.language(),
                    out.text(), e.getMessage(), "", millisSince(start));

        } finally {
            guard.disarm();
        }
    }

    private static ExecutionStatus classify(PolyglotException e) {
        if (e.isCancelled()) {
            return ExecutionStatus.TIMEOUT;
        }
        if (e.isResourceExhausted()) {
            return ExecutionStatus.LIMIT_EXCEEDED;
        }
        return ExecutionStatus.ERROR;
    }

    private static long millisSince(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
