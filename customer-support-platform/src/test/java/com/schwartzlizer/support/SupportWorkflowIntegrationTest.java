package com.schwartzlizer.support;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SupportWorkflowIntegrationTest {
    @Autowired MockMvc mvc;
    @Test void supportsFeedbackAnalysisDraftAndSummaryFlow() throws Exception {
        String feedbackId = mvc.perform(post("/api/v1/feedback").with(httpBasic("agent","test-agent-password")).contentType(APPLICATION_JSON).content("{\"customerReference\":\"CUST-DEMO\",\"message\":\"The app crashes during checkout\"}"))
            .andExpect(status().isCreated()).andExpect(header().string("X-Correlation-ID", org.hamcrest.Matchers.matchesPattern("[0-9a-f-]{36}"))).andReturn().getResponse().getContentAsString();
        String id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build().readTree(feedbackId).get("id").asText();
        mvc.perform(post("/api/v1/feedback/"+id+"/analyses").with(httpBasic("agent","test-agent-password"))).andExpect(status().isCreated()).andExpect(jsonPath("$.category").value("TECHNICAL"));
        mvc.perform(post("/api/v1/feedback/"+id+"/response-drafts").with(httpBasic("agent","test-agent-password"))).andExpect(status().isCreated()).andExpect(jsonPath("$.decision").value("PENDING"));
        mvc.perform(get("/api/v1/dashboard/summary").with(httpBasic("agent","test-agent-password"))).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1));
    }
    @Test void anonymousApiIsUnauthorized() throws Exception { mvc.perform(get("/api/v1/dashboard/summary")).andExpect(status().isUnauthorized()); }
}
