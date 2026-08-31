# Coursera Lab Mapping

This table maps course topics to original repository evidence. A repository row means the implementation exists and is tested here — it is original work, not the lab artifact submitted to Coursera. Course-level completion and the Final Exam grade are verified and documented in the [README](../README.md#course-attribution-and-license).

| Course lab | Repository path | Learning objective | Verification | Portfolio enhancement |
| --- | --- | --- | --- | --- |
| Environment Setup | `labs/01-ai-environment` | Verify a Java runtime before running AI examples | `./mvnw.cmd -pl labs/01-ai-environment test` and `package` | Executable runtime health report with a stable CLI contract |
| Basic Prediction | `labs/02-prediction-model` | Train a simple regression model from CSV data | `./mvnw.cmd -pl labs/02-prediction-model test` | Input validation, immutable records, CSV error context, executable shaded JAR |
| Sentiment Analysis | `labs/03-sentiment-analysis` | Classify review text using a transparent baseline | `./mvnw.cmd -pl labs/03-sentiment-analysis test` | Case/punctuation normalization, immediate negation, immutable batch API |
| Product Image Recognition | `labs/04-image-recognition` | Extract image features and rank labels | `./mvnw.cmd -pl labs/04-image-recognition test` | Runtime-generated fixtures, normalized RGB features, documented heuristic limits |
| Support Chatbot | `labs/05-support-chatbot` | Expose a support assistant through Spring MVC | `./mvnw.cmd -pl labs/05-support-chatbot verify` | Provider port, demo/Gemini profile switch, validation error contract, MVC tests |
| Recommendation System | `labs/06-recommendation-system` | Rank catalog items from preferences | `./mvnw.cmd -pl labs/06-recommendation-system test` | Immutable domain records, normalized genres, deterministic tie-breaking |
| Response Template Generator | `labs/07-response-generator` | Generate safe, tone-aware support drafts | `./mvnw.cmd -pl labs/07-response-generator verify` | Bounded prompt factory, tone validation, `201 Created` REST contract, provider adapter |
| Final Project Part A — Feedback Analyzer | `final-project/part-a-feedback-analyzer` | Parse synthetic feedback and produce per-record plus aggregate sentiment results | `./mvnw.cmd -pl final-project/part-a-feedback-analyzer verify` and packaged CLI | Standalone parser, immutable summary, deterministic negation-aware lexicon, stable report writer |
| Final Project Part B — Customer Support Platform | `customer-support-platform` | Persist feedback, analyze it, generate a response draft, and review it in a Spring Boot app | `./mvnw.cmd -pl customer-support-platform verify`; `docker compose up --build` for PostgreSQL runtime | Modular monolith with documented bounded contexts, Flyway/PostgreSQL, role security, append-only audit history, demo/Gemini provider boundary, OpenAPI and dashboard |

## Scope note

The rows above document repository evidence only — original code and passing tests, not a per-lab Coursera submission or grade. Course-level completion and the Final Exam grade are documented in the README, not claimed here per lab. PostgreSQL Testcontainers runs automatically in Docker-capable CI; the local workstation verification records that container test as skipped when Docker is unavailable.
