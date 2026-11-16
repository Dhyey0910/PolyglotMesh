package com.polyglotmesh.api;

import com.polyglotmesh.model.ExecutionRequest;
import com.polyglotmesh.model.ExecutionResult;
import com.polyglotmesh.model.Language;
import com.polyglotmesh.service.PolyglotExecutionService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** Reactive execution API: receives code payloads and returns the console output. */
@RestController
@RequestMapping("/api")
public class ExecutionController {

    private final PolyglotExecutionService executionService;

    public ExecutionController(PolyglotExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping("/execute")
    public Mono<ExecutionResult> execute(@Valid @RequestBody ExecutionRequest request) {
        // Guest code blocks its thread, so keep it off the Netty event loop.
        return Mono.fromCallable(() -> executionService.execute(request))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/languages")
    public Flux<String> languages() {
        return Flux.fromArray(Language.values()).map(Language::label);
    }

    @GetMapping("/health")
    public Mono<Map<String, String>> health() {
        return Mono.just(Map.of(
                "status", "UP",
                "vm", System.getProperty("java.vm.name", "unknown"),
                "javaVersion", System.getProperty("java.version", "unknown")));
    }
}
