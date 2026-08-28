package com.schwartzlizer.ai.chat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@Import({ChatService.class, DemoCustomerSupportAiClient.class, ChatExceptionHandler.class})
class ChatControllerTest {
    @Autowired
    MockMvc mvc;

    @Test
    void returnsChatResponse() throws Exception {
        mvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"The app crashes\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("demo"))
                .andExpect(jsonPath("$.reply").isNotEmpty());
    }

    @Test
    void rejectsBlankMessage() throws Exception {
        mvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.message").exists());
    }
}
