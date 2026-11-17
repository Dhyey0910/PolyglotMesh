package com.polyglotmesh.sandbox;

import com.polyglotmesh.model.Language;
import java.io.OutputStream;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.EnvironmentAccess;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.io.IOAccess;
import org.springframework.stereotype.Component;

/**
 * Builds locked-down GraalVM contexts. Guest code gets no file system, no
 * network, no threads, no processes, no environment variables and no access
 * to arbitrary Java classes.
 */
@Component
public class SandboxContextFactory {

    /** Only members marked @HostAccess.Export plus plain maps and lists are visible to guests. */
    private static final HostAccess HOST_ACCESS = HostAccess.newBuilder(HostAccess.EXPLICIT)
            .allowMapAccess(true)
            .allowListAccess(true)
            .allowArrayAccess(true)
            .build();

    public Context create(Language language, OutputStream out, OutputStream err) {
        return baseBuilder(language, out, err).build();
    }

    protected Context.Builder baseBuilder(Language language, OutputStream out, OutputStream err) {
        return Context.newBuilder(language.id())
                .allowHostAccess(HOST_ACCESS)
                .allowHostClassLookup(className -> false)
                .allowIO(IOAccess.NONE)
                .allowNativeAccess(false)
                .allowCreateThread(false)
                .allowCreateProcess(false)
                .allowEnvironmentAccess(EnvironmentAccess.NONE)
                .allowPolyglotAccess(PolyglotAccess.NONE)
                .out(out)
                .err(err)
                .option("engine.WarnInterpreterOnly", "false");
    }
}
