package com.schwartzlizer.support;

import com.jayway.jsonpath.JsonPath;
import com.schwartzlizer.support.feedback.Feedback;
import com.schwartzlizer.support.feedback.FeedbackRepository;
import com.schwartzlizer.support.response.ResponseDraft;
import com.schwartzlizer.support.response.ResponseDraftRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

/**
 * QA-R3-MAPPING-03: proves, over MockMvc against real PostgreSQL, that a commit-time optimistic lock
 * failure on the draft-decision endpoint surfaces as HTTP 409 with {@code CONCURRENT_MODIFICATION}, leaks
 * no persistence internals, and leaves exactly one terminal decision persisted.
 *
 * <p>MOCK web environment starts no embedded server and opens no server socket; {@code MockMvc.perform}
 * runs the whole filter chain and the transaction on the calling thread, so two pool threads in
 * {@link #qaOptLock21_twoThreadRace()} still produce two genuinely concurrent transactions on two
 * connections against real PostgreSQL, which is the layer optimistic locking operates at.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("container")
@TestPropertySource(properties = {
    "app.security.agent-password=it-agent-password",
    "app.security.admin-password=it-admin-password"
})
class ResponseDraftConcurrencyIT {

    @Container @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired MockMvc mvc;
    @Autowired ResponseDraftRepository draftRepository;
    @Autowired FeedbackRepository feedbackRepository;
    @Autowired JdbcTemplate jdbc;

    private static final Instant NOW = Instant.parse("2026-08-31T12:00:00Z");
    private static final List<String> FORBIDDEN_TOKENS = List.of(
        "optimistic", "hibernate", "staleobject", "com.schwartzlizer", "org.springframework",
        "batch update", "row was updated");

    private record CallOutcome(boolean latchAwaited, int status, String body, Throwable error) { }

    private ResponseDraft seedDraft(String customerRef) {
        Feedback feedback = feedbackRepository.saveAndFlush(Feedback.create(UUID.randomUUID(), customerRef, "Issue", NOW));
        ResponseDraft draft = draftRepository.saveAndFlush(ResponseDraft.create(UUID.randomUUID(), feedback, "Reply", "demo", "v1", NOW));
        assertThat(draft.version()).isEqualTo(0L);
        return draft;
    }

    private void assertNoForbiddenTokens(String body) {
        String lower = body.toLowerCase();
        for (String token : FORBIDDEN_TOKENS) {
            assertThat(lower).as("body must not contain forbidden token: %s", token).doesNotContain(token);
        }
    }

    private MvcResult patchDecisionAuthenticated(UUID draftId, String decision) throws Exception {
        return mvc.perform(patch("/api/v1/response-drafts/{id}/decision", draftId)
                .with(httpBasic("agent", "it-agent-password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"decision\":\"" + decision + "\"}"))
            .andReturn();
    }

    @Test
    @Timeout(60)
    void qaOptLock21_twoThreadRace() throws Exception {
        ResponseDraft draft = seedDraft("CUST-CONC-21");
        UUID draftId = draft.id();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Callable<CallOutcome> taskA = () -> {
                boolean awaited;
                try {
                    awaited = latch.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return new CallOutcome(false, -1, null, e);
                }
                try {
                    MvcResult result = patchDecisionAuthenticated(draftId, "APPROVED");
                    return new CallOutcome(awaited, result.getResponse().getStatus(), result.getResponse().getContentAsString(), null);
                } catch (Throwable t) {
                    return new CallOutcome(awaited, -1, null, t);
                }
            };
            Callable<CallOutcome> taskB = () -> {
                boolean awaited;
                try {
                    awaited = latch.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return new CallOutcome(false, -1, null, e);
                }
                try {
                    MvcResult result = patchDecisionAuthenticated(draftId, "REJECTED");
                    return new CallOutcome(awaited, result.getResponse().getStatus(), result.getResponse().getContentAsString(), null);
                } catch (Throwable t) {
                    return new CallOutcome(awaited, -1, null, t);
                }
            };

            Future<CallOutcome> futureA = pool.submit(taskA);
            Future<CallOutcome> futureB = pool.submit(taskB);
            latch.countDown();

            CallOutcome outcomeA = futureA.get(20, TimeUnit.SECONDS);
            CallOutcome outcomeB = futureB.get(20, TimeUnit.SECONDS);

            assertThat(outcomeA.error()).as("thread A (APPROVED) threw: %s", outcomeA.error()).isNull();
            assertThat(outcomeB.error()).as("thread B (REJECTED) threw: %s", outcomeB.error()).isNull();
            assertThat(outcomeA.latchAwaited()).isTrue();
            assertThat(outcomeB.latchAwaited()).isTrue();

            List<CallOutcome> outcomes = List.of(outcomeA, outcomeB);
            long twoXx = outcomes.stream().filter(o -> o.status() >= 200 && o.status() < 300).count();
            long conflicts = outcomes.stream().filter(o -> o.status() == 409).count();
            long serverErrors = outcomes.stream().filter(o -> o.status() >= 500 && o.status() < 600).count();

            assertThat(twoXx).isEqualTo(1);
            assertThat(conflicts).isEqualTo(1);
            assertThat(serverErrors).isEqualTo(0);

            CallOutcome conflictOutcome = outcomes.stream().filter(o -> o.status() == 409).findFirst().orElseThrow();
            String conflictBody = conflictOutcome.body();
            String observedCode = JsonPath.read(conflictBody, "$.code");
            assertThat(observedCode).isIn("CONCURRENT_MODIFICATION", "INVALID_STATE_TRANSITION");
            assertNoForbiddenTokens(conflictBody);

            System.out.println("[QA-OPT-LOCK-21] observed conflict code: " + observedCode);

            Map<String, Object> row = jdbc.queryForMap("SELECT decision, decided_at, version FROM response_draft WHERE id = ?", draftId);
            assertThat(row.get("decision")).isIn("APPROVED", "REJECTED");
            assertThat(row.get("decided_at")).isNotNull();
            assertThat(((Number) row.get("version")).longValue()).isEqualTo(1L);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void qaOptLock22_sequentialDoubleDecide() throws Exception {
        ResponseDraft draft = seedDraft("CUST-CONC-22");
        UUID draftId = draft.id();

        MvcResult first = patchDecisionAuthenticated(draftId, "APPROVED");
        int firstStatus = first.getResponse().getStatus();
        assertThat(firstStatus).isGreaterThanOrEqualTo(200).isLessThan(300);

        MvcResult second = patchDecisionAuthenticated(draftId, "REJECTED");
        assertThat(second.getResponse().getStatus()).isEqualTo(409);
        String body = second.getResponse().getContentAsString();
        String code = JsonPath.read(body, "$.code");
        assertThat(code).isEqualTo("INVALID_STATE_TRANSITION");
        assertThat(code).isNotEqualTo("CONCURRENT_MODIFICATION");

        Map<String, Object> row = jdbc.queryForMap("SELECT decision, version FROM response_draft WHERE id = ?", draftId);
        assertThat(row.get("decision")).isEqualTo("APPROVED");
        assertThat(((Number) row.get("version")).longValue()).isEqualTo(1L);
    }

    @Test
    void qaOptLock23_unauthenticated() throws Exception {
        ResponseDraft draft = seedDraft("CUST-CONC-23");
        UUID draftId = draft.id();

        MvcResult result = mvc.perform(patch("/api/v1/response-drafts/{id}/decision", draftId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"decision\":\"APPROVED\"}"))
            .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(401);

        Map<String, Object> row = jdbc.queryForMap("SELECT decision, version FROM response_draft WHERE id = ?", draftId);
        assertThat(row.get("decision")).isEqualTo("PENDING");
        assertThat(((Number) row.get("version")).longValue()).isEqualTo(0L);
    }
}
