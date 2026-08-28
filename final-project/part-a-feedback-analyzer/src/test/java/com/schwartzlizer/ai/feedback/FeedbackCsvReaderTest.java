package com.schwartzlizer.ai.feedback;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedbackCsvReaderTest {
    @TempDir Path directory;
    @Test void readsFeedbackInInputOrder() throws IOException {
        Path csv = directory.resolve("feedback.csv"); Files.writeString(csv, "reference,message\nF-001,Delivery was fast\nF-002,The item arrived broken\n");
        assertThat(new FeedbackCsvReader().read(csv)).containsExactly(new FeedbackRecord("F-001", "Delivery was fast"), new FeedbackRecord("F-002", "The item arrived broken"));
    }
    @Test void rejectsMissingMessageHeader() throws IOException { Path csv = directory.resolve("invalid.csv"); Files.writeString(csv, "reference,text\nF-001,Missing header\n"); assertThatThrownBy(() -> new FeedbackCsvReader().read(csv)).isInstanceOf(IllegalArgumentException.class).hasMessage("CSV requires reference and message headers"); }
    @Test void rejectsBlankMessageWithoutEchoingContent() throws IOException { Path csv = directory.resolve("blank.csv"); Files.writeString(csv, "reference,message\nF-001, \n"); assertThatThrownBy(() -> new FeedbackCsvReader().read(csv)).hasMessage("Invalid feedback at line 2: message is required"); }
}
