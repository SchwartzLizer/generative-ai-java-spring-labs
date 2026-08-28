package com.schwartzlizer.ai.response;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResponseDraftController.class)
@Import({ResponsePromptFactory.class, ResponseDraftService.class, DemoSupportResponseAiClient.class, ResponseExceptionHandler.class})
class ResponseDraftControllerTest {
    @Autowired
    MockMvc mvc;

    @Test
    void createsDraft() throws Exception {
        mvc.perform(post("/api/v1/response-drafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"issue\":\"Shipment is late\",\"tone\":\"empathetic\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.provider").value("demo"))
                .andExpect(jsonPath("$.content").isNotEmpty());
    }

    @Test
    void rejectsBlankIssue() throws Exception {
        mvc.perform(post("/api/v1/response-drafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"issue\":\" \",\"tone\":\"empathetic\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.issue").exists());
    }

    @Test
    void rejectsUnsupportedTone() throws Exception {
        mvc.perform(post("/api/v1/response-drafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"issue\":\"Shipment is late\",\"tone\":\"robotic\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.tone").exists());
    }
}
