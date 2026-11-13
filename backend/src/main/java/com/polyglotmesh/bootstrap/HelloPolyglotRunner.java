package com.polyglotmesh.bootstrap;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Week 1 smoke test: proves Python and JavaScript run inside this JVM.
 * Enabled with polyglotmesh.hello=true.
 */
@Component
@ConditionalOnProperty(name = "polyglotmesh.hello", havingValue = "true")
public class HelloPolyglotRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(HelloPolyglotRunner.class);

    @Override
    public void run(String... args) {
        try (Context ctx = Context.newBuilder("python", "js")
                .option("engine.WarnInterpreterOnly", "false")
                .build()) {

            ctx.eval("python", "print('Hello')");

            Value add = ctx.eval("js", "(a, b) => a + b");
            int sum = add.execute(2, 3).asInt();
            log.info("Polyglot ready: JS says 2 + 3 = {}", sum);
        }
    }
}
