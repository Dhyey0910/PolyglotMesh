package com.polyglotmesh.sandbox;

import static org.assertj.core.api.Assertions.assertThat;

import com.polyglotmesh.model.ExecutionRequest;
import com.polyglotmesh.model.ExecutionResult;
import com.polyglotmesh.model.ExecutionStatus;
import com.polyglotmesh.model.Language;
import com.polyglotmesh.service.PolyglotExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "polyglotmesh.hello=false")
class SandboxSecurityTest {

    @Autowired
    private PolyglotExecutionService service;

    private ExecutionResult run(Language language, String code) {
        return service.execute(new ExecutionRequest(language, code, null));
    }

    @Test
    void normalScriptSucceeds() {
        ExecutionResult result = run(Language.PYTHON, "print(21 * 2)");
        assertThat(result.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(result.output().trim()).isEqualTo("42");
    }

    @Test
    void infiniteLoopIsStopped() {
        ExecutionResult result = run(Language.PYTHON, "while True:\n    pass\n");
        assertThat(result.status()).isIn(ExecutionStatus.TIMEOUT, ExecutionStatus.LIMIT_EXCEEDED);
    }

    @Test
    void fileSystemIsNotReachable() {
        ExecutionResult result = run(Language.PYTHON, "print(open('/etc/passwd').read())");
        assertThat(result.status()).isNotEqualTo(ExecutionStatus.SUCCESS);
        assertThat(result.output()).doesNotContain("root:");
    }

    @Test
    void javaClassesAreNotReachableFromJavaScript() {
        ExecutionResult result = run(Language.JAVASCRIPT,
                "console.log(Java.type('java.lang.System').getProperty('user.home'))");
        assertThat(result.status()).isNotEqualTo(ExecutionStatus.SUCCESS);
    }

    @Test
    void syntaxErrorsAreReportedSeparately() {
        ExecutionResult result = run(Language.PYTHON, "def broken(:\n    pass\n");
        assertThat(result.status()).isEqualTo(ExecutionStatus.SYNTAX_ERROR);
    }

    @Test
    void hugeOutputIsTruncated() {
        ExecutionResult result = run(Language.PYTHON, "print('x' * 50000)");
        assertThat(result.output()).contains("output truncated");
        assertThat(result.output().length()).isLessThan(11_000);
    }
}
