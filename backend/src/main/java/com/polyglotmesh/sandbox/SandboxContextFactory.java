package com.polyglotmesh.sandbox;

import com.polyglotmesh.model.Language;
import java.io.OutputStream;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.EnvironmentAccess;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.ResourceLimits;
import org.graalvm.polyglot.io.IOAccess;
import org.springframework.stereotype.Component;

/**
 * Builds locked-down GraalVM contexts. Guest code gets no file system, no
 * network, no threads, no processes, no environment variables and no access
 * to arbitrary Java classes. A statement limit caps CPU work per run.
 */
@Component
public class SandboxContextFactory {

    /** Only members marked @HostAccess.Export plus plain maps and lists are visible to guests. */
    private static final HostAccess HOST_ACCESS = HostAccess.newBuilder(HostAccess.EXPLICIT)
            .allowMapAccess(true)
            .allowListAccess(true)
            .allowArrayAccess(true)
            .build();

    private final SandboxProperties properties;

    public SandboxContextFactory(SandboxProperties properties) {
        this.properties = properties;
    }

    public Context create(Language language, OutputStream out, OutputStream err) {
        ResourceLimits limits = ResourceLimits.newBuilder()
                .statementLimit(properties.statementLimit(), null)
                .build();

        return Context.newBuilder(language.id())
                .allowHostAccess(HOST_ACCESS)
                .allowHostClassLookup(className -> false)
                .allowIO(IOAccess.NONE)
                .allowNativeAccess(false)
                .allowCreateThread(false)
                .allowCreateProcess(false)
                .allowEnvironmentAccess(EnvironmentAccess.NONE)
                .allowPolyglotAccess(PolyglotAccess.NONE)
                .resourceLimits(limits)
                .out(out)
                .err(err)
                .option("engine.WarnInterpreterOnly", "false")
                .build();
    }
}
