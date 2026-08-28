# Lab 06 — Movie Recommendation System

Deterministic content-based ranking: matching genres contribute `3.0` each, rating contributes `rating / 10`, and below-threshold ratings receive a `1.0` penalty. Ties are stable by title.

## Verify and run

```powershell
..\..\mvnw.cmd -pl labs/06-recommendation-system test
..\..\mvnw.cmd -pl labs/06-recommendation-system package
java -jar labs/06-recommendation-system/target/recommendation-system-1.0.0-SNAPSHOT.jar
```
