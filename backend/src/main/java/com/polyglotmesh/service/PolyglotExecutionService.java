package com.polyglotmesh.service;

import com.polyglotmesh.model.ExecutionRequest;
import com.polyglotmesh.model.ExecutionResult;
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
    private final ExecutionErrorMapper errorMapper;

    public PolyglotExecutionService(SandboxContextFactory contextFactory,
                                    SandboxProperties properties,
                                    Watchdog watchdog,
                                    ExecutionErrorMapper errorMapper) {
        this.contextFactory = contextFactory;
        this.properties = properties;
        this.watchdog = watchdog;
        this.errorMapper = errorMapper;
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
            return errorMapper.map(request.language(), e, out.text(), millisSince(start));

        } finally {
            guard.disarm();
        }
    }

    private static long millisSince(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
