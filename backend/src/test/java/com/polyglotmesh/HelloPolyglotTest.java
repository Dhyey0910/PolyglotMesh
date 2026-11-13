package com.polyglotmesh;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

class HelloPolyglotTest {

    private Context.Builder builder(String language, ByteArrayOutputStream out) {
        return Context.newBuilder(language)
                .out(out)
                .option("engine.WarnInterpreterOnly", "false");
    }

    @Test
    void pythonPrintsHello() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Context ctx = builder("python", out).build()) {
            ctx.eval("python", "print('Hello')");
        }
        assertThat(out.toString(StandardCharsets.UTF_8).trim()).isEqualTo("Hello");
    }

    @Test
    void javascriptReturnsValueToJava() {
        try (Context ctx = builder("js", new ByteArrayOutputStream()).build()) {
            Value result = ctx.eval("js", "6 * 7");
            assertThat(result.asInt()).isEqualTo(42);
        }
    }

    @Test
    void pythonFunctionCanBeCalledFromJava() {
        try (Context ctx = builder("python", new ByteArrayOutputStream()).build()) {
            ctx.eval("python", "def square(n):\n    return n * n\n");
            Value square = ctx.getBindings("python").getMember("square");
            assertThat(square.execute(9).asInt()).isEqualTo(81);
        }
    }
}
