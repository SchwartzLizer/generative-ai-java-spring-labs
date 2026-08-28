# Final Project Part A — Feedback Analyzer

Standalone Java 21 command-line implementation for synthetic customer-feedback sentiment analysis. It parses UTF-8 CSV input, preserves input order, produces auditable per-record classifications, and prints complete aggregate counts.

```powershell
..\..\mvnw.cmd -pl final-project/part-a-feedback-analyzer verify
..\..\mvnw.cmd -pl final-project/part-a-feedback-analyzer package
java -jar target/part-a-feedback-analyzer-1.0.0-SNAPSHOT.jar src/main/resources/sample-feedback.csv
```

The analyzer is deterministic and offline. It uses a small documented lexicon with immediate `not`, `never`, and `no` negation.
