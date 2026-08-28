# Lab 03 — Sentiment Analysis

Deterministic lexicon sentiment analysis with immediate negation handling. It is intentionally transparent and local so the behavior can be tested without an external NLP service.

## Verify and run

```powershell
..\..\mvnw.cmd -pl labs/03-sentiment-analysis test
..\..\mvnw.cmd -pl labs/03-sentiment-analysis package
java -jar labs/03-sentiment-analysis/target/sentiment-analysis-1.0.0-SNAPSHOT.jar "Excellent and helpful" "not good"
```

The CLI prints one `SENTIMENT score=N` line per review.
