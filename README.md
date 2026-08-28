# Generative AI Java & Spring Portfolio

Original, test-first Java and Spring Boot implementations inspired by the Coursera *Generative AI for Java and Spring Development* labs. The repository is a Maven monorepo: each learning lab is independently runnable, while the flagship app demonstrates how the same ideas become a maintainable Spring Boot service.

## Current modules

| Course area | Repository evidence | Run |
| --- | --- | --- |
| Module 1 — Environment setup | [`labs/01-ai-environment`](labs/01-ai-environment) | `./mvnw -pl labs/01-ai-environment test` |
| Module 1 — Basic prediction | [`labs/02-prediction-model`](labs/02-prediction-model) | `./mvnw -pl labs/02-prediction-model test` |
| Module 1 — Sentiment analysis | [`labs/03-sentiment-analysis`](labs/03-sentiment-analysis) | `./mvnw -pl labs/03-sentiment-analysis test` |
| Module 1 — Product image recognition | [`labs/04-image-recognition`](labs/04-image-recognition) | `./mvnw -pl labs/04-image-recognition test` |

See [`docs/coursera-lab-mapping.md`](docs/coursera-lab-mapping.md) for the evidence map and scope boundaries.

## Requirements

- Java 21+
- Maven Wrapper (no global Maven install required)

Run every current module with:

```powershell
./mvnw.cmd --batch-mode --no-transfer-progress verify
```

The implementations are original portfolio work, not copied course answer files. Course references are acknowledged for learning context; repository tests and source are the evidence for what is implemented here.
