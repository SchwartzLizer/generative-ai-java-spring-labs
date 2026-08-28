# Lab 05 — Spring AI Customer Support Chatbot

Spring Boot REST API with a provider port, deterministic demo provider, validation/error contract, and an optional Spring AI Google GenAI adapter. The default provider is `demo`; tests never call a remote model.

## Verify and run locally

```powershell
..\..\mvnw.cmd -pl labs/05-support-chatbot verify
..\..\mvnw.cmd -pl labs/05-support-chatbot spring-boot:run
curl -X POST http://localhost:8080/api/v1/chat -H "Content-Type: application/json" -d '{"message":"The app crashes"}'
```

Use the live provider only when a key is already configured in your local environment; never commit it:

```powershell
$env:SPRING_PROFILES_ACTIVE = "gemini"
$env:GEMINI_API_KEY = "<local secret>"
..\..\mvnw.cmd -pl labs/05-support-chatbot spring-boot:run
```

The Gemini profile uses `gemini-2.5-flash` and the `spring.ai.google.genai.api-key` property. The application does not log prompts, keys, or provider responses.
