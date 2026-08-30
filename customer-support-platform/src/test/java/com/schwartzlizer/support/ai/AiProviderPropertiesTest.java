package com.schwartzlizer.support.ai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThat;

class AiProviderPropertiesTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(TestConfig.class)
        .withPropertyValues("app.ai.provider=demo", "app.ai.model=demo-rules-v1");

    @Test
    void bindsDefaultTimeoutWhenPropertyIsAbsent() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(AiProviderProperties.class).timeout()).isEqualTo(Duration.ofSeconds(10));
        });
    }

    @EnableConfigurationProperties(AiProviderProperties.class)
    static class TestConfig { }
}
