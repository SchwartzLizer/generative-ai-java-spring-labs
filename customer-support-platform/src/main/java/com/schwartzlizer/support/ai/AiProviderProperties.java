package com.schwartzlizer.support.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.time.Duration;

@ConfigurationProperties("app.ai")
public record AiProviderProperties(String provider, String model, Duration timeout) {
    public AiProviderProperties(String provider, String model) {
        this(provider, model, Duration.ofSeconds(10));
    }

    @ConstructorBinding
    public AiProviderProperties {
        provider = provider == null || provider.isBlank() ? "demo" : provider;
        model = model == null || model.isBlank() ? "demo-rules-v1" : model;
        timeout = timeout == null ? Duration.ofSeconds(10) : timeout;
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("AI timeout must be positive");
        }
    }
}
