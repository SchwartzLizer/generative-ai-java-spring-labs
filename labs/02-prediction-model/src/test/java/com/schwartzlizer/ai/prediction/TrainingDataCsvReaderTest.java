package com.schwartzlizer.ai.prediction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrainingDataCsvReaderTest {
    @TempDir
    Path directory;

    @Test
    void readsIncomeAndPurchaseColumns() throws IOException {
        Path csv = directory.resolve("training.csv");
        Files.writeString(csv, "customer_id,income,purchase_amount\n1,39000,150\n2,58000,225\n");

        assertThat(new TrainingDataCsvReader().read(csv)).containsExactly(
                new TrainingRecord(39_000, 150),
                new TrainingRecord(58_000, 225));
    }

    @Test
    void reportsMalformedNumericData() throws IOException {
        Path csv = directory.resolve("invalid.csv");
        Files.writeString(csv, "income,purchase_amount\nunknown,150\n");

        assertThatThrownBy(() -> new TrainingDataCsvReader().read(csv))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("line 2");
    }
}
