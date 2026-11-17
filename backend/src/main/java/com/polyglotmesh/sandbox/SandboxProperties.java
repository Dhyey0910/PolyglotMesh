package com.polyglotmesh.sandbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Limits applied to every user-submitted script (see application.yml).
 *
 * @param timeoutMs      wall-clock limit before the context is cancelled
 * @param statementLimit maximum guest statements executed per run
 * @param maxOutputChars console output cap, so a print loop cannot fill the heap
 */
@ConfigurationProperties(prefix = "sandbox")
public record SandboxProperties(long timeoutMs, long statementLimit, int maxOutputChars) {

    public SandboxProperties {
        if (timeoutMs <= 0) {
            timeoutMs = 3000;
        }
        if (statementLimit <= 0) {
            statementLimit = 5_000_000;
        }
        if (maxOutputChars <= 0) {
            maxOutputChars = 10_000;
        }
    }
}
