# Spring AI Labs Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add original, tested Spring implementations for the customer-support chatbot, recommendation system, and response-generator labs.

**Architecture:** Labs 05 and 07 are independent Spring Boot REST applications with provider ports, deterministic demo providers, and optional Spring AI Google GenAI adapters. Lab 06 is a deterministic Java recommendation application. No lab imports production code from another lab.

**Tech Stack:** Java 21, Spring Boot 4.1.1, Spring AI 2.0.1, Google GenAI starter, Maven, JUnit 5, AssertJ, Spring MVC Test.

**Spec:** `docs/superpowers/specs/2026-08-28-generative-ai-java-spring-portfolio-design.md`

## Global Constraints

- Use the repository parent and Maven Wrapper created by Plan 1.
- Default AI provider is `demo`; tests never call Gemini.
- Live configuration uses `spring.ai.google.genai.api-key=${GEMINI_API_KEY}` and model `gemini-2.5-flash`.
- API keys, prompts containing customer text, and provider responses are not logged.
- Each Spring module owns its DTOs, errors, provider port, configuration, and tests.
- Behavioral code follows red-green-refactor; POM and configuration changes use focused validation.

---

### Task 1: Add Spring Dependency Management

**Files:**
- Modify: `pom.xml`

**Interfaces:**
- Produces: properties `spring-boot.version=4.1.1`, `spring-ai.version=2.0.1`.
- Produces: imported `spring-boot-dependencies` and `spring-ai-bom`.
- Produces: reactor modules for labs 05 through 07.
- Consumes: root build from Plan 1.

- [ ] **Step 1: Extend root modules and properties**

Add:

```xml
<module>labs/05-support-chatbot</module>
<module>labs/06-recommendation-system</module>
<module>labs/07-response-generator</module>
```

Add properties:

```xml
<spring-boot.version>4.1.1</spring-boot.version>
<spring-ai.version>2.0.1</spring-ai.version>
```

Import these BOMs after the JUnit BOM:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-dependencies</artifactId>
    <version>${spring-boot.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-bom</artifactId>
    <version>${spring-ai.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

- [ ] **Step 2: Verify configuration**

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress -N validate
git diff --check
```

- [ ] **Step 3: Commit**

```powershell
git add pom.xml
git commit -m "build: add Spring Boot and Spring AI dependency management"
```

---

### Task 2: Lab 05 Chat Service and Demo Provider

**Files:**
- Create: `labs/05-support-chatbot/pom.xml`
- Create: `labs/05-support-chatbot/src/test/java/com/schwartzlizer/ai/chat/ChatServiceTest.java`
- Create: `labs/05-support-chatbot/src/main/java/com/schwartzlizer/ai/chat/SupportChatbotApplication.java`
- Create: `labs/05-support-chatbot/src/main/java/com/schwartzlizer/ai/chat/ChatRequest.java`
- Create: `labs/05-support-chatbot/src/main/java/com/schwartzlizer/ai/chat/ChatResponse.java`
- Create: `labs/05-support-chatbot/src/main/java/com/schwartzlizer/ai/chat/CustomerSupportAiClient.java`
- Create: `labs/05-support-chatbot/src/main/java/com/schwartzlizer/ai/chat/DemoCustomerSupportAiClient.java`
- Create: `labs/05-support-chatbot/src/main/java/com/schwartzlizer/ai/chat/ChatService.java`

**Interfaces:**
- Produces: `CustomerSupportAiClient#reply(String): String`.
- Produces: `ChatService#reply(String): ChatResponse`.
- Produces: `record ChatRequest(@NotBlank @Size(max=2000) String message)`.
- Produces: `record ChatResponse(String reply, String provider)`.
- Consumes: Spring Boot Web MVC, Validation, and Test starters.

- [ ] **Step 1: Create module POM**

Declare `spring-boot-starter-webmvc`, `spring-boot-starter-validation`, `spring-ai-starter-model-google-genai`, and `spring-boot-starter-test`. Configure `spring-boot-maven-plugin` with `repackage` execution.

- [ ] **Step 2: Write failing service tests**

```java
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
```

- [ ] **Step 3: Run RED**

```powershell
.\mvnw.cmd -pl labs/05-support-chatbot test
```

- [ ] **Step 4: Implement port, service, and demo provider**

`DemoCustomerSupportAiClient` returns deterministic guidance:

- Messages containing `crash` or `error`: ask for restart, version, and reproduction steps.
- Messages containing `refund` or `charge`: acknowledge billing concern and route to billing support.
- Other messages: acknowledge and request relevant details.

Keep matching case-insensitive and return a non-blank response for every non-blank message.

- [ ] **Step 5: Verify GREEN**

```powershell
.\mvnw.cmd -pl labs/05-support-chatbot -Dtest=ChatServiceTest test
```

---

### Task 3: Lab 05 REST API and Error Contract

**Files:**
- Create: `labs/05-support-chatbot/src/test/java/com/schwartzlizer/ai/chat/ChatControllerTest.java`
- Create: `labs/05-support-chatbot/src/main/java/com/schwartzlizer/ai/chat/ChatController.java`
- Create: `labs/05-support-chatbot/src/main/java/com/schwartzlizer/ai/chat/ApiError.java`
- Create: `labs/05-support-chatbot/src/main/java/com/schwartzlizer/ai/chat/ChatExceptionHandler.java`

**Interfaces:**
- Produces: `POST /api/v1/chat`.
- Produces: validation error code `VALIDATION_FAILED` with field error map.
- Consumes: `ChatService` from Task 2.

- [ ] **Step 1: Write failing MVC tests**

```java
@WebMvcTest(ChatController.class)
@Import({ChatService.class, DemoCustomerSupportAiClient.class, ChatExceptionHandler.class})
class ChatControllerTest {
    @Autowired MockMvc mvc;

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
```

- [ ] **Step 2: Run RED**

```powershell
.\mvnw.cmd -pl labs/05-support-chatbot -Dtest=ChatControllerTest test
```

- [ ] **Step 3: Implement controller and advice**

`ChatController` uses `@RestController`, `@RequestMapping("/api/v1/chat")`, and `@Valid`. `ApiError` contains `code`, `message`, `timestamp`, and `fieldErrors`. `ChatExceptionHandler` maps validation and illegal arguments without returning stack traces.

- [ ] **Step 4: Verify GREEN**

```powershell
.\mvnw.cmd -pl labs/05-support-chatbot test
```

---

### Task 4: Lab 05 Spring AI Gemini Adapter

**Files:**
- Create: `labs/05-support-chatbot/src/test/java/com/schwartzlizer/ai/chat/GeminiCustomerSupportAiClientTest.java`
- Create: `labs/05-support-chatbot/src/main/java/com/schwartzlizer/ai/chat/ChatCompletionGateway.java`
- Create: `labs/05-support-chatbot/src/main/java/com/schwartzlizer/ai/chat/GeminiCustomerSupportAiClient.java`
- Create: `labs/05-support-chatbot/src/main/java/com/schwartzlizer/ai/chat/AiProviderConfiguration.java`
- Create: `labs/05-support-chatbot/src/main/resources/application.yml`
- Create: `labs/05-support-chatbot/README.md`

**Interfaces:**
- Produces: `ChatCompletionGateway#complete(String): String`.
- Produces: `GeminiCustomerSupportAiClient(ChatCompletionGateway)` implementing `CustomerSupportAiClient`.
- Consumes: Spring AI `ChatClient.Builder` only inside configuration.

- [ ] **Step 1: Write failing prompt test**

```java
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
}
```

- [ ] **Step 2: Run RED, implement adapter, verify GREEN**

The adapter rejects blank provider output with `IllegalStateException("AI provider returned an empty response")`.

```powershell
.\mvnw.cmd -pl labs/05-support-chatbot -Dtest=GeminiCustomerSupportAiClientTest test
```

- [ ] **Step 3: Add conditional provider configuration**

`application.yml`:

```yaml
app:
  ai:
    provider: ${AI_PROVIDER:demo}
spring:
  ai:
    model:
      chat: none
```

`application-gemini.yml`:

```yaml
app:
  ai:
    provider: gemini
spring:
  config:
    activate:
      on-profile: gemini
  ai:
    model:
      chat: google-genai
    google:
      genai:
        api-key: ${GEMINI_API_KEY:}
        chat:
          model: gemini-2.5-flash
```

Set `app.ai.provider=gemini` inside `application-gemini.yml`. Use `@ConditionalOnProperty` beans. Demo provider is active for `app.ai.provider=demo`. Gemini provider is active for `gemini` and constructs `ChatCompletionGateway` from `ChatClient.Builder`. Live execution requires `SPRING_PROFILES_ACTIVE=gemini` and `GEMINI_API_KEY`.

- [ ] **Step 4: Verify default application context and commit**

Add `@SpringBootTest` smoke test using the default demo provider, then run:

```powershell
.\mvnw.cmd -pl labs/05-support-chatbot verify
git add labs/05-support-chatbot
git commit -m "feat: add Spring AI support chatbot lab"
```

---

### Task 5: Lab 06 Recommendation Domain

**Files:**
- Create: `labs/06-recommendation-system/pom.xml`
- Create: `labs/06-recommendation-system/src/test/java/com/schwartzlizer/ai/recommendation/MovieRecommendationServiceTest.java`
- Create: `labs/06-recommendation-system/src/main/java/com/schwartzlizer/ai/recommendation/Movie.java`
- Create: `labs/06-recommendation-system/src/main/java/com/schwartzlizer/ai/recommendation/UserPreference.java`
- Create: `labs/06-recommendation-system/src/main/java/com/schwartzlizer/ai/recommendation/Recommendation.java`
- Create: `labs/06-recommendation-system/src/main/java/com/schwartzlizer/ai/recommendation/MovieRecommendationService.java`

**Interfaces:**
- Produces: `record Movie(String title, Set<String> genres, double rating)`.
- Produces: `record UserPreference(Set<String> genres, double minimumRating)`.
- Produces: `record Recommendation(Movie movie, double score)`.
- Produces: `MovieRecommendationService#recommend(List<Movie>, UserPreference, int): List<Recommendation>`.

- [ ] **Step 1: Write failing ranking tests**

```java
class MovieRecommendationServiceTest {
    private final MovieRecommendationService service = new MovieRecommendationService();

    @Test
    void ranksGenreMatchesBeforeRatingOnlyMatches() {
        var catalog = List.of(
            new Movie("Space Journey", Set.of("sci-fi"), 8.0),
            new Movie("Award Drama", Set.of("drama"), 9.8)
        );

        assertThat(service.recommend(
            catalog, new UserPreference(Set.of("sci-fi"), 7.0), 2))
            .extracting(result -> result.movie().title())
            .containsExactly("Space Journey", "Award Drama");
    }

    @Test
    void breaksEqualScoresByTitle() {
        var catalog = List.of(
            new Movie("Zulu", Set.of("drama"), 8.0),
            new Movie("Alpha", Set.of("drama"), 8.0)
        );

        assertThat(service.recommend(
            catalog, new UserPreference(Set.of("drama"), 0), 2))
            .extracting(result -> result.movie().title())
            .containsExactly("Alpha", "Zulu");
    }
}
```

- [ ] **Step 2: Run RED, implement scoring, verify GREEN**

Normalize genres to lower case. Score is `3.0 * matchingGenreCount + rating / 10.0`; subtract `1.0` when rating is below the preference minimum. Validate non-empty titles, rating `0..10`, positive limit, and non-null collections. Return an immutable list sorted by score descending then title ascending.

```powershell
.\mvnw.cmd -pl labs/06-recommendation-system test
```

- [ ] **Step 3: Add CLI, README, and commit**

Create `RecommendationApplication` with a small synthetic catalog and documented command. Configure executable JAR.

```powershell
git add labs/06-recommendation-system
git commit -m "feat: add deterministic movie recommendation lab"
```

---

### Task 6: Lab 07 Response Generator Domain and API

**Files:**
- Create: `labs/07-response-generator/pom.xml`
- Create: `labs/07-response-generator/src/test/java/com/schwartzlizer/ai/response/ResponseDraftServiceTest.java`
- Create: `labs/07-response-generator/src/test/java/com/schwartzlizer/ai/response/ResponseDraftControllerTest.java`
- Create: `labs/07-response-generator/src/main/java/com/schwartzlizer/ai/response/ResponseGeneratorApplication.java`
- Create: `labs/07-response-generator/src/main/java/com/schwartzlizer/ai/response/ResponseDraftRequest.java`
- Create: `labs/07-response-generator/src/main/java/com/schwartzlizer/ai/response/ResponseDraft.java`
- Create: `labs/07-response-generator/src/main/java/com/schwartzlizer/ai/response/SupportResponseAiClient.java`
- Create: `labs/07-response-generator/src/main/java/com/schwartzlizer/ai/response/DemoSupportResponseAiClient.java`
- Create: `labs/07-response-generator/src/main/java/com/schwartzlizer/ai/response/ResponsePromptFactory.java`
- Create: `labs/07-response-generator/src/main/java/com/schwartzlizer/ai/response/ResponseDraftService.java`
- Create: `labs/07-response-generator/src/main/java/com/schwartzlizer/ai/response/ResponseDraftController.java`
- Create: `labs/07-response-generator/src/main/java/com/schwartzlizer/ai/response/ResponseExceptionHandler.java`

**Interfaces:**
- Produces: `SupportResponseAiClient#generate(String prompt): String`.
- Produces: `ResponsePromptFactory#create(String issue, String tone): String`.
- Produces: `ResponseDraftService#create(String issue, String tone): ResponseDraft`.
- Produces: `POST /api/v1/response-drafts`.

- [ ] **Step 1: Write failing prompt and service tests**

```java
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
```

- [ ] **Step 2: Run RED, implement domain behavior, verify GREEN**

Allowed tones are `professional`, `empathetic`, and `concise`. Reject other tones and issues over 2,000 characters. Demo provider extracts the issue and returns a deterministic safe draft.

```powershell
.\mvnw.cmd -pl labs/07-response-generator -Dtest=ResponseDraftServiceTest test
```

- [ ] **Step 3: Write failing MVC tests**

Test `201 Created` for a valid request and `400` with `VALIDATION_FAILED` for a blank issue or unsupported tone. Use JSON fields `issue` and `tone`; response fields are `content` and `provider`.

- [ ] **Step 4: Implement controller and errors, verify GREEN**

```powershell
.\mvnw.cmd -pl labs/07-response-generator test
```

---

### Task 7: Lab 07 Gemini Adapter and Plan Integration

**Files:**
- Create: `labs/07-response-generator/src/test/java/com/schwartzlizer/ai/response/GeminiSupportResponseAiClientTest.java`
- Create: `labs/07-response-generator/src/main/java/com/schwartzlizer/ai/response/ChatCompletionGateway.java`
- Create: `labs/07-response-generator/src/main/java/com/schwartzlizer/ai/response/GeminiSupportResponseAiClient.java`
- Create: `labs/07-response-generator/src/main/java/com/schwartzlizer/ai/response/AiProviderConfiguration.java`
- Create: `labs/07-response-generator/src/main/resources/application.yml`
- Create: `labs/07-response-generator/README.md`
- Modify: `README.md`
- Modify: `docs/coursera-lab-mapping.md`

**Interfaces:**
- Produces: conditional demo/Gemini provider wiring for Lab 07.
- Consumes: `SupportResponseAiClient` from Task 6.
- Produces: repository mapping for all seven labs.

- [ ] **Step 1: Write failing Gemini adapter test**

Use a capturing fake `ChatCompletionGateway`. Verify the adapter passes the prompt unchanged, returns non-blank provider content, and rejects blank provider content with `IllegalStateException`.

- [ ] **Step 2: Implement adapter and conditional configuration**

Use `application.yml` plus `application-gemini.yml`, the same profile, environment property names, and model as Lab 05, but keep classes in the Lab 07 package. Default context uses demo provider.

- [ ] **Step 3: Verify Lab 07**

```powershell
.\mvnw.cmd -pl labs/07-response-generator verify
```

- [ ] **Step 4: Complete lab mapping and README**

Add Module 2 rows for the chatbot, recommendation system, and response generator. Add `AI_PROVIDER=demo` and optional Gemini run commands without showing or requesting an API key value.

- [ ] **Step 5: Run full reactor verification**

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress verify
git diff --check
```

Expected: all seven labs build; every test reports zero failures and zero errors.

- [ ] **Step 6: Commit**

```powershell
git add labs/07-response-generator README.md docs/coursera-lab-mapping.md
git commit -m "feat: complete Spring AI lab portfolio"
```
