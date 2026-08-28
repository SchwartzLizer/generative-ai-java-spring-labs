package com.schwartzlizer.ai.chat;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatServiceTest {
    @Test
    void returnsProviderReplyAndName() {
        CustomerSupportAiClient client = message -> "Please restart the app and retry.";
        var service = new ChatService(client, "demo");

        assertThat(service.reply("The app crashes")).isEqualTo(
                new ChatResponse("Please restart the app and retry.", "demo"));
    }

    @Test
    void rejectsBlankMessage() {
        var service = new ChatService(message -> "unused", "demo");
        assertThatThrownBy(() -> service.reply(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Message is required");
    }
}
