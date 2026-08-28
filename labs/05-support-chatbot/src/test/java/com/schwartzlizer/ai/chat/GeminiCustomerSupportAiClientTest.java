package com.schwartzlizer.ai.chat;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeminiCustomerSupportAiClientTest {
    @Test
    void sendsBoundedSupportPrompt() {
        AtomicReference<String> prompt = new AtomicReference<>();
        ChatCompletionGateway gateway = value -> {
            prompt.set(value);
            return "Draft reply";
        };

        String reply = new GeminiCustomerSupportAiClient(gateway)
                .reply("The app crashes on upload");

        assertThat(reply).isEqualTo("Draft reply");
        assertThat(prompt.get())
                .contains("customer support assistant")
                .contains("The app crashes on upload")
                .contains("Do not invent account details");
    }

    @Test
    void rejectsBlankProviderOutput() {
        assertThatThrownBy(() -> new GeminiCustomerSupportAiClient(prompt -> " ")
                .reply("The app crashes"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("AI provider returned an empty response");
    }
}
