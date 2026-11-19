package com.polyglotmesh.api;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

/** Consistent JSON errors for bad requests, so the IDE never sees an HTML error page. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ApiError(String message, List<String> details) {
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiError> onValidation(WebExchangeBindException ex) {
        List<String> details = ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest().body(new ApiError("Validation failed", details));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ApiError> onMalformed(ServerWebInputException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiError("Malformed request", List.of(String.valueOf(ex.getReason()))));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> onUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.internalServerError()
                .body(new ApiError("Unexpected server error", List.of()));
    }
}
