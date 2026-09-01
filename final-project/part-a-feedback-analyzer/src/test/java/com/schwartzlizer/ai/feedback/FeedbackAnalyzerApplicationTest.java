package com.schwartzlizer.ai.feedback;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.assertj.core.api.Assertions.assertThat;

class FeedbackAnalyzerApplicationTest {
    @Test
    void noArgumentsShowUsage() {
        var err = new ByteArrayOutputStream();
        int code = FeedbackAnalyzerApplication.run(new String[0], System.out, new PrintStream(err));
        assertThat(code).isEqualTo(2);
        assertThat(err.toString()).contains("Usage: feedback-analyzer <csv-path>");
    }
}
