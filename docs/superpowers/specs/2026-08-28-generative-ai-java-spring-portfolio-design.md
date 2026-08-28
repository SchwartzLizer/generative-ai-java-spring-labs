# Generative AI for Java and Spring Portfolio Design

## Purpose

Build one public GitHub monorepo that proves two things:

1. Every hands-on learning objective from Coursera's **Generative AI for Java and Spring Development** course has a runnable, original implementation.
2. The final project is a portfolio-quality Spring Boot application rather than a collection of tutorial snippets.

The repository will be named `generative-ai-java-spring-labs` and owned by `SchwartzLizer`.

## Success Criteria

- One root Maven command builds and tests every Java module.
- Seven lab modules map directly to the seven Coursera labs.
- Final Project Part A has a dedicated runnable Java module.
- Final Project Part B becomes the flagship Spring Boot application.
- All code is original implementation based on learning objectives; course starter code is not copied wholesale.
- Course and starter-repository sources are acknowledged without presenting upstream code as original work.
- No API keys, credentials, generated secrets, database data, build output, or IDE state are committed.
- The flagship application can run locally without a live AI key by using a deterministic demo provider.
- Live Gemini integration is enabled only through environment configuration.
- CI builds and tests the entire repository on every push and pull request.
- Root documentation lets a reviewer understand, run, and evaluate the project within five minutes.

## Non-Goals

- Microservices, service discovery, Kafka, Kubernetes, and distributed tracing are excluded.
- The repository will not include copied Coursera answers, graded assessment content, or screenshots containing answer keys.
- The project will not implement an OAuth authorization server.
- Cloud deployment is excluded from the initial release. The repository will be deployment-ready through containers, but local reproducibility and automated verification come first.
- The image-recognition lab will demonstrate inference with a documented small model or deterministic classifier; it will not train a large model.

## Repository Structure

```text
generative-ai-java-spring-labs/
├── pom.xml
├── .github/
│   └── workflows/
│       └── ci.yml
├── labs/
│   ├── 01-ai-environment/
│   ├── 02-prediction-model/
│   ├── 03-sentiment-analysis/
│   ├── 04-image-recognition/
│   ├── 05-support-chatbot/
│   ├── 06-recommendation-system/
│   └── 07-response-generator/
├── final-project/
│   └── part-a-feedback-analyzer/
├── customer-support-platform/
├── docs/
│   ├── architecture.md
│   ├── coursera-lab-mapping.md
│   └── superpowers/
│       ├── specs/
│       └── plans/
├── compose.yaml
├── .env.example
├── .gitignore
├── LICENSE
└── README.md
```

The root `pom.xml` is an aggregator and dependency-management parent. Each lab remains independently understandable and runnable. The root build provides one verification entrypoint without forcing unrelated lab code into a shared runtime.

## Technology Baseline

- Java 21
- Maven Wrapper committed at repository root
- JUnit 5 and AssertJ for tests
- Spring Boot and Spring AI stable releases that are officially compatible on the implementation date
- No milestone, snapshot, or release-candidate dependencies
- PostgreSQL for the flagship application's persistent runtime
- H2 only for narrow MVC tests where persistence behavior is outside the test scope
- Testcontainers PostgreSQL for repository and full integration tests
- Docker Compose for local application and database startup

Exact dependency versions will be centralized in the root Maven configuration and recorded in the root README. Course-required libraries will be added only to modules that use them.

## Lab Modules

### 01: AI Environment

Purpose: prove Java 21 and Maven setup with a minimal command-line AI-themed application.

Deliverables:

- A Maven application with a deterministic greeting and environment report.
- Tests for the formatted output and unsupported runtime input.
- README with build, test, and run commands.

### 02: Basic Prediction Model

Purpose: predict purchase amount from customer income using simple linear regression.

Deliverables:

- CSV parsing into immutable training records.
- A regression service that trains and predicts without static mutable state.
- Input validation for missing, malformed, and insufficient training data.
- A command-line entrypoint accepting a CSV path and income value.
- Unit tests covering training, prediction, and invalid datasets.

### 03: Sentiment Analysis

Purpose: classify product reviews as positive, neutral, or negative.

Deliverables:

- A sentiment analyzer interface.
- A deterministic lexicon implementation suitable for offline tests.
- A batch review analyzer that reads text input and returns counts and per-review results.
- Tests covering mixed case, punctuation, negation boundaries, neutral text, and empty input.

The flagship application will use the same interface shape but a separate package-local implementation. Lab modules do not become shared production libraries.

### 04: Image Recognition

Purpose: demonstrate Java image preprocessing and classification flow.

Deliverables:

- Image validation and preprocessing for supported PNG and JPEG inputs.
- A classifier interface and deterministic test classifier.
- A runnable classifier implementation based on a small documented model or course-compatible library.
- Tests for dimensions, unsupported formats, corrupt input, and deterministic predictions.

Large binary models will not be committed. A download instruction with checksum will be documented if a model is required.

### 05: Support Chatbot

Purpose: expose a Spring Boot customer-support chat endpoint backed by an interchangeable AI provider.

Deliverables:

- Request and response DTOs with Jakarta Validation.
- Service interface separating prompt construction from provider transport.
- Demo provider for local and CI use.
- Gemini provider enabled through configuration.
- REST controller and structured error responses.
- Unit and MVC tests without live network calls.

### 06: Recommendation System

Purpose: recommend movies from explicit user preferences and catalog metadata.

Deliverables:

- Immutable movie and preference models.
- Deterministic scoring service with documented tie-breaking.
- REST or command-line adapter matching the lab's learning objective.
- Tests for ranking, ties, missing preferences, and empty catalogs.

### 07: Response Generator

Purpose: generate a customer-support response draft from a reusable template and AI provider.

Deliverables:

- Template request validation.
- Prompt builder with tone and issue constraints.
- Demo and Gemini provider implementations.
- REST endpoint with unit and MVC tests.
- No secrets or live provider calls in tests.

## Final Project Part A

`final-project/part-a-feedback-analyzer` is a plain Java command-line application matching the Java AI implementation objective.

It will:

- Read feedback records from a UTF-8 text or CSV file.
- Analyze sentiment through a dedicated interface.
- Produce per-record results and an aggregate summary.
- Reject unreadable or malformed input with actionable messages.
- Include unit tests and a sample data file that contains synthetic, non-personal data.

This module provides direct evidence for Part A instead of relying only on the earlier sentiment lab.

## Flagship Application

### Product Story

`customer-support-platform` helps support agents process customer feedback. It stores incoming feedback, analyzes sentiment and urgency, suggests a support category and action, generates a response draft, and lets an agent review the result from a server-rendered dashboard.

### Core User Flow

1. A client submits feedback through the REST API.
2. The application validates and stores the feedback with status `NEW`.
3. An agent requests AI analysis.
4. The application stores sentiment, category, urgency, provider, model, and analysis timestamp.
5. An agent requests a response draft.
6. The application stores the draft separately from the original feedback.
7. The agent approves, rejects, or regenerates the draft.
8. Dashboard summaries reflect the persisted state.

AI failures never delete or overwrite the original feedback.

### Functional Scope

- Submit and list feedback.
- View one feedback record and its analysis history.
- Analyze sentiment, category, and urgency.
- Generate and retain response drafts.
- Approve or reject a response draft.
- Update feedback workflow status.
- View dashboard counts by sentiment, urgency, category, and status.
- Run with either `demo` or `gemini` AI provider configuration.

### API Contract

```text
POST  /api/v1/feedback
GET   /api/v1/feedback?page={page}&size={size}
GET   /api/v1/feedback/{feedbackId}
POST  /api/v1/feedback/{feedbackId}/analyses
POST  /api/v1/feedback/{feedbackId}/response-drafts
PATCH /api/v1/feedback/{feedbackId}/status
PATCH /api/v1/response-drafts/{draftId}/decision
GET   /api/v1/dashboard/summary
```

REST responses use DTOs rather than exposing persistence entities. Collection endpoints are paginated. Resource identifiers are UUIDs.

### Domain Model

#### Feedback

- `id: UUID`
- `customerReference: String`
- `message: String`
- `status: FeedbackStatus`
- `createdAt: Instant`
- `updatedAt: Instant`
- optimistic-lock version

`customerReference` is synthetic or caller-provided business data; the sample dataset will not contain real customer information.

#### FeedbackAnalysis

- `id: UUID`
- `feedbackId: UUID`
- `sentiment: Sentiment`
- `category: SupportCategory`
- `urgency: Urgency`
- `recommendedAction: String`
- `provider: String`
- `model: String`
- `createdAt: Instant`

Analyses are append-only so regeneration remains auditable.

#### ResponseDraft

- `id: UUID`
- `feedbackId: UUID`
- `content: String`
- `decision: DraftDecision`
- `provider: String`
- `model: String`
- `createdAt: Instant`
- `decidedAt: Instant?`

Draft content can be approved or rejected but not edited in the initial release. A regenerated response creates a new draft.

### Package Structure

The flagship application uses package-by-feature:

```text
com.schwartzlizer.support
├── feedback
├── analysis
├── response
├── dashboard
├── ai
├── security
└── common
```

Each feature owns its controller, DTOs, application service, persistence mapping, and tests. `ai` owns the provider interface and provider implementations. `common` is limited to cross-cutting API errors, time configuration, and identifiers.

No generic base controller, repository wrapper, or speculative shared framework will be introduced.

## AI Integration

The application defines a narrow provider port:

```java
public interface CustomerSupportAiClient {
    FeedbackAnalysisResult analyze(String feedbackMessage);
    ResponseDraftResult draftResponse(
        String feedbackMessage,
        FeedbackAnalysisResult analysis
    );
}
```

Implementations:

- `DemoCustomerSupportAiClient`: deterministic, offline, active by default for local evaluation and CI.
- `GeminiCustomerSupportAiClient`: uses Spring AI and is active only when the configured provider is `gemini`.

`GEMINI_API_KEY` is read from the environment. Startup fails with a clear configuration error when `gemini` is selected without a key. Logs never include prompts containing customer feedback, API keys, or raw provider responses.

Provider calls have explicit timeouts. Provider failure returns a structured `503 Service Unavailable` response while keeping persisted input unchanged.

## Persistence

- Spring Data JPA repositories persist flagship data.
- PostgreSQL is the only supported production-like database.
- Flyway owns schema creation and changes.
- Hibernate schema generation is disabled outside tests.
- Foreign keys protect analysis and draft ownership.
- Indexes support feedback status, creation time, sentiment, category, and urgency queries.
- Testcontainers verifies migrations and repository behavior against PostgreSQL.

## Security

Spring Security protects all dashboard and mutation routes.

- Dashboard authentication uses form login.
- `/api/**` supports HTTP Basic for local portfolio evaluation.
- Roles are `AGENT` and `ADMIN`.
- Read and workflow operations require `AGENT` or `ADMIN`.
- Operational administration requires `ADMIN`.
- CSRF protection remains enabled for browser forms.
- Health liveness and readiness endpoints are public; other Actuator endpoints are not exposed publicly.
- Demo credentials come from environment configuration and are documented in `.env.example`, never hard-coded as production defaults.

JWT, OAuth login, user registration, password reset, and persistent user management are excluded from the initial release.

## Error Handling

All REST errors use one structure:

```json
{
  "code": "FEEDBACK_NOT_FOUND",
  "message": "Feedback was not found",
  "timestamp": "2026-08-28T12:00:00Z",
  "path": "/api/v1/feedback/00000000-0000-0000-0000-000000000000",
  "fieldErrors": []
}
```

The application distinguishes validation errors, missing resources, invalid state transitions, AI-provider unavailability, and unexpected server errors. Internal exception details and secrets are not returned to clients.

## Dashboard

Thymeleaf provides a small server-rendered dashboard. It contains:

- Summary cards for total feedback, open work, urgent feedback, and pending drafts.
- Filterable feedback table.
- Feedback detail page with analysis and draft history.
- Actions to analyze, create a draft, approve, reject, and change status.

The dashboard is intentionally small. It demonstrates Spring MVC and application usability without adding a separate JavaScript frontend.

## API Documentation and Operations

- OpenAPI documents every public REST endpoint, request, response, validation rule, and security scheme.
- Swagger UI is available in the local profile.
- Spring Boot Actuator exposes health, liveness, readiness, and application info.
- Application logs are structured and include a correlation identifier.
- Logs exclude secrets, authentication headers, and raw customer feedback.

## Testing Strategy

### Lab Modules

- Unit tests cover every non-trivial behavior.
- Tests use deterministic data and no live network calls.
- Each module has its own Maven test command.

### Flagship Application

- Plain unit tests cover domain rules and state transitions.
- MVC slice tests cover validation, authorization, response shapes, and error mapping.
- Repository tests use PostgreSQL Testcontainers and execute Flyway migrations.
- Full application tests use the demo AI provider.
- Gemini integration tests verify request mapping against a local stub server, never the live provider.
- Security tests verify anonymous, `AGENT`, and `ADMIN` access.
- One container smoke test verifies application startup with PostgreSQL.

Every new behavior follows red-green-refactor: a failing test is observed before the implementation is added.

## Continuous Integration

GitHub Actions will:

1. Check out the repository.
2. Set up the required Java distribution and Maven cache.
3. Run `./mvnw --batch-mode --no-transfer-progress verify`.
4. Build the flagship container image without publishing it.
5. Upload test reports only when a job fails.

The workflow will not require Gemini credentials.

## Local Runtime

Default evaluation path:

```text
copy .env.example to .env
docker compose up --build
open http://localhost:8080/dashboard
```

The demo AI provider is the default. Live Gemini use requires setting `AI_PROVIDER=gemini` and `GEMINI_API_KEY` locally.

## Documentation

### Root README

- One-sentence product value.
- Short feature list.
- Architecture diagram using Mermaid.
- Dashboard screenshots produced from verified local runtime.
- Quick-start commands.
- API examples.
- Test and CI commands.
- Technology choices and trade-offs.
- Link to the Coursera mapping document.
- Source acknowledgements.

### Coursera Mapping

`docs/coursera-lab-mapping.md` maps every Module 1 and Module 2 lab plus Module 3 Parts A and B to:

- Repository path.
- Learning objective.
- Runnable command.
- Relevant automated tests.
- Portfolio enhancement beyond the course exercise.

It will not claim that Coursera marked an item complete; it documents repository evidence only.

### Architecture Document

`docs/architecture.md` records system context, application components, request flow, database schema, AI provider boundary, and security boundary.

## Source and Attribution Policy

- Implementations are written fresh from learning objectives and public API documentation.
- Any copied sample data or assets require an explicit compatible license and attribution.
- Upstream course repositories are listed as learning references when used.
- No upstream repository history is imported into this repository.
- The repository license applies only to original work and clearly identified compatible third-party material.

## Implementation Decomposition

The scope will be implemented through four plans so each checkpoint produces a reviewable, working deliverable:

1. Repository foundation and plain Java labs 01 through 04.
2. Spring-based labs 05 through 07.
3. Final Project Part A feedback analyzer.
4. Flagship customer support platform, documentation, containers, and release verification.

Each plan uses the root build established by the first plan. Later plans may consume repository conventions but will not share production code across otherwise independent lab modules.

## Verification and Release

Before the public repository is created or updated:

- Run the root Maven verification command and inspect the full result.
- Run the flagship application with demo AI and PostgreSQL.
- Exercise the health endpoint and core feedback workflow.
- Render and inspect dashboard pages.
- Build the Docker image.
- Scan tracked files and Git history for secrets.
- Confirm every Coursera item has a mapping entry and runnable evidence.
- Confirm GitHub repository ownership, visibility, default branch, and pushed commit.

The first public release is tagged `v1.0.0` only after these checks pass.

## Acceptance Checklist

- [ ] Root build passes with all modules.
- [ ] Seven lab modules are runnable and tested.
- [ ] Final Project Part A is runnable and tested.
- [ ] Flagship Spring Boot application satisfies the documented workflow.
- [ ] Demo AI runtime works without external credentials.
- [ ] Gemini integration is configurable without committed secrets.
- [ ] PostgreSQL migrations and repositories pass Testcontainers tests.
- [ ] Security roles and protected routes are verified.
- [ ] Docker Compose starts the application and database.
- [ ] GitHub Actions verifies the repository without secret configuration.
- [ ] Documentation includes architecture, screenshots, API examples, and course mapping.
- [ ] Public GitHub repository contains the verified main branch and `v1.0.0` tag.
