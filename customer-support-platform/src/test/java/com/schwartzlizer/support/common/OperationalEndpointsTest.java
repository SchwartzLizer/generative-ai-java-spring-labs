package com.schwartzlizer.support.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OperationalEndpointsTest {
    @Autowired MockMvc mvc;
    @Test void preservesSuppliedCorrelationId() throws Exception { mvc.perform(get("/actuator/health/liveness").header(CorrelationIdFilter.HEADER,"trace-123")).andExpect(status().isOk()).andExpect(header().string(CorrelationIdFilter.HEADER,"trace-123")); }
    @Test void createsCorrelationIdAndDocumentsApi() throws Exception { mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(header().string(CorrelationIdFilter.HEADER,org.hamcrest.Matchers.matchesPattern("[0-9a-f-]{36}"))).andExpect(content().string(org.hamcrest.Matchers.containsString("/api/v1/feedback"))).andExpect(content().string(org.hamcrest.Matchers.containsString("basicAuth"))); }
    @Test void adminCanReadInfoButAgentCannot() throws Exception { mvc.perform(get("/actuator/info").with(httpBasic("admin","test-admin-password"))).andExpect(status().isOk()); mvc.perform(get("/actuator/info").with(httpBasic("agent","test-agent-password"))).andExpect(status().isForbidden()); }
    @Test void authenticatedAgentCanOpenDashboard() throws Exception { mvc.perform(get("/dashboard").with(user("agent").roles("AGENT"))).andExpect(status().isOk()).andExpect(view().name("dashboard")).andExpect(model().attributeExists("summary", "query", "feedbackPage")); }
}
