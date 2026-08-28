package com.schwartzlizer.ai.response;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseDraftServiceTest {
    @Test
    void buildsPromptAndReturnsDraft() {
        AtomicReference<String> prompt = new AtomicReference<>();
        SupportResponseAiClient client = value -> {
            prompt.set(value);
            return "We are sorry about the delay. We are checking the shipment.";
        };
        var service = new ResponseDraftService(new ResponsePromptFactory(), client, "demo");

        ResponseDraft draft = service.create("Shipment is late", "empathetic");

        assertThat(draft.provider()).isEqualTo("demo");
        assertThat(draft.content()).contains("sorry");
        assertThat(prompt.get()).contains("empathetic", "Shipment is late")
                .contains("Do not promise refunds or dates");
    }
}
