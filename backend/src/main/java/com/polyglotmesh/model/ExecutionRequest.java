package com.polyglotmesh.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Code payload sent by the IDE.
 *
 * @param dataset optional mock collection to bind into the script (Week 4)
 */
public record ExecutionRequest(
        @NotNull(message = "language is required") Language language,
        @NotBlank(message = "code must not be empty")
        @Size(max = 20000, message = "code is limited to 20000 characters") String code,
        String dataset) {
}
