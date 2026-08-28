package com.schwartzlizer.ai.feedback;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class FeedbackCsvReader {
    public List<FeedbackRecord> read(Path path) {
        if (path == null) throw new IllegalArgumentException("CSV path is required");
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
             CSVReaderHeaderAware csv = new CSVReaderHeaderAware(reader)) {
            Map<String, String> row;
            List<FeedbackRecord> records = new ArrayList<>();
            int line = 1;
            Map<String, String> headers = csv.readMap();
            if (headers == null || !headers.containsKey("reference") || !headers.containsKey("message")) {
                throw new IllegalArgumentException("CSV requires reference and message headers");
            }
            do {
                line++;
                row = headers;
                if (row == null) break;
                String reference = row.get("reference");
                String message = row.get("message");
                if (reference == null || reference.isBlank()) throw invalid(line, "reference is required");
                if (message == null || message.isBlank()) throw invalid(line, "message is required");
                records.add(new FeedbackRecord(reference, message));
                headers = csv.readMap();
            } while (headers != null);
            return List.copyOf(records);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException | CsvValidationException e) {
            throw new IllegalArgumentException("Unable to read feedback CSV: " + path.getFileName(), e);
        }
    }

    private IllegalArgumentException invalid(int line, String reason) {
        return new IllegalArgumentException("Invalid feedback at line " + line + ": " + reason);
    }
}
