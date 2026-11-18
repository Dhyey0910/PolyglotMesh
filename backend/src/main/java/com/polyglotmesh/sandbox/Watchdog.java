package com.polyglotmesh.sandbox;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.graalvm.polyglot.Context;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * Cancels a context that runs longer than sandbox.timeout-ms. This is what
 * stops an infinite loop like "while True: pass".
 */
@Component
public class Watchdog implements DisposableBean {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "sandbox-watchdog");
        thread.setDaemon(true);
        return thread;
    });

    private final SandboxProperties properties;

    public Watchdog(SandboxProperties properties) {
        this.properties = properties;
    }

    public Guard arm(Context context) {
        ScheduledFuture<?> task = scheduler.schedule(() -> {
            try {
                context.close(true); // cancel even if the guest is mid-execution
            } catch (RuntimeException ignored) {
                // context already closed
            }
        }, properties.timeoutMs(), TimeUnit.MILLISECONDS);
        return new Guard(task);
    }

    @Override
    public void destroy() {
        scheduler.shutdownNow();
    }

    /** Handle returned by arm(); call disarm() once the script finished in time. */
    public record Guard(ScheduledFuture<?> task) {
        public void disarm() {
            task.cancel(false);
        }
    }
}
