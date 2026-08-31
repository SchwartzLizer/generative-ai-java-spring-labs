package com.schwartzlizer.support.common;

import com.schwartzlizer.support.response.DraftDecision;
import com.schwartzlizer.support.response.ResponseDraft;
import com.schwartzlizer.support.response.ResponseDraftService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

/**
 * QA-R3-MAPPING-04 / QA-OPT-LOCK-24: proves, without Docker, that {@link GlobalExceptionHandler} maps any
 * {@link org.springframework.dao.OptimisticLockingFailureException} subtype thrown by
 * {@link ResponseDraftService#decide} to HTTP 409 {@code CONCURRENT_MODIFICATION} with the fixed literal
 * message, leaking no persistence internals from the exception's own message text.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConcurrentModificationMappingTest {

    @Autowired MockMvc mvc;
    @MockitoBean ResponseDraftService service;

    static final UUID DRAFT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final List<String> FORBIDDEN_TOKENS = List.of(
        "optimistic", "hibernate", "staleobject", "com.schwartzlizer", "org.springframework",
        "batch update", "row was updated");

    @Test
    void qaOptLock24_optimisticLockingFailureMapsToConcurrentModification() throws Exception {
        given(service.decide(DRAFT_ID, DraftDecision.APPROVED))
            .willThrow(new ObjectOptimisticLockingFailureException(ResponseDraft.class, DRAFT_ID));

        MvcResult result = mvc.perform(patch("/api/v1/response-drafts/{id}/decision", DRAFT_ID)
                .with(httpBasic("agent", "test-agent-password"))
                .contentType(APPLICATION_JSON)
                .content("{\"decision\":\"APPROVED\"}"))
            .andReturn();

        int status = result.getResponse().getStatus();
        String body = result.getResponse().getContentAsString();

        assertThat(status).isEqualTo(409);
        assertThat(result.getResponse().getContentType()).isNotNull();
        assertThat(result.getResponse().getContentType()).contains("application/json");
        assertThat((String) com.jayway.jsonpath.JsonPath.read(body, "$.code")).isEqualTo("CONCURRENT_MODIFICATION");
        assertThat((String) com.jayway.jsonpath.JsonPath.read(body, "$.message")).isEqualTo("The record was changed by another request. Reload it and try again");
        assertThat((String) com.jayway.jsonpath.JsonPath.read(body, "$.path")).isEqualTo("/api/v1/response-drafts/11111111-1111-1111-1111-111111111111/decision");
        Object timestamp = com.jayway.jsonpath.JsonPath.read(body, "$.timestamp");
        assertThat(timestamp).isNotNull();
        assertThat(timestamp.toString()).isNotBlank();
        Map<String, Object> fieldErrors = com.jayway.jsonpath.JsonPath.read(body, "$.fieldErrors");
        assertThat(fieldErrors).isEmpty();

        String lower = body.toLowerCase();
        for (String token : FORBIDDEN_TOKENS) {
            assertThat(lower).as("body must not contain forbidden token: %s", token).doesNotContain(token);
        }

        verify(service, times(1)).decide(DRAFT_ID, DraftDecision.APPROVED);
    }
}
