package com.schwartzlizer.ai.prediction;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

class PurchasePredictionServiceTest {
    @Test
    void predictsFromLinearTrainingData() {
        var service = new PurchasePredictionService().train(List.of(
                new TrainingRecord(1_000, 100),
                new TrainingRecord(2_000, 200),
                new TrainingRecord(3_000, 300)));

        assertThat(service.predict(2_500)).isCloseTo(250, offset(0.0001));
    }

    @Test
    void rejectsFewerThanTwoDistinctIncomeValues() {
        assertThatThrownBy(() -> new PurchasePredictionService().train(List.of(
                new TrainingRecord(1_000, 100),
                new TrainingRecord(1_000, 200))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training data requires two distinct income values");
    }
}
