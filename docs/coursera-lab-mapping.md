# Coursera Lab Mapping

This table maps course topics to original repository evidence. A repository row means the implementation exists and is tested here; it does not claim that a Coursera submission was made or graded.

| Course lab | Repository path | Learning objective | Verification | Portfolio enhancement |
| --- | --- | --- | --- | --- |
| Environment Setup | `labs/01-ai-environment` | Verify a Java runtime before running AI examples | `./mvnw.cmd -pl labs/01-ai-environment test` and `package` | Executable runtime health report with a stable CLI contract |
| Basic Prediction | `labs/02-prediction-model` | Train a simple regression model from CSV data | `./mvnw.cmd -pl labs/02-prediction-model test` | Input validation, immutable records, CSV error context, executable shaded JAR |
| Sentiment Analysis | `labs/03-sentiment-analysis` | Classify review text using a transparent baseline | `./mvnw.cmd -pl labs/03-sentiment-analysis test` | Case/punctuation normalization, immediate negation, immutable batch API |
| Product Image Recognition | `labs/04-image-recognition` | Extract image features and rank labels | `./mvnw.cmd -pl labs/04-image-recognition test` | Runtime-generated fixtures, normalized RGB features, documented heuristic limits |

## Scope note

The remaining course areas are implemented as separate Spring AI labs and a final project in later modules. They will be linked here after their focused verification passes.
