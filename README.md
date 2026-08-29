# Generative AI with Java and Spring Boot

This Maven monorepo contains seven Coursera labs, Final Project Part A, and a larger Spring Boot application named `customer-support-platform`.

The main application is a modular monolith for customer support teams. It stores synthetic feedback, analyzes sentiment, category, and urgency, then drafts safe responses for an agent to review. It has a REST API and a server-rendered dashboard. The default `demo` provider uses deterministic rules, so you can run the project without an API key.

[![CI](https://github.com/SchwartzLizer/generative-ai-java-spring-labs/actions/workflows/ci.yml/badge.svg)](https://github.com/SchwartzLizer/generative-ai-java-spring-labs/actions/workflows/ci.yml)

## Run the application

You need Java 21 or newer, Docker Desktop, and the Maven Wrapper included in this repository.

```powershell
copy .env.example .env
docker compose up --build
```

Open `http://localhost:8080/dashboard` and sign in with the credentials from your local `.env` file.

The `demo` provider is active by default. To use Gemini, set `AI_PROVIDER=gemini` and add `GEMINI_API_KEY` to the untracked `.env` file. Keys and prompts are not committed or logged. Provider responses are not logged.

## Review the project in five minutes

1. Read [`docs/architecture.md`](docs/architecture.md) for the request flow and module boundaries.
2. Run all tests with `./mvnw.cmd --batch-mode --no-transfer-progress verify`.
3. Start the application with `copy .env.example .env; docker compose up --build`.
4. Open `http://localhost:8080/dashboard` and use the credentials from `.env`.
5. Run the synthetic API example below, then open `/swagger-ui.html`.

## What the Spring Boot application covers

| Area | Implementation |
| --- | --- |
| Feedback API | Validated and paginated endpoints, UUID resources, and structured errors |
| AI workflow | Auditable sentiment, category, and urgency analysis, plus response drafts that agents can approve or reject |
| Providers | Deterministic `demo` provider for offline use and an optional Spring AI Gemini adapter |
| Persistence | PostgreSQL, Flyway migrations, append-only analysis and draft history, and optimistic locking |
| Security | `AGENT` access for support workflows and `ADMIN` access for operational endpoints |
| Web interface | Thymeleaf dashboard with summary cards, queue filters, analysis history, and draft history |
| Operations | Health probes, OpenAPI documentation, and correlation IDs |

Approve and reject decisions are final. The application keeps previous analyses and response drafts instead of overwriting them.

Dashboard screenshots come from the manual GitHub Actions workflow in `dashboard-screenshot.yml` after a Docker-capable run. The repository does not include unverified mockups.

## Architecture

```mermaid
flowchart LR
  Client[REST client] --> API[Spring MVC API]
  Agent[Support agent] --> UI[Thymeleaf dashboard]
  UI --> API
  API --> Domain[Feedback + analysis + draft services]
  Domain --> DB[(PostgreSQL / Flyway)]
  Domain --> Port[CustomerSupportAiClient]
  Port --> Demo[Demo rules provider]
  Port -. optional .-> Gemini[Spring AI Gemini]
```

See [`docs/architecture.md`](docs/architecture.md) for package ownership, lifecycle transitions, security, and persistence details.

## Try the API

The following PowerShell example creates synthetic feedback, runs an analysis, and generates a response draft.

```powershell
$credential = New-Object PSCredential("agent", (ConvertTo-SecureString "agent-local-password" -AsPlainText -Force))
$created = Invoke-RestMethod -Authentication Basic -Credential $credential -Method Post -Uri http://localhost:8080/api/v1/feedback -ContentType 'application/json' -Body '{"customerReference":"CUST-DEMO","message":"The app crashes during checkout"}'
$id = $created.id
Invoke-RestMethod -Authentication Basic -Credential $credential -Method Post -Uri "http://localhost:8080/api/v1/feedback/$id/analyses"
Invoke-RestMethod -Authentication Basic -Credential $credential -Method Post -Uri "http://localhost:8080/api/v1/feedback/$id/response-drafts"
```

API contract: `POST/GET /api/v1/feedback`, `POST /api/v1/feedback/{id}/analyses`, `POST /api/v1/feedback/{id}/response-drafts`, `PATCH /api/v1/response-drafts/{id}/decision`, `GET /api/v1/dashboard/summary`.

## Repository layout

| Area | Path | How to verify it |
| --- | --- | --- |
| Seven course labs | [`labs/`](labs) | `./mvnw.cmd verify` |
| Final Project Part A | [`final-project/part-a-feedback-analyzer`](final-project/part-a-feedback-analyzer) | `./mvnw.cmd -pl final-project/part-a-feedback-analyzer verify` |
| Spring Boot application | [`customer-support-platform`](customer-support-platform) | `./mvnw.cmd -pl customer-support-platform verify` |
| Architecture notes | [`docs/architecture.md`](docs/architecture.md) | Review the Mermaid diagrams |
| Course evidence map | [`docs/coursera-lab-mapping.md`](docs/coursera-lab-mapping.md) | Review the linked repository paths and tests |

## Testing

```powershell
./mvnw.cmd --batch-mode --no-transfer-progress verify
```

This command runs unit tests, Spring context tests, MVC workflow tests, and a PostgreSQL integration test. Testcontainers starts PostgreSQL when Docker is available. GitHub Actions runs the full verification on a Docker-capable runner and also builds the application image. See `.github/workflows/ci.yml`.

## Course attribution and license

The seven lab folders and Final Project Part A are original implementations of learning objectives from Coursera's *Generative AI for Java and Spring Development*. The mapping document links each objective to repository code and tests. It does not claim Coursera grading or submission status.

Original work is licensed under Apache 2.0. Course and framework names remain trademarks of their respective owners.
