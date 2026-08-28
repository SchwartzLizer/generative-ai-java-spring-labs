# Lab 02 — Purchase Prediction

An original linear-regression example using Apache Commons Math and a small OpenCSV reader. The sample data is local and deterministic.

## Verify and run

```powershell
..\..\mvnw.cmd -pl labs/02-prediction-model test
..\..\mvnw.cmd -pl labs/02-prediction-model package
java -jar labs/02-prediction-model/target/prediction-model-1.0.0-SNAPSHOT.jar labs/02-prediction-model/src/test/resources/training-data.csv 60000
```

Expected output shape: `Predicted purchase amount:  <amount>`.
