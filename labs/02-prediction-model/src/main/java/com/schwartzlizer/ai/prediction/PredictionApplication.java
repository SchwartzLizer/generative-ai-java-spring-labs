package com.schwartzlizer.ai.prediction;

import java.nio.file.Path;
import java.util.Locale;

public final class PredictionApplication {
    private PredictionApplication() {
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar prediction-model-*.jar <csv-path> <income>");
            System.exit(2);
        }
        try {
            var records = new TrainingDataCsvReader().read(Path.of(args[0]));
            var income = Double.parseDouble(args[1]);
            var prediction = new PurchasePredictionService().train(records).predict(income);
            System.out.printf(Locale.ROOT, "Predicted purchase amount: %.2f%n", prediction);
        } catch (RuntimeException exception) {
            System.err.println("Prediction failed: " + exception.getMessage());
            System.exit(1);
        }
    }
}
