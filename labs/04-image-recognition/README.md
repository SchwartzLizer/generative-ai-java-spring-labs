# Lab 04 — Product Image Recognition

Java ImageIO extracts normalized average RGB/brightness features. A documented heuristic ranks four product labels deterministically; this is a learning exercise, not a trained commercial image recognizer.

## Verify and run

```powershell
..\..\mvnw.cmd -pl labs/04-image-recognition test
..\..\mvnw.cmd -pl labs/04-image-recognition package
java -jar labs/04-image-recognition/target/image-recognition-1.0.0-SNAPSHOT.jar <path-to-png>
```
