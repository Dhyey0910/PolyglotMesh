package com.polyglotmesh.sandbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.polyglotmesh.service.InteropDemoService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "polyglotmesh.hello=false")
class InteropDemoTest {

    @Autowired
    private InteropDemoService demoService;

    @SuppressWarnings("unchecked")
    @Test
    void pythonMutatesTheJavaMapInPlace() {
        Map<String, Object> response = demoService.run();
        Map<String, Object> after = (Map<String, Object>) response.get("after");
        Map<String, Object> before = (Map<String, Object>) response.get("before");

        assertThat(before).doesNotContainKey("total");
        assertThat(((Number) after.get("total")).doubleValue()).isCloseTo(270.0, within(0.001));
        assertThat(after.get("label")).isEqualTo("bulk");
    }
}
