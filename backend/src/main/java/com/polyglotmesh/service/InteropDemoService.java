package com.polyglotmesh.service;

import com.polyglotmesh.model.Language;
import com.polyglotmesh.sandbox.SandboxContextFactory;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.graalvm.polyglot.Context;
import org.springframework.stereotype.Service;

/**
 * Mid-project "Polyglot Audit": a Java HashMap is handed to a Python script,
 * changed there, and read back in Java. Nothing is serialized in between, the
 * Python code works on the very same Java object.
 */
@Service
public class InteropDemoService {

    private static final String PYTHON_SCRIPT = """
            order["total"] = order["price"] * order["qty"] * (1 - order["discount"])
            order["label"] = "bulk" if order["qty"] >= 3 else "single"
            """;

    private final SandboxContextFactory contextFactory;

    public InteropDemoService(SandboxContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    public Map<String, Object> run() {
        Map<String, Object> order = new HashMap<>();
        order.put("price", 100.0);
        order.put("qty", 3);
        order.put("discount", 0.1);
        Map<String, Object> before = new LinkedHashMap<>(order);

        long start = System.nanoTime();
        try (Context ctx = contextFactory.create(
                Language.PYTHON, OutputStream.nullOutputStream(), OutputStream.nullOutputStream())) {
            ctx.getBindings("python").putMember("order", order);
            ctx.eval("python", PYTHON_SCRIPT);
        }
        long micros = (System.nanoTime() - start) / 1_000;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("script", PYTHON_SCRIPT.strip());
        response.put("before", before);
        response.put("after", order);
        response.put("elapsedMicros", micros);
        response.put("note", "Python changed the Java HashMap in place. No JSON, no network, no copy.");
        return response;
    }
}
