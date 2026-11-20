package com.polyglotmesh.api;

import com.polyglotmesh.service.InteropDemoService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/interop")
public class InteropController {

    private final InteropDemoService demoService;

    public InteropController(InteropDemoService demoService) {
        this.demoService = demoService;
    }

    @GetMapping("/demo")
    public Mono<Map<String, Object>> demo() {
        return Mono.fromCallable(demoService::run).subscribeOn(Schedulers.boundedElastic());
    }
}
