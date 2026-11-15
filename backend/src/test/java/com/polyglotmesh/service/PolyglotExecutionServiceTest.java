package com.polyglotmesh.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.polyglotmesh.model.ExecutionRequest;
import com.polyglotmesh.model.ExecutionResult;
import com.polyglotmesh.model.ExecutionStatus;
import com.polyglotmesh.model.Language;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "polyglotmesh.hello=false")
class PolyglotExecutionServiceTest {

    @Autowired
    private PolyglotExecutionService service;

    private ExecutionResult run(Language language, String code) {
        return service.execute(new ExecutionRequest(language, code, null));
    }

    @Test
    void pythonOutputIsCaptured() {
        ExecutionResult result = run(Language.PYTHON, "print('Hello from Python')");

        assertThat(result.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(result.output().trim()).isEqualTo("Hello from Python");
        assertThat(result.language()).isEqualTo(Language.PYTHON);
    }

    @Test
    void javascriptConsoleLogIsCaptured() {
        ExecutionResult result = run(Language.JAVASCRIPT, "console.log('Hello from JS')");

        assertThat(result.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(result.output().trim()).isEqualTo("Hello from JS");
    }

    @Test
    void runtimeErrorIsReportedNotThrown() {
        ExecutionResult result = run(Language.PYTHON, "print(1 / 0)");

        assertThat(result.status()).isEqualTo(ExecutionStatus.ERROR);
        assertThat(result.error()).containsIgnoringCase("division");
    }

    @Test
    void durationIsMeasured() {
        ExecutionResult result = run(Language.JAVASCRIPT, "let n = 0; for (let i = 0; i < 1000; i++) n += i;");

        assertThat(result.durationMs()).isGreaterThanOrEqualTo(0);
    }
}
