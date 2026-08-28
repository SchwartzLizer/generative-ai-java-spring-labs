package com.schwartzlizer.ai.prediction;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public final class TrainingDataCsvReader {
    public java.util.List<TrainingRecord> read(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("CSV path is required");
        }
        var records = new ArrayList<TrainingRecord>();
        try (var reader = new CSVReaderHeaderAware(Files.newBufferedReader(path))) {
            Map<String, String> row;
            int line = 1;
            while ((row = reader.readMap()) != null) {
                line++;
                records.add(new TrainingRecord(
                        parse(row, "income", path, line),
                        parse(row, "purchase_amount", path, line)));
            }
        } catch (IOException | CsvValidationException exception) {
            throw new IllegalArgumentException("Unable to read CSV file: " + path, exception);
        } catch (IllegalArgumentException exception) {
            throw exception;
        }
        return java.util.List.copyOf(records);
    }

    private static double parse(Map<String, String> row, String header, Path path, int line) {
        var value = row.get(header);
        if (value == null) {
            throw new IllegalArgumentException("Missing required column '" + header + "' in " + path);
        }
        try {
            var parsed = Double.parseDouble(value.trim());
            if (!Double.isFinite(parsed)) {
                throw new NumberFormatException("non-finite");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    String.format(Locale.ROOT, "Invalid numeric value for %s at line %d in %s", header, line, path),
                    exception);
        }
    }
}
