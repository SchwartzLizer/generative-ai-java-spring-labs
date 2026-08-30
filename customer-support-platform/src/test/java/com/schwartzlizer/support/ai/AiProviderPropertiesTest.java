package com.schwartzlizer.support.ai;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class AiProviderPropertiesTest {
    @Test
    void defaultsTimeoutToTenSecondsWhenAbsent() {
        AiProviderProperties properties = new AiProviderProperties(null, null, null);

        assertThat(properties.provider()).isEqualTo("demo");
        assertThat(properties.model()).isEqualTo("demo-rules-v1");
        assertThat(properties.timeout()).isEqualTo(Duration.ofSeconds(10));
    }
}
