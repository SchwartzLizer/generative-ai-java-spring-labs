package com.schwartzlizer.support.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.ai")
public record AiProviderProperties(String provider, String model) {
    public AiProviderProperties { provider=provider == null || provider.isBlank() ? "demo" : provider; model=model == null || model.isBlank() ? "demo-rules-v1" : model; }
}
