# Final Project Part A Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a standalone Java feedback analyzer that reads synthetic customer feedback, performs deterministic sentiment analysis, and produces auditable per-record and aggregate results.

**Architecture:** A plain Maven module separates parsing, analysis, and reporting. It implements the final-project learning objective independently from Lab 03 so the repository contains direct evidence for Module 3 Part A.

**Tech Stack:** Java 21, Maven, OpenCSV 5.12.0, JUnit 5, AssertJ.

**Spec:** `docs/superpowers/specs/2026-08-28-generative-ai-java-spring-portfolio-design.md`

## Global Constraints

- Use only synthetic, non-personal feedback data.
- Do not import production classes from Lab 03.
- Parsing and analysis errors name the record or line without echoing full customer feedback.
- Behavioral production code follows red-green-refactor.
- Output ordering follows input ordering for reproducibility.

---

### Task 1: Add Part A Module and Feedback Parser

**Files:**
- Modify: `pom.xml`
- Create: `final-project/part-a-feedback-analyzer/pom.xml`
- Create: `final-project/part-a-feedback-analyzer/src/test/java/com/schwartzlizer/ai/feedback/FeedbackCsvReaderTest.java`
- Create: `final-project/part-a-feedback-analyzer/src/main/java/com/schwartzlizer/ai/feedback/FeedbackRecord.java`
- Create: `final-project/part-a-feedback-analyzer/src/main/java/com/schwartzlizer/ai/feedback/FeedbackCsvReader.java`

**Interfaces:**
- Produces: `record FeedbackRecord(String reference, String message)`.
- Produces: `FeedbackCsvReader#read(Path): List<FeedbackRecord>`.
- Consumes: OpenCSV.

- [ ] **Step 1: Add reactor module and module POM**

Add `<module>final-project/part-a-feedback-analyzer</module>` to the root. The module declares OpenCSV, JUnit Jupiter, and AssertJ and configures an executable JAR with main class `FeedbackAnalyzerApplication`.

- [ ] **Step 2: Write failing parser tests**

```java
class FeedbackCsvReaderTest {
    @TempDir Path directory;

    @Test
    void readsFeedbackInInputOrder() throws IOException {
        Path csv = directory.resolve("feedback.csv");
        Files.writeString(csv,
            "reference,message\nF-001,Delivery was fast\nF-002,The item arrived broken\n");

        assertThat(new FeedbackCsvReader().read(csv)).containsExactly(
            new FeedbackRecord("F-001", "Delivery was fast"),
            new FeedbackRecord("F-002", "The item arrived broken")
        );
    }

    @Test
    void rejectsMissingMessageHeader() throws IOException {
        Path csv = directory.resolve("invalid.csv");
        Files.writeString(csv, "reference,text\nF-001,Missing header\n");

        assertThatThrownBy(() -> new FeedbackCsvReader().read(csv))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CSV requires reference and message headers");
    }

    @Test
    void rejectsBlankMessageWithoutEchoingContent() throws IOException {
        Path csv = directory.resolve("blank.csv");
        Files.writeString(csv, "reference,message\nF-001, \n");

        assertThatThrownBy(() -> new FeedbackCsvReader().read(csv))
            .hasMessage("Invalid feedback at line 2: message is required");
    }
}
```

- [ ] **Step 3: Run RED**

```powershell
.\mvnw.cmd -pl final-project/part-a-feedback-analyzer test
```

- [ ] **Step 4: Implement parser and verify GREEN**

Use `CSVReaderHeaderAware`, require unique non-blank `reference` and non-blank `message`, return an immutable list, and wrap malformed CSV with a line-oriented message.

```powershell
.\mvnw.cmd -pl final-project/part-a-feedback-analyzer -Dtest=FeedbackCsvReaderTest test
```

- [ ] **Step 5: Commit**

```powershell
git add pom.xml final-project/part-a-feedback-analyzer
git commit -m "feat: add final project feedback input parser"
```

---

### Task 2: Sentiment Analysis and Aggregate Summary

**Files:**
- Create: `final-project/part-a-feedback-analyzer/src/test/java/com/schwartzlizer/ai/feedback/FeedbackAnalysisServiceTest.java`
- Create: `final-project/part-a-feedback-analyzer/src/main/java/com/schwartzlizer/ai/feedback/Sentiment.java`
- Create: `final-project/part-a-feedback-analyzer/src/main/java/com/schwartzlizer/ai/feedback/FeedbackAnalysis.java`
- Create: `final-project/part-a-feedback-analyzer/src/main/java/com/schwartzlizer/ai/feedback/AnalysisSummary.java`
- Create: `final-project/part-a-feedback-analyzer/src/main/java/com/schwartzlizer/ai/feedback/FeedbackSentimentAnalyzer.java`
- Create: `final-project/part-a-feedback-analyzer/src/main/java/com/schwartzlizer/ai/feedback/LexiconFeedbackSentimentAnalyzer.java`
- Create: `final-project/part-a-feedback-analyzer/src/main/java/com/schwartzlizer/ai/feedback/FeedbackAnalysisService.java`

**Interfaces:**
- Produces: `FeedbackSentimentAnalyzer#analyze(String): Sentiment`.
- Produces: `record FeedbackAnalysis(String reference, Sentiment sentiment)`.
- Produces: `record AnalysisSummary(List<FeedbackAnalysis> analyses, Map<Sentiment, Long> counts)`.
- Produces: `FeedbackAnalysisService#analyze(List<FeedbackRecord>): AnalysisSummary`.

- [ ] **Step 1: Write failing analysis tests**

```java
class FeedbackAnalysisServiceTest {
    @Test
    void returnsPerRecordAnalysisAndCompleteCounts() {
        FeedbackSentimentAnalyzer analyzer = message -> {
            if (message.contains("fast")) return Sentiment.POSITIVE;
            if (message.contains("broken")) return Sentiment.NEGATIVE;
            return Sentiment.NEUTRAL;
        };
        var service = new FeedbackAnalysisService(analyzer);

        AnalysisSummary result = service.analyze(List.of(
            new FeedbackRecord("F-001", "Delivery was fast"),
            new FeedbackRecord("F-002", "The item arrived broken"),
            new FeedbackRecord("F-003", "Package arrived Tuesday")
        ));

        assertThat(result.analyses()).containsExactly(
            new FeedbackAnalysis("F-001", Sentiment.POSITIVE),
            new FeedbackAnalysis("F-002", Sentiment.NEGATIVE),
            new FeedbackAnalysis("F-003", Sentiment.NEUTRAL)
        );
        assertThat(result.counts()).containsEntry(Sentiment.POSITIVE, 1L)
            .containsEntry(Sentiment.NEGATIVE, 1L)
            .containsEntry(Sentiment.NEUTRAL, 1L);
    }

    @Test
    void includesZeroCountsForEmptyInput() {
        AnalysisSummary result = new FeedbackAnalysisService(message -> Sentiment.NEUTRAL)
            .analyze(List.of());
        assertThat(result.counts()).containsOnly(
            entry(Sentiment.POSITIVE, 0L),
            entry(Sentiment.NEUTRAL, 0L),
            entry(Sentiment.NEGATIVE, 0L));
    }
}
```

- [ ] **Step 2: Run RED**

```powershell
.\mvnw.cmd -pl final-project/part-a-feedback-analyzer -Dtest=FeedbackAnalysisServiceTest test
```

- [ ] **Step 3: Implement service and lexicon analyzer**

The lexicon analyzer uses the same documented scoring rules as the course objective but a fresh implementation. Positive terms: `fast`, `helpful`, `excellent`, `resolved`, `easy`, `satisfied`. Negative terms: `broken`, `late`, `difficult`, `unhelpful`, `error`, `frustrated`. Immediate `not`, `never`, or `no` negates the next scored term. Zero score is neutral.

`FeedbackAnalysisService` calls the analyzer once per record, preserves order, and creates an immutable enum map containing every sentiment value including zero counts.

- [ ] **Step 4: Verify GREEN**

```powershell
.\mvnw.cmd -pl final-project/part-a-feedback-analyzer test
```

- [ ] **Step 5: Commit**

```powershell
git add final-project/part-a-feedback-analyzer
git commit -m "feat: analyze final project customer feedback"
```

---

### Task 3: Report Writer and Command-Line Application

**Files:**
- Create: `final-project/part-a-feedback-analyzer/src/test/java/com/schwartzlizer/ai/feedback/AnalysisReportWriterTest.java`
- Create: `final-project/part-a-feedback-analyzer/src/test/java/com/schwartzlizer/ai/feedback/FeedbackAnalyzerApplicationTest.java`
- Create: `final-project/part-a-feedback-analyzer/src/main/java/com/schwartzlizer/ai/feedback/AnalysisReportWriter.java`
- Create: `final-project/part-a-feedback-analyzer/src/main/java/com/schwartzlizer/ai/feedback/FeedbackAnalyzerApplication.java`
- Create: `final-project/part-a-feedback-analyzer/src/main/resources/sample-feedback.csv`
- Create: `final-project/part-a-feedback-analyzer/README.md`
- Modify: `docs/coursera-lab-mapping.md`

**Interfaces:**
- Produces: `AnalysisReportWriter#write(AnalysisSummary, Appendable): void`.
- Produces: CLI `FeedbackAnalyzerApplication <csv-path>`.
- Consumes: parser from Task 1 and analysis service from Task 2.

- [ ] **Step 1: Write failing report test**

```java
class AnalysisReportWriterTest {
    @Test
    void writesStableHumanReadableReport() throws IOException {
        var summary = new AnalysisSummary(
            List.of(
                new FeedbackAnalysis("F-001", Sentiment.POSITIVE),
                new FeedbackAnalysis("F-002", Sentiment.NEGATIVE)),
            Map.of(
                Sentiment.POSITIVE, 1L,
                Sentiment.NEUTRAL, 0L,
                Sentiment.NEGATIVE, 1L));
        var output = new StringBuilder();

        new AnalysisReportWriter().write(summary, output);

        assertThat(output.toString()).isEqualTo("""
            Feedback Analysis
            F-001,POSITIVE
            F-002,NEGATIVE
            Summary: POSITIVE=1 NEUTRAL=0 NEGATIVE=1
            """);
    }
}
```

- [ ] **Step 2: Run RED, implement writer, verify GREEN**

```powershell
.\mvnw.cmd -pl final-project/part-a-feedback-analyzer -Dtest=AnalysisReportWriterTest test
```

- [ ] **Step 3: Write failing CLI argument test**

Extract `run(String[] args, PrintStream out, PrintStream err): int` so tests can verify exit codes. Test no arguments returns 2 and writes `Usage: feedback-analyzer <csv-path>` to stderr.

- [ ] **Step 4: Implement CLI and verify GREEN**

`main` calls `System.exit(run(...))` only for non-zero return. Successful execution returns 0. Invalid input returns 1 with a concise error that does not print a stack trace.

```powershell
.\mvnw.cmd -pl final-project/part-a-feedback-analyzer verify
```

- [ ] **Step 5: Add synthetic sample and documentation**

Add six records spanning all sentiments. README documents test, package, and run commands and expected summary shape. Add Module 3 Part A to the Coursera mapping.

- [ ] **Step 6: Run packaged application**

```powershell
.\mvnw.cmd -pl final-project/part-a-feedback-analyzer package
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.1\jbr\bin\java.exe' `
  -jar 'final-project\part-a-feedback-analyzer\target\part-a-feedback-analyzer-1.0.0-SNAPSHOT.jar' `
  'final-project\part-a-feedback-analyzer\src\main\resources\sample-feedback.csv'
```

Expected: six per-record lines and a summary containing positive, neutral, and negative counts.

- [ ] **Step 7: Full verification and commit**

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress verify
git diff --check
git add final-project/part-a-feedback-analyzer docs/coursera-lab-mapping.md
git commit -m "feat: complete final project Java feedback analyzer"
```
