package com.schwartzlizer.support.ai;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

class AiProviderConfigurationTest {
    @Test
    void rejectsSubMillisecondTimeout() {
        assertThatThrownBy(() -> AiProviderConfiguration.validateTimeout(Duration.ofNanos(1)))
            .isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    void rejectsTimeoutAboveHttpClientMaximum() {
        assertThatThrownBy(() -> AiProviderConfiguration.validateTimeout(Duration.ofMillis(Integer.MAX_VALUE).plusMillis(1)))
            .isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    void acceptsOneMillisecondTimeout() {
        assertThat(AiProviderConfiguration.validateTimeout(Duration.ofMillis(1))).isEqualTo(1);
    }
}
