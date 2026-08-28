# Lab 07 — Spring AI Response Template Generator

Spring Boot REST API that builds a bounded support-response prompt, validates tone, and returns a deterministic demo draft by default. An optional Spring AI Google GenAI adapter is profile-gated; tests never call a remote model.

## Verify and run locally

```powershell
..\..\mvnw.cmd -pl labs/07-response-generator verify
..\..\mvnw.cmd -pl labs/07-response-generator spring-boot:run
curl -X POST http://localhost:8080/api/v1/response-drafts -H "Content-Type: application/json" -d '{"issue":"Shipment is late","tone":"empathetic"}'
```

For an already-configured local Gemini environment, set `SPRING_PROFILES_ACTIVE=gemini` and `GEMINI_API_KEY` without committing the secret. The profile uses `gemini-2.5-flash` and never logs prompts or provider responses.
