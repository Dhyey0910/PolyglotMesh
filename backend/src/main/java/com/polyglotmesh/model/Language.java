package com.polyglotmesh.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Guest languages the sandbox can run. */
public enum Language {
    PYTHON("python", "python"),
    JAVASCRIPT("js", "javascript");

    private final String graalId;
    private final String label;

    Language(String graalId, String label) {
        this.graalId = graalId;
        this.label = label;
    }

    /** Language id understood by the GraalVM Polyglot API. */
    public String id() {
        return graalId;
    }

    /** Name used in the REST API and the frontend. */
    @JsonValue
    public String label() {
        return label;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Language from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("language is required");
        }
        return switch (value.trim().toLowerCase()) {
            case "python", "py" -> PYTHON;
            case "javascript", "js" -> JAVASCRIPT;
            default -> throw new IllegalArgumentException("Unsupported language: " + value);
        };
    }
}
