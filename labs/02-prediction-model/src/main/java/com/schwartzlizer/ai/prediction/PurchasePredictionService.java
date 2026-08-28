package com.schwartzlizer.ai.prediction;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.List;

public final class PurchasePredictionService {
    private SimpleRegression regression;

    public PurchasePredictionService train(List<TrainingRecord> records) {
        if (records == null || records.size() < 2) {
            throw new IllegalArgumentException("Training data requires at least two records");
        }
        var distinctIncomes = records.stream()
                .mapToDouble(TrainingRecord::income)
                .peek(value -> requireFinite(value, "Income"))
                .distinct()
                .count();
        records.forEach(record -> requireFinite(record.purchaseAmount(), "Purchase amount"));
        if (distinctIncomes < 2) {
            throw new IllegalArgumentException("Training data requires two distinct income values");
        }

        var candidate = new SimpleRegression(true);
        records.forEach(record -> candidate.addData(record.income(), record.purchaseAmount()));
        regression = candidate;
        return this;
    }

    public double predict(double income) {
        if (regression == null) {
            throw new IllegalStateException("Model has not been trained");
        }
        requireFinite(income, "Income");
        return regression.predict(income);
    }

    private static void requireFinite(double value, String field) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(field + " must be finite");
        }
    }
}
