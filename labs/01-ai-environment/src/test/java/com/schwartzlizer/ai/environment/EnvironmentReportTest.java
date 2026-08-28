package com.schwartzlizer.ai.environment;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnvironmentReportTest {
    private final EnvironmentReport report = new EnvironmentReport();

    @Test
    void formatsVerifiedRuntime() {
        assertThat(report.create("OpenJDK", "21.0.8"))
                .isEqualTo("Java AI environment ready: OpenJDK 21.0.8");
    }

    @Test
    void rejectsBlankRuntimeName() {
        assertThatThrownBy(() -> report.create(" ", "21.0.8"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Runtime name is required");
    }
}
