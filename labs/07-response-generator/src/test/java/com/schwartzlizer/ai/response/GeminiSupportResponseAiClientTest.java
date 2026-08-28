package com.schwartzlizer.ai.response;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeminiSupportResponseAiClientTest {
    @Test
    void passesPromptToGateway() {
        AtomicReference<String> captured = new AtomicReference<>();
        var client = new GeminiSupportResponseAiClient(prompt -> {
            captured.set(prompt);
            return "safe draft";
        });

        assertThat(client.generate("Draft a response")).isEqualTo("safe draft");
        assertThat(captured).hasValue("Draft a response");
    }

    @Test
    void rejectsBlankProviderContent() {
        assertThatThrownBy(() -> new GeminiSupportResponseAiClient(prompt -> " ")
                .generate("Draft a response"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("AI provider returned an empty response");
    }
}
