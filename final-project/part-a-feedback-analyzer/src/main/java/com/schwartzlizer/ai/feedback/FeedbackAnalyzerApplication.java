package com.schwartzlizer.ai.feedback;

import java.io.PrintStream;
import java.nio.file.Path;

public final class FeedbackAnalyzerApplication {
    private FeedbackAnalyzerApplication() { }
    public static void main(String[] args) {
        int code = run(args, System.out, System.err);
        if (code != 0) System.exit(code);
    }
    static int run(String[] args, PrintStream out, PrintStream err) {
        if (args == null || args.length != 1) {
            err.println("Usage: feedback-analyzer <csv-path>");
            return 2;
        }
        try {
            AnalysisSummary summary = new FeedbackAnalysisService(new LexiconFeedbackSentimentAnalyzer()).analyze(new FeedbackCsvReader().read(Path.of(args[0])));
            new AnalysisReportWriter().write(summary, out);
            return 0;
        } catch (Exception e) {
            err.println(e.getMessage());
            return 1;
        }
    }
}
