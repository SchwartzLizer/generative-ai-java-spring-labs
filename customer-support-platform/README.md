# Customer Support Platform

Portfolio-grade Spring Boot modular monolith for a synthetic support queue.

## Run

```powershell
copy ..\.env.example ..\.env
docker compose up --build
```

The app listens on `http://localhost:8080`. Default AI is the offline `demo` provider. Configure `AI_PROVIDER=gemini` and a local `GEMINI_API_KEY` only when you want live Spring AI calls.

## API

- `POST /api/v1/feedback` — submit validated feedback.
- `GET /api/v1/feedback?page=0&size=20` — list feedback.
- `GET /api/v1/feedback/{id}` — inspect analysis and draft histories.
- `POST /api/v1/feedback/{id}/analyses` — append an AI analysis.
- `POST /api/v1/feedback/{id}/response-drafts` — append a safe draft after analysis.
- `PATCH /api/v1/response-drafts/{id}/decision` — approve or reject once.
- `GET /api/v1/dashboard/summary` — operational counts.

Use HTTP Basic with the local `AGENT` or `ADMIN` credentials from `.env`. Swagger UI is available at `/swagger-ui.html`; liveness/readiness are under `/actuator/health`.

## Verification

```powershell
..\mvnw.cmd -pl customer-support-platform verify
```

The `SupportRepositoryIT` test runs against PostgreSQL Testcontainers when Docker is available. The deterministic demo path and all MVC/context tests run without a network or AI key.
