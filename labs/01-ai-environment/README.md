# Lab 01 — AI Environment

Small, deterministic Java CLI that verifies the active runtime before later AI labs run.

## Verify

```powershell
..\..\mvnw.cmd -pl labs/01-ai-environment test
..\..\mvnw.cmd -pl labs/01-ai-environment package
java -jar labs/01-ai-environment/target/ai-environment-1.0.0-SNAPSHOT.jar
```

Expected output shape:

```text
Java AI environment ready: <runtime name> <runtime version>
```
