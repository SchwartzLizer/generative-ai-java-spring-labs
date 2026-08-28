# Flagship Customer Support Platform Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the portfolio-quality Spring Boot customer support platform, verify it in CI with PostgreSQL, document it, and publish the complete monorepo as a public GitHub release.

**Architecture:** A modular monolith uses package-by-feature for feedback, analysis, response drafts, dashboard, AI, security, and common HTTP concerns. PostgreSQL and Flyway own persistence; a narrow AI port supports deterministic demo and Spring AI Gemini providers.

**Tech Stack:** Java 21, Spring Boot 4.1.1, Spring AI 2.0.1, Google GenAI `gemini-2.5-flash`, Spring MVC, Thymeleaf, Spring Data JPA, PostgreSQL, Flyway, Spring Security, Actuator, springdoc-openapi 3.1.0, Testcontainers, Docker Compose, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-08-28-generative-ai-java-spring-portfolio-design.md`

## Global Constraints

- The default AI provider is deterministic `demo`; live Gemini requires environment configuration.
- UUIDs identify API resources and `Instant` stores timestamps.
- REST DTOs never expose JPA entities.
- Feedback, analyses, and drafts store only synthetic portfolio data.
- API keys, authorization headers, raw feedback, prompts, and provider responses are excluded from logs.
- State transitions are enforced in domain methods and tested without Spring.
- Container tests use `@Testcontainers(disabledWithoutDocker = true)` locally and must run in GitHub Actions where Docker is available.
- Local Docker verification is reported as unavailable until Docker exists; CI evidence is required before release.
- Behavioral code follows red-green-refactor; configuration, templates, workflows, and documentation use focused command or render verification.

---

### Task 1: Flagship Module and Dependency Baseline

**Files:**
- Modify: `pom.xml`
- Create: `customer-support-platform/pom.xml`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/CustomerSupportPlatformApplication.java`
- Create: `customer-support-platform/src/main/resources/application.yml`
- Create: `customer-support-platform/src/test/resources/application-test.yml`

**Interfaces:**
- Produces: Spring Boot artifact `com.schwartzlizer.ai:customer-support-platform`.
- Produces: application profiles `default`, `gemini`, `test`, and `container`.
- Consumes: root Spring Boot and Spring AI BOMs from Plan 2.

- [ ] **Step 1: Add root module and springdoc version**

Add `<module>customer-support-platform</module>` and property:

```xml
<springdoc.version>3.1.0</springdoc.version>
```

- [ ] **Step 2: Create module POM**

Declare:

```xml
<dependencies>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-webmvc</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-thymeleaf</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-actuator</artifactId></dependency>
    <dependency><groupId>org.springframework.ai</groupId><artifactId>spring-ai-starter-model-google-genai</artifactId></dependency>
    <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-database-postgresql</artifactId></dependency>
    <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><scope>runtime</scope></dependency>
    <dependency><groupId>org.springdoc</groupId><artifactId>springdoc-openapi-starter-webmvc-ui</artifactId><version>${springdoc.version}</version></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-testcontainers</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.springframework.security</groupId><artifactId>spring-security-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.testcontainers</groupId><artifactId>testcontainers-junit-jupiter</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.testcontainers</groupId><artifactId>postgresql</artifactId><scope>test</scope></dependency>
</dependencies>
```

Configure Spring Boot `repackage` and Maven Failsafe for `*IT` tests.

- [ ] **Step 3: Add application entrypoint and base configuration**

Default `application.yml` reads datasource and security credentials from environment variables, sets `app.ai.provider=demo`, and sets `spring.ai.model.chat=none`. `application-gemini.yml` activates on profile `gemini`, sets `app.ai.provider=gemini`, sets `spring.ai.model.chat=google-genai`, reads `GEMINI_API_KEY`, and selects `gemini-2.5-flash`. Set `spring.jpa.hibernate.ddl-auto=validate`; Flyway remains enabled. Expose only `health` and `info` Actuator endpoints.

- [ ] **Step 4: Verify module configuration**

```powershell
.\mvnw.cmd -pl customer-support-platform -DskipTests package
git diff --check
```

- [ ] **Step 5: Commit**

```powershell
git add pom.xml customer-support-platform
git commit -m "build: scaffold flagship customer support platform"
```

---

### Task 2: Feedback and Draft Domain Lifecycles

**Files:**
- Create: `customer-support-platform/src/test/java/com/schwartzlizer/support/feedback/FeedbackTest.java`
- Create: `customer-support-platform/src/test/java/com/schwartzlizer/support/response/ResponseDraftTest.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/feedback/FeedbackStatus.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/feedback/Feedback.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/analysis/Sentiment.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/analysis/SupportCategory.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/analysis/Urgency.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/analysis/FeedbackAnalysis.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/response/DraftDecision.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/response/ResponseDraft.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/common/InvalidStateTransitionException.java`

**Interfaces:**
- Produces: `Feedback#create(UUID, String, String, Instant): Feedback`.
- Produces: `Feedback#changeStatus(FeedbackStatus, Instant): void`.
- Produces: `ResponseDraft#approve(Instant): void` and `#reject(Instant): void`.
- Produces: JPA entities with protected no-argument constructors and domain factories.

- [ ] **Step 1: Write failing feedback transition tests**

```java
class FeedbackTest {
    private final Instant now = Instant.parse("2026-08-28T12:00:00Z");

    @Test
    void newFeedbackStartsInNewStatus() {
        Feedback feedback = Feedback.create(UUID.randomUUID(), "CUST-001", "App crashes", now);
        assertThat(feedback.status()).isEqualTo(FeedbackStatus.NEW);
        assertThat(feedback.createdAt()).isEqualTo(now);
    }

    @Test
    void followsAllowedLifecycle() {
        Feedback feedback = Feedback.create(UUID.randomUUID(), "CUST-001", "App crashes", now);
        feedback.changeStatus(FeedbackStatus.ANALYZED, now.plusSeconds(1));
        feedback.changeStatus(FeedbackStatus.IN_PROGRESS, now.plusSeconds(2));
        feedback.changeStatus(FeedbackStatus.RESOLVED, now.plusSeconds(3));
        feedback.changeStatus(FeedbackStatus.CLOSED, now.plusSeconds(4));
        assertThat(feedback.status()).isEqualTo(FeedbackStatus.CLOSED);
    }

    @Test
    void rejectsClosingNewFeedback() {
        Feedback feedback = Feedback.create(UUID.randomUUID(), "CUST-001", "App crashes", now);
        assertThatThrownBy(() -> feedback.changeStatus(FeedbackStatus.CLOSED, now))
            .isInstanceOf(InvalidStateTransitionException.class)
            .hasMessage("Cannot change feedback status from NEW to CLOSED");
    }
}
```

Allowed transitions: `NEW -> ANALYZED|IN_PROGRESS`, `ANALYZED -> IN_PROGRESS`, `IN_PROGRESS -> RESOLVED`, `RESOLVED -> IN_PROGRESS|CLOSED`; `CLOSED` is terminal.

- [ ] **Step 2: Run RED, implement feedback entity, verify GREEN**

```powershell
.\mvnw.cmd -pl customer-support-platform -Dtest=FeedbackTest test
```

- [ ] **Step 3: Write failing draft decision tests**

Test new drafts start `PENDING`, approve sets `APPROVED` and `decidedAt`, reject sets `REJECTED`, and a second decision throws `InvalidStateTransitionException("Draft has already been decided")`.

- [ ] **Step 4: Implement analysis and draft entities and verify GREEN**

`FeedbackAnalysis` and `ResponseDraft` are append-only JPA entities linked to `Feedback`. Provider and model are required non-blank fields. Content and recommended action are required.

```powershell
.\mvnw.cmd -pl customer-support-platform -Dtest=FeedbackTest,ResponseDraftTest test
```

- [ ] **Step 5: Commit**

```powershell
git add customer-support-platform/src/main/java customer-support-platform/src/test/java
git commit -m "feat: define support workflow domain model"
```

---

### Task 3: Flyway Schema and PostgreSQL Repositories

**Files:**
- Create: `customer-support-platform/src/main/resources/db/migration/V1__create_support_tables.sql`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/feedback/FeedbackRepository.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/analysis/FeedbackAnalysisRepository.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/response/ResponseDraftRepository.java`
- Create: `customer-support-platform/src/test/java/com/schwartzlizer/support/persistence/SupportRepositoryIT.java`

**Interfaces:**
- Produces: tables `feedback`, `feedback_analysis`, and `response_draft`.
- Produces: Spring Data repositories keyed by UUID.
- Consumes: entities from Task 2.

- [ ] **Step 1: Write container integration test before migration**

```java
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@ActiveProfiles("container")
class SupportRepositoryIT {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired FeedbackRepository feedbackRepository;

    @Test
    void persistsFeedbackWithOptimisticVersion() {
        Instant now = Instant.parse("2026-08-28T12:00:00Z");
        Feedback saved = feedbackRepository.saveAndFlush(
            Feedback.create(UUID.randomUUID(), "CUST-001", "App crashes", now));

        assertThat(feedbackRepository.findById(saved.id()))
            .get().extracting(Feedback::status)
            .isEqualTo(FeedbackStatus.NEW);
    }
}
```

- [ ] **Step 2: Run RED in Docker-capable environment**

```powershell
.\mvnw.cmd -pl customer-support-platform -Dtest=SupportRepositoryIT test
```

Expected with Docker: context fails because the migration and repositories do not exist. Without Docker: test is skipped and RED evidence must be obtained later in GitHub Actions before merging this task.

- [ ] **Step 3: Create migration and repositories**

Use UUID primary keys, `TIMESTAMPTZ`, foreign keys with `ON DELETE CASCADE`, `BIGINT` optimistic version on feedback, enum values stored as constrained `VARCHAR`, and indexes on status, created time, sentiment, category, urgency, and feedback foreign keys.

- [ ] **Step 4: Verify GREEN in Docker-capable environment**

```powershell
.\mvnw.cmd -pl customer-support-platform -Dtest=SupportRepositoryIT test
```

- [ ] **Step 5: Commit**

```powershell
git add customer-support-platform/src/main/resources/db customer-support-platform/src/main/java customer-support-platform/src/test/java
git commit -m "feat: persist support workflow in PostgreSQL"
```

---

### Task 4: Common API Errors and Feedback REST API

**Files:**
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/common/ApiError.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/common/ResourceNotFoundException.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/common/GlobalExceptionHandler.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/feedback/SubmitFeedbackRequest.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/feedback/UpdateFeedbackStatusRequest.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/feedback/FeedbackResponse.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/feedback/FeedbackService.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/feedback/FeedbackController.java`
- Create: `customer-support-platform/src/test/java/com/schwartzlizer/support/feedback/FeedbackServiceTest.java`
- Create: `customer-support-platform/src/test/java/com/schwartzlizer/support/feedback/FeedbackControllerTest.java`

**Interfaces:**
- Produces: submit, list, get, and status endpoints under `/api/v1/feedback`.
- Produces: paginated `Page<FeedbackResponse>`.
- Produces: standard `ApiError(code, message, timestamp, path, fieldErrors)`.
- Consumes: `FeedbackRepository` and domain behavior.

- [ ] **Step 1: Write failing service tests**

Use a mocked repository boundary to verify `submit` trims values, generates UUID through injected `Supplier<UUID>`, uses injected `Clock`, and saves `NEW` feedback. Verify missing lookup throws `ResourceNotFoundException("Feedback was not found")` and invalid transition is preserved.

- [ ] **Step 2: Run RED, implement service, verify GREEN**

```powershell
.\mvnw.cmd -pl customer-support-platform -Dtest=FeedbackServiceTest test
```

- [ ] **Step 3: Write failing MVC tests**

```java
@WebMvcTest(FeedbackController.class)
@Import(GlobalExceptionHandler.class)
class FeedbackControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean FeedbackService service;

    @Test
    void rejectsBlankFeedback() throws Exception {
        mvc.perform(post("/api/v1/feedback")
                .with(httpBasic("agent", "test-password"))
                .contentType(APPLICATION_JSON)
                .content("{\"customerReference\":\"CUST-001\",\"message\":\" \"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
            .andExpect(jsonPath("$.fieldErrors.message").exists());
    }
}
```

Add valid submit `201`, paginated list `200`, missing feedback `404`, invalid status transition `409`, and malformed UUID `400` tests.

- [ ] **Step 4: Implement DTOs, controller, and advice**

Limits: customer reference 1–100 characters, message 1–4,000 characters, page size 1–100. `Location` header points to the created feedback URL.

- [ ] **Step 5: Verify GREEN and commit**

```powershell
.\mvnw.cmd -pl customer-support-platform -Dtest=FeedbackServiceTest,FeedbackControllerTest test
git add customer-support-platform/src
git commit -m "feat: expose validated feedback workflow API"
```

---

### Task 5: AI Port and Deterministic Demo Provider

**Files:**
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/ai/FeedbackAnalysisResult.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/ai/ResponseDraftResult.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/ai/CustomerSupportAiClient.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/ai/DemoCustomerSupportAiClient.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/ai/AiProviderProperties.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/ai/AiProviderConfiguration.java`
- Create: `customer-support-platform/src/test/java/com/schwartzlizer/support/ai/DemoCustomerSupportAiClientTest.java`

**Interfaces:**
- Produces: `CustomerSupportAiClient#analyze(String): FeedbackAnalysisResult`.
- Produces: `CustomerSupportAiClient#draftResponse(String, FeedbackAnalysisResult): ResponseDraftResult`.
- Produces: `AiProviderProperties(provider, model)`.

- [ ] **Step 1: Write failing demo-provider tests**

```java
class DemoCustomerSupportAiClientTest {
    private final CustomerSupportAiClient client = new DemoCustomerSupportAiClient();

    @Test
    void identifiesUrgentTechnicalFailure() {
        FeedbackAnalysisResult result = client.analyze(
            "Payment error blocks checkout and the app crashes");
        assertThat(result.sentiment()).isEqualTo(Sentiment.NEGATIVE);
        assertThat(result.category()).isEqualTo(SupportCategory.TECHNICAL);
        assertThat(result.urgency()).isEqualTo(Urgency.HIGH);
        assertThat(result.recommendedAction()).isNotBlank();
    }

    @Test
    void draftsSafeResponseWithoutInventedResolution() {
        FeedbackAnalysisResult analysis = new FeedbackAnalysisResult(
            Sentiment.NEGATIVE, SupportCategory.BILLING, Urgency.HIGH,
            "Route to billing support");
        String content = client.draftResponse("Unexpected charge", analysis).content();
        assertThat(content).containsIgnoringCase("billing")
            .doesNotContainIgnoringCase("refund approved");
    }
}
```

- [ ] **Step 2: Run RED, implement deterministic rules, verify GREEN**

Rules are documented immutable keyword sets. Urgency is high for `blocked`, `crash`, `fraud`, or `cannot pay`; category precedence is security, billing, technical, delivery, then general. Drafts acknowledge the issue, state the next action, and avoid unverified promises.

```powershell
.\mvnw.cmd -pl customer-support-platform -Dtest=DemoCustomerSupportAiClientTest test
```

- [ ] **Step 3: Add conditional default bean and commit**

Create demo bean when `app.ai.provider=demo` or missing. Add `@ConfigurationPropertiesScan` to the application.

```powershell
git add customer-support-platform/src
git commit -m "feat: add deterministic support AI provider"
```

---

### Task 6: Persisted Analysis Workflow

**Files:**
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/analysis/FeedbackAnalysisResponse.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/analysis/FeedbackAnalysisService.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/analysis/FeedbackAnalysisController.java`
- Create: `customer-support-platform/src/test/java/com/schwartzlizer/support/analysis/FeedbackAnalysisServiceTest.java`
- Create: `customer-support-platform/src/test/java/com/schwartzlizer/support/analysis/FeedbackAnalysisControllerTest.java`

**Interfaces:**
- Produces: `POST /api/v1/feedback/{feedbackId}/analyses`.
- Produces: append-only analysis record and `201 Created` response.
- Consumes: feedback repository, analysis repository, AI client, provider properties, UUID supplier, and clock.

- [ ] **Step 1: Write failing service test**

Use repositories mocked only at persistence boundary and a real fake AI client. Verify the service loads feedback, calls `analyze` once, saves provider/model metadata, changes `NEW` to `ANALYZED`, and does not replace earlier analyses.

- [ ] **Step 2: Run RED, implement service, verify GREEN**

```powershell
.\mvnw.cmd -pl customer-support-platform -Dtest=FeedbackAnalysisServiceTest test
```

- [ ] **Step 3: Write controller tests and implement endpoint**

Test success `201`, missing feedback `404`, AI unavailable `503` with code `AI_PROVIDER_UNAVAILABLE`, and forbidden anonymous access.

- [ ] **Step 4: Verify and commit**

```powershell
.\mvnw.cmd -pl customer-support-platform -Dtest=FeedbackAnalysisServiceTest,FeedbackAnalysisControllerTest test
git add customer-support-platform/src
git commit -m "feat: persist AI feedback analyses"
```

---

### Task 7: Response Draft Workflow

**Files:**
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/response/ResponseDraftResponse.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/response/DraftDecisionRequest.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/response/ResponseDraftService.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/response/ResponseDraftController.java`
- Create: `customer-support-platform/src/test/java/com/schwartzlizer/support/response/ResponseDraftServiceTest.java`
- Create: `customer-support-platform/src/test/java/com/schwartzlizer/support/response/ResponseDraftControllerTest.java`

**Interfaces:**
- Produces: `POST /api/v1/feedback/{feedbackId}/response-drafts`.
- Produces: `PATCH /api/v1/response-drafts/{draftId}/decision`.
- Consumes: latest analysis for feedback; generation fails with `409 ANALYSIS_REQUIRED` when none exists.

- [ ] **Step 1: Write failing service tests**

Verify generation creates a new pending draft with provider/model metadata, repeated generation appends rather than overwrites, missing analysis is rejected, approve/reject records decision time, and second decision is rejected.

- [ ] **Step 2: Run RED, implement service, verify GREEN**

```powershell
.\mvnw.cmd -pl customer-support-platform -Dtest=ResponseDraftServiceTest test
```

- [ ] **Step 3: Write MVC tests and implement controller**

Test generation `201`, decision `200`, invalid decision `400`, already-decided `409`, and anonymous `401`.

- [ ] **Step 4: Verify and commit**

```powershell
.\mvnw.cmd -pl customer-support-platform -Dtest=ResponseDraftServiceTest,ResponseDraftControllerTest test
git add customer-support-platform/src
git commit -m "feat: add auditable response draft workflow"
```

---

### Task 8: Dashboard Summary API

**Files:**
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/dashboard/DashboardSummary.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/dashboard/DashboardService.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/dashboard/DashboardController.java`
- Create: `customer-support-platform/src/test/java/com/schwartzlizer/support/dashboard/DashboardServiceTest.java`
- Create: `customer-support-platform/src/test/java/com/schwartzlizer/support/dashboard/DashboardControllerTest.java`
- Modify: repository interfaces with explicit count queries.

**Interfaces:**
- Produces: `GET /api/v1/dashboard/summary`.
- Produces: counts for total, open, urgent, pending drafts, and enum-grouped maps.

- [ ] **Step 1: Write failing summary service test**

Inject repository count suppliers and verify missing categories appear with zero counts. `open` includes `NEW`, `ANALYZED`, and `IN_PROGRESS`; it excludes `RESOLVED` and `CLOSED`.

- [ ] **Step 2: Implement service and repository count queries**

Use explicit JPQL or derived count methods. Do not load full entity collections to count them.

- [ ] **Step 3: Write controller test and implement endpoint**

Verify stable JSON property names and authenticated access.

- [ ] **Step 4: Verify and commit**

```powershell
.\mvnw.cmd -pl customer-support-platform -Dtest=DashboardServiceTest,DashboardControllerTest test
git add customer-support-platform/src
git commit -m "feat: expose support dashboard metrics"
```

---

### Task 9: Spring AI Gemini Provider

**Files:**
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/ai/ChatCompletionGateway.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/ai/AnalysisPromptFactory.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/ai/ResponsePromptFactory.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/ai/GeminiCustomerSupportAiClient.java`
- Modify: `customer-support-platform/src/main/java/com/schwartzlizer/support/ai/AiProviderConfiguration.java`
- Create: `customer-support-platform/src/test/java/com/schwartzlizer/support/ai/GeminiCustomerSupportAiClientTest.java`

**Interfaces:**
- Produces: Gemini implementation of `CustomerSupportAiClient`.
- Produces: strict JSON analysis contract with keys `sentiment`, `category`, `urgency`, and `recommendedAction`.
- Consumes: Spring AI `ChatClient.Builder` in configuration only and Jackson `ObjectMapper` for parsing.

- [ ] **Step 1: Write failing adapter tests**

Use a capturing fake gateway. Verify analysis prompt includes allowed enum values and an instruction to return JSON only. Verify valid JSON maps to domain enums, unknown enums and malformed JSON throw `AiProviderException`, and draft prompt prohibits invented refunds, dates, account actions, and policy claims.

- [ ] **Step 2: Run RED, implement factories and adapter, verify GREEN**

```powershell
.\mvnw.cmd -pl customer-support-platform -Dtest=GeminiCustomerSupportAiClientTest test
```

- [ ] **Step 3: Wire conditional Gemini configuration**

When profile `gemini` sets `app.ai.provider=gemini`, require non-blank `GEMINI_API_KEY`, enable `spring.ai.model.chat=google-genai`, set model `gemini-2.5-flash`, temperature `0.2`, and a bounded output token limit. Wrap `ChatClient` in the gateway lambda.

- [ ] **Step 4: Add configuration context tests and commit**

Use `ApplicationContextRunner` to verify demo is default, profile `gemini` without a key fails clearly, and demo selection does not create a Spring AI client.

```powershell
.\mvnw.cmd -pl customer-support-platform -Dtest=GeminiCustomerSupportAiClientTest,AiProviderConfigurationTest test
git add customer-support-platform/src
git commit -m "feat: integrate Gemini through Spring AI"
```

---

### Task 10: Role-Based Security

**Files:**
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/security/SecurityProperties.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/security/SecurityConfiguration.java`
- Create: `customer-support-platform/src/test/java/com/schwartzlizer/support/security/SecurityConfigurationTest.java`
- Modify: `customer-support-platform/src/main/resources/application.yml`

**Interfaces:**
- Produces: roles `AGENT` and `ADMIN`.
- Produces: form login for dashboard and HTTP Basic for `/api/**`.
- Consumes: environment credentials `APP_AGENT_PASSWORD` and `APP_ADMIN_PASSWORD`.

- [ ] **Step 1: Write failing security tests**

Verify anonymous API access returns `401`, anonymous dashboard access redirects to login, AGENT can submit/analyze/draft, ADMIN can access `/actuator/info`, AGENT cannot access non-health Actuator endpoints, and `/actuator/health/liveness` remains public.

- [ ] **Step 2: Run RED, implement configuration, verify GREEN**

Use in-memory users with delegating password encoding. Usernames default to `agent` and `admin`; passwords have no committed non-empty defaults and must come from environment outside tests. Test profile supplies synthetic credentials.

```powershell
.\mvnw.cmd -pl customer-support-platform -Dtest=SecurityConfigurationTest test
```

- [ ] **Step 3: Commit**

```powershell
git add customer-support-platform/src
git commit -m "feat: secure support workflows by role"
```

---

### Task 11: Thymeleaf Agent Dashboard

**Files:**
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/dashboard/DashboardPageController.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/feedback/FeedbackPageController.java`
- Create: `customer-support-platform/src/main/resources/templates/dashboard.html`
- Create: `customer-support-platform/src/main/resources/templates/feedback-detail.html`
- Create: `customer-support-platform/src/main/resources/templates/error.html`
- Create: `customer-support-platform/src/main/resources/static/css/app.css`
- Create: `customer-support-platform/src/test/java/com/schwartzlizer/support/dashboard/DashboardPageControllerTest.java`
- Create: `customer-support-platform/src/test/java/com/schwartzlizer/support/feedback/FeedbackPageControllerTest.java`

**Interfaces:**
- Produces: `/dashboard` and `/feedback/{feedbackId}` HTML pages.
- Consumes: dashboard, feedback, analysis, and response services.

- [ ] **Step 1: Write failing rendered-view tests**

Use `@WebMvcTest` and `@WithMockUser(roles="AGENT")`. Verify dashboard model contains summary and page, template is `dashboard`, detail view contains analysis and draft histories, and anonymous access redirects to login.

- [ ] **Step 2: Run RED, implement controllers and semantic templates**

Templates include summary cards, filter form, paginated table, detail sections, CSRF-protected actions, accessible labels, visible empty states, and no inline secrets or raw provider metadata.

- [ ] **Step 3: Verify GREEN and inspect HTML**

```powershell
.\mvnw.cmd -pl customer-support-platform -Dtest=DashboardPageControllerTest,FeedbackPageControllerTest test
```

- [ ] **Step 4: Commit**

```powershell
git add customer-support-platform/src
git commit -m "feat: add agent support dashboard"
```

---

### Task 12: OpenAPI, Health, and Correlation Logging

**Files:**
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/common/OpenApiConfiguration.java`
- Create: `customer-support-platform/src/main/java/com/schwartzlizer/support/common/CorrelationIdFilter.java`
- Create: `customer-support-platform/src/test/java/com/schwartzlizer/support/common/OperationalEndpointsTest.java`
- Modify: controller annotations and `application.yml`.

**Interfaces:**
- Produces: `/v3/api-docs`, local Swagger UI, health liveness/readiness, `X-Correlation-ID` response header.

- [ ] **Step 1: Write failing operational tests**

Verify supplied correlation ID is returned, absent ID produces a UUID-shaped value, health endpoint is reachable anonymously, and OpenAPI contains `/api/v1/feedback` plus HTTP Basic security scheme.

- [ ] **Step 2: Run RED, implement configuration/filter, verify GREEN**

Store correlation ID in MDC only for request duration and remove it in `finally`. Never log request/response bodies.

```powershell
.\mvnw.cmd -pl customer-support-platform -Dtest=OperationalEndpointsTest test
```

- [ ] **Step 3: Commit**

```powershell
git add customer-support-platform/src
git commit -m "feat: add API documentation and operational health"
```

---

### Task 13: Container Runtime and CI

**Files:**
- Create: `customer-support-platform/Dockerfile`
- Create: `compose.yaml`
- Create: `.env.example`
- Create: `.github/workflows/ci.yml`
- Create: `.github/workflows/dashboard-screenshot.yml`

**Interfaces:**
- Produces: app container on port 8080 and PostgreSQL 17 service.
- Produces: CI root verification, image build, and dashboard screenshot artifact.

- [ ] **Step 1: Add multi-stage Dockerfile**

Build stage uses an Eclipse Temurin 21 JDK image and root Maven Wrapper with `-pl customer-support-platform -am package`. Runtime stage uses Eclipse Temurin 21 JRE, runs as non-root user, exposes 8080, and starts the repackaged JAR.

- [ ] **Step 2: Add Compose configuration**

Compose defines PostgreSQL 17 with health check and application dependency on healthy database. Environment values come from `.env`; `.env.example` contains only synthetic usernames and variable names, never an API key value.

- [ ] **Step 3: Add CI workflow**

On push and pull request:

```text
checkout -> setup Java 21 with Maven cache -> ./mvnw verify -> docker build
```

Run PostgreSQL/Testcontainers tests on the GitHub runner. Fail on any skipped `*IT` caused by unavailable Docker by passing `-DfailIfNoTests=true` and checking Surefire/Failsafe reports for the container test class.

- [ ] **Step 4: Add manual screenshot workflow**

Use a PostgreSQL service container, start the application with demo AI and synthetic test credentials, submit sample feedback through authenticated API calls, log in with Playwright, and capture `dashboard.png` plus `feedback-detail.png` as workflow artifacts. The workflow never prints credentials.

- [ ] **Step 5: Validate locally where possible**

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress verify
```

If Docker remains unavailable, record `docker` checks as not locally verified and require successful GitHub workflow evidence before Task 15.

- [ ] **Step 6: Commit**

```powershell
git add customer-support-platform/Dockerfile compose.yaml .env.example .github
git commit -m "ci: verify and package portfolio application"
```

---

### Task 14: Portfolio Documentation

**Files:**
- Modify: `README.md`
- Modify: `docs/coursera-lab-mapping.md`
- Create: `docs/architecture.md`
- Create after verified runtime: `docs/images/dashboard.png`
- Create after verified runtime: `docs/images/feedback-detail.png`
- Create: `customer-support-platform/README.md`

**Interfaces:**
- Produces: five-minute reviewer path and complete Module 3 Part B mapping.
- Consumes: verified commands, endpoints, screenshots, and CI state from earlier tasks.

- [ ] **Step 1: Write architecture document**

Include Mermaid diagrams for system context and request flow, package ownership, AI provider boundary, security boundary, and database entity relationships. Match actual class and endpoint names.

- [ ] **Step 2: Complete course mapping**

Map Final Project Part B and Submission to flagship paths, tests, Docker runtime, and documentation. State repository evidence rather than claiming Coursera submission state.

- [ ] **Step 3: Rewrite root README for portfolio review**

Required order:

1. Product value and verified build badge.
2. Dashboard screenshots.
3. Feature and engineering-signal bullets.
4. Architecture diagram.
5. Quick start with demo provider.
6. Optional Gemini configuration using variable names only.
7. API examples with synthetic data.
8. Test commands and project map.
9. Course attribution and license.

- [ ] **Step 4: Add verified screenshots only**

Download artifacts from the successful screenshot workflow, visually inspect them, then place the exact PNG files under `docs/images`. Do not use mockups or generated screenshots as runtime evidence.

- [ ] **Step 5: Verify documentation links and commit**

```powershell
rg -n "TBD|TODO|YOUR_API_KEY|replace-me" README.md docs customer-support-platform/README.md
git diff --check
git add README.md docs customer-support-platform/README.md
git commit -m "docs: present Spring Boot AI portfolio"
```

Expected: placeholder scan returns no matches.

---

### Task 15: Public GitHub Release Verification

**Files:**
- No source files expected unless verification finds a defect.

**Interfaces:**
- Produces: public repository `https://github.com/SchwartzLizer/generative-ai-java-spring-labs`.
- Produces: main branch and annotated tag `v1.0.0` pointing to the verified release commit.
- Consumes: all prior plans and successful CI evidence.

- [ ] **Step 1: Run fresh local verification**

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
git diff --check
git status --short --branch
git log --oneline --decorate -10
```

Read complete Maven output. Do not infer success from partial module output.

- [ ] **Step 2: Scan tracked content and history for secrets**

```powershell
git grep -n -I -E "AIza[0-9A-Za-z_-]{30,}|GEMINI_API_KEY=.+|BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY|password: [^$]"
git log -p --all -- . ':!docs/superpowers/**' | rg -n "AIza[0-9A-Za-z_-]{30,}|BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY"
```

Expected: no secret matches. Review any generic `password` match as configuration before continuing.

- [ ] **Step 3: Create public GitHub repository**

Use the signed-in BrowserOS GitHub session. Confirm no existing `SchwartzLizer/generative-ai-java-spring-labs`, create it public without generated README/license/gitignore, and verify owner and visibility on the resulting page.

- [ ] **Step 4: Add remote and push main**

```powershell
git remote add origin https://github.com/SchwartzLizer/generative-ai-java-spring-labs.git
git push -u origin main
```

If credential manager cannot authenticate, use GitHub's browser-supported credential flow; never request or print a token in chat.

- [ ] **Step 5: Verify GitHub Actions**

Open the repository Actions page, wait for the full CI workflow, inspect every job, and require green Maven verification, container tests, and image build. If CI fails, reproduce or diagnose, add a failing regression test for behavioral defects, fix, and push another commit.

- [ ] **Step 6: Capture and commit screenshots**

Run the manual screenshot workflow, download artifacts, visually inspect them, add verified images, update README image links, rerun verification, commit, and push.

- [ ] **Step 7: Tag verified release**

```powershell
git tag -a v1.0.0 -m "Generative AI Java and Spring portfolio v1.0.0"
git push origin v1.0.0
```

- [ ] **Step 8: Final evidence check**

Verify exact public URL, owner, `main` default branch, latest commit, `v1.0.0` tag, README rendering, screenshots, and latest successful Actions run. Report local Maven result, CI result, Docker validation source, and anything not verified.
