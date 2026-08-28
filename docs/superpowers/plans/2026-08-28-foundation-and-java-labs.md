# Repository Foundation and Java Labs Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create the Maven monorepo foundation and original, tested implementations for Coursera labs 01 through 04.

**Architecture:** A root Maven aggregator centralizes Java and dependency versions while each lab remains an independent application. Lab code does not share production classes; each module demonstrates one learning objective with deterministic tests.

**Tech Stack:** Java 21, Maven 3.9.16 Wrapper, JUnit 5, AssertJ, Apache Commons Math 3.6.1, OpenCSV 5.12.0, Java AWT/ImageIO.

**Spec:** `docs/superpowers/specs/2026-08-28-generative-ai-java-spring-portfolio-design.md`

## Global Constraints

- Use Java release 21.
- Group ID is `com.schwartzlizer.ai` and repository version is `1.0.0-SNAPSHOT`.
- Behavioral production code follows red-green-refactor; configuration and documentation use command-based verification.
- No copied upstream source files, course answer text, secrets, binary models, build output, or IDE files.
- Tests use deterministic local data and make no network calls.
- Every task ends with a focused commit after fresh verification.

---

### Task 1: Root Maven Build and Repository Hygiene

**Files:**
- Create: `pom.xml`
- Create: `.gitignore`
- Create: `LICENSE`
- Create: `README.md`
- Create: `.mvn/wrapper/maven-wrapper.properties`
- Create: `mvnw`
- Create: `mvnw.cmd`

**Interfaces:**
- Produces: Maven parent `com.schwartzlizer.ai:generative-ai-java-spring-labs:1.0.0-SNAPSHOT`.
- Produces: properties `java.version=21`, `junit.version=5.14.3`, `assertj.version=3.27.6`, `commons-math3.version=3.6.1`, and `opencsv.version=5.12.0`.
- Consumes: no earlier task.

- [ ] **Step 1: Create the root aggregator POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.schwartzlizer.ai</groupId>
    <artifactId>generative-ai-java-spring-labs</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>labs/01-ai-environment</module>
        <module>labs/02-prediction-model</module>
        <module>labs/03-sentiment-analysis</module>
        <module>labs/04-image-recognition</module>
    </modules>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.release>21</maven.compiler.release>
        <junit.version>5.14.3</junit.version>
        <assertj.version>3.27.6</assertj.version>
        <commons-math3.version>3.6.1</commons-math3.version>
        <opencsv.version>5.12.0</opencsv.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.junit</groupId>
                <artifactId>junit-bom</artifactId>
                <version>${junit.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.assertj</groupId>
                <artifactId>assertj-core</artifactId>
                <version>${assertj.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.14.1</version>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>3.5.5</version>
                    <configuration>
                        <useModulePath>false</useModulePath>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

- [ ] **Step 2: Add ignore rules and Apache-2.0 license**

`.gitignore` must contain:

```gitignore
.idea/
.vscode/
*.iml
.env
target/
**/target/
*.log
output/
*.class
```

Use the unmodified Apache License 2.0 text in `LICENSE`. Keep `README.md` limited to the repository title, one-sentence goal, and a statement that implementation is in progress.

- [ ] **Step 3: Generate Maven Wrapper 3.9.16**

Run with the discovered IntelliJ Java and Maven paths:

```powershell
$env:JAVA_HOME='C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.1\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd' wrapper:wrapper -Dmaven=3.9.16
```

- [ ] **Step 4: Verify the root configuration**

Run:

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress -N validate
git diff --check
```

Expected: Maven exits 0 and `git diff --check` prints no errors.

- [ ] **Step 5: Commit**

```powershell
git add pom.xml .gitignore LICENSE README.md .mvn mvnw mvnw.cmd
git commit -m "build: initialize Maven portfolio monorepo"
```

---

### Task 2: Lab 01 AI Environment

**Files:**
- Create: `labs/01-ai-environment/pom.xml`
- Create: `labs/01-ai-environment/src/test/java/com/schwartzlizer/ai/environment/EnvironmentReportTest.java`
- Create: `labs/01-ai-environment/src/main/java/com/schwartzlizer/ai/environment/EnvironmentReport.java`
- Create: `labs/01-ai-environment/src/main/java/com/schwartzlizer/ai/environment/EnvironmentApplication.java`
- Create: `labs/01-ai-environment/README.md`

**Interfaces:**
- Produces: `EnvironmentReport#create(String runtimeName, String runtimeVersion): String`.
- Produces: CLI main class `EnvironmentApplication`.
- Consumes: root Maven parent from Task 1.

- [ ] **Step 1: Add the module POM**

The module inherits the root parent and declares `org.junit.jupiter:junit-jupiter` plus `org.assertj:assertj-core` as test dependencies. Configure `maven-jar-plugin` with main class `com.schwartzlizer.ai.environment.EnvironmentApplication`.

- [ ] **Step 2: Write the failing output tests**

```java
package com.schwartzlizer.ai.environment;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnvironmentReportTest {
    private final EnvironmentReport report = new EnvironmentReport();

    @Test
    void formatsVerifiedRuntime() {
        assertThat(report.create("OpenJDK", "21.0.8"))
            .isEqualTo("Java AI environment ready: OpenJDK 21.0.8");
    }

    @Test
    void rejectsBlankRuntimeName() {
        assertThatThrownBy(() -> report.create(" ", "21.0.8"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Runtime name is required");
    }
}
```

- [ ] **Step 3: Run the tests and observe RED**

```powershell
.\mvnw.cmd -pl labs/01-ai-environment test
```

Expected: compilation fails because `EnvironmentReport` does not exist.

- [ ] **Step 4: Add minimal production code**

```java
package com.schwartzlizer.ai.environment;

public final class EnvironmentReport {
    public String create(String runtimeName, String runtimeVersion) {
        if (runtimeName == null || runtimeName.isBlank()) {
            throw new IllegalArgumentException("Runtime name is required");
        }
        if (runtimeVersion == null || runtimeVersion.isBlank()) {
            throw new IllegalArgumentException("Runtime version is required");
        }
        return "Java AI environment ready: " + runtimeName.trim() + " " + runtimeVersion.trim();
    }
}
```

`EnvironmentApplication.main` reads `java.runtime.name` and `java.runtime.version`, calls `EnvironmentReport`, and prints the returned line.

- [ ] **Step 5: Verify GREEN and the packaged CLI**

```powershell
.\mvnw.cmd -pl labs/01-ai-environment package
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.1\jbr\bin\java.exe' -jar 'labs\01-ai-environment\target\ai-environment-1.0.0-SNAPSHOT.jar'
```

Expected: tests pass and output begins `Java AI environment ready:`.

- [ ] **Step 6: Document and commit**

README commands must cover `test`, `package`, and `java -jar` with expected output shape.

```powershell
git add labs/01-ai-environment
git commit -m "feat: add verified Java AI environment lab"
```

---

### Task 3: Lab 02 Prediction Model

**Files:**
- Create: `labs/02-prediction-model/pom.xml`
- Create: `labs/02-prediction-model/src/test/java/com/schwartzlizer/ai/prediction/PurchasePredictionServiceTest.java`
- Create: `labs/02-prediction-model/src/test/java/com/schwartzlizer/ai/prediction/TrainingDataCsvReaderTest.java`
- Create: `labs/02-prediction-model/src/main/java/com/schwartzlizer/ai/prediction/TrainingRecord.java`
- Create: `labs/02-prediction-model/src/main/java/com/schwartzlizer/ai/prediction/PurchasePredictionService.java`
- Create: `labs/02-prediction-model/src/main/java/com/schwartzlizer/ai/prediction/TrainingDataCsvReader.java`
- Create: `labs/02-prediction-model/src/main/java/com/schwartzlizer/ai/prediction/PredictionApplication.java`
- Create: `labs/02-prediction-model/src/test/resources/training-data.csv`
- Create: `labs/02-prediction-model/README.md`

**Interfaces:**
- Produces: `record TrainingRecord(double income, double purchaseAmount)`.
- Produces: `PurchasePredictionService#train(List<TrainingRecord>): PurchasePredictionService`.
- Produces: `PurchasePredictionService#predict(double income): double`.
- Produces: `TrainingDataCsvReader#read(Path): List<TrainingRecord>`.
- Consumes: Apache Commons Math `SimpleRegression` and OpenCSV.

- [ ] **Step 1: Add module dependencies**

Declare `commons-math3`, `opencsv`, JUnit Jupiter, and AssertJ. Set the JAR main class to `PredictionApplication`.

- [ ] **Step 2: Write failing regression tests**

```java
class PurchasePredictionServiceTest {
    @Test
    void predictsFromLinearTrainingData() {
        var service = new PurchasePredictionService().train(List.of(
            new TrainingRecord(1_000, 100),
            new TrainingRecord(2_000, 200),
            new TrainingRecord(3_000, 300)
        ));

        assertThat(service.predict(2_500)).isCloseTo(250, within(0.0001));
    }

    @Test
    void rejectsFewerThanTwoDistinctIncomeValues() {
        assertThatThrownBy(() -> new PurchasePredictionService().train(List.of(
            new TrainingRecord(1_000, 100),
            new TrainingRecord(1_000, 200)
        ))).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Training data requires two distinct income values");
    }
}
```

- [ ] **Step 3: Run RED**

```powershell
.\mvnw.cmd -pl labs/02-prediction-model test
```

Expected: missing `PurchasePredictionService` and `TrainingRecord` compilation failure.

- [ ] **Step 4: Implement the regression service**

Use `SimpleRegression(true)`. Validate at least two records, finite values, and two distinct incomes before adding data. Reject `predict` before training and reject non-finite income. Return `regression.predict(income)`.

- [ ] **Step 5: Verify service GREEN**

```powershell
.\mvnw.cmd -pl labs/02-prediction-model -Dtest=PurchasePredictionServiceTest test
```

- [ ] **Step 6: Write failing CSV reader tests**

```java
class TrainingDataCsvReaderTest {
    @TempDir Path directory;

    @Test
    void readsIncomeAndPurchaseColumns() throws IOException {
        Path csv = directory.resolve("training.csv");
        Files.writeString(csv, "customer_id,income,purchase_amount\n1,39000,150\n2,58000,225\n");

        assertThat(new TrainingDataCsvReader().read(csv)).containsExactly(
            new TrainingRecord(39_000, 150),
            new TrainingRecord(58_000, 225)
        );
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
```

- [ ] **Step 7: Run RED, implement reader, verify GREEN**

Use `CSVReaderHeaderAware` with required headers `income` and `purchase_amount`. Wrap missing files, missing headers, and numeric errors with messages naming the file or line. Run:

```powershell
.\mvnw.cmd -pl labs/02-prediction-model test
```

- [ ] **Step 8: Add CLI, sample data, docs, and commit**

The CLI accepts exactly two arguments: CSV path and income. It prints `Predicted purchase amount: %.2f` and exits non-zero for invalid arguments.

```powershell
git add labs/02-prediction-model
git commit -m "feat: add tested purchase prediction lab"
```

---

### Task 4: Lab 03 Sentiment Analysis

**Files:**
- Create: `labs/03-sentiment-analysis/pom.xml`
- Create: `labs/03-sentiment-analysis/src/test/java/com/schwartzlizer/ai/sentiment/LexiconSentimentAnalyzerTest.java`
- Create: `labs/03-sentiment-analysis/src/test/java/com/schwartzlizer/ai/sentiment/ReviewBatchAnalyzerTest.java`
- Create: `labs/03-sentiment-analysis/src/main/java/com/schwartzlizer/ai/sentiment/Sentiment.java`
- Create: `labs/03-sentiment-analysis/src/main/java/com/schwartzlizer/ai/sentiment/SentimentResult.java`
- Create: `labs/03-sentiment-analysis/src/main/java/com/schwartzlizer/ai/sentiment/SentimentAnalyzer.java`
- Create: `labs/03-sentiment-analysis/src/main/java/com/schwartzlizer/ai/sentiment/LexiconSentimentAnalyzer.java`
- Create: `labs/03-sentiment-analysis/src/main/java/com/schwartzlizer/ai/sentiment/ReviewBatchAnalyzer.java`
- Create: `labs/03-sentiment-analysis/src/main/java/com/schwartzlizer/ai/sentiment/SentimentApplication.java`
- Create: `labs/03-sentiment-analysis/README.md`

**Interfaces:**
- Produces: `SentimentAnalyzer#analyze(String): SentimentResult`.
- Produces: `record SentimentResult(Sentiment sentiment, int score)`.
- Produces: `ReviewBatchAnalyzer#analyze(List<String>): List<SentimentResult>`.
- Consumes: no external NLP runtime.

- [ ] **Step 1: Write failing analyzer tests**

```java
class LexiconSentimentAnalyzerTest {
    private final SentimentAnalyzer analyzer = new LexiconSentimentAnalyzer();

    @Test void classifiesPositiveTextIgnoringCaseAndPunctuation() {
        assertThat(analyzer.analyze("Excellent product, I LOVE it!").sentiment())
            .isEqualTo(Sentiment.POSITIVE);
    }

    @Test void classifiesNegativeText() {
        assertThat(analyzer.analyze("The device is broken and terrible").sentiment())
            .isEqualTo(Sentiment.NEGATIVE);
    }

    @Test void handlesImmediateNegation() {
        assertThat(analyzer.analyze("not good").sentiment())
            .isEqualTo(Sentiment.NEGATIVE);
    }

    @Test void returnsNeutralForNoKnownTerms() {
        assertThat(analyzer.analyze("The parcel arrived Tuesday").sentiment())
            .isEqualTo(Sentiment.NEUTRAL);
    }

    @Test void rejectsBlankReview() {
        assertThatThrownBy(() -> analyzer.analyze(" "))
            .hasMessage("Review text is required");
    }
}
```

- [ ] **Step 2: Run RED**

```powershell
.\mvnw.cmd -pl labs/03-sentiment-analysis test
```

- [ ] **Step 3: Implement minimal analyzer**

Tokenize lower-case text with `[^a-z']+`. Use immutable positive terms `good`, `great`, `excellent`, `love`, `helpful`, `fast` and negative terms `bad`, `terrible`, `broken`, `hate`, `slow`, `poor`. If `not`, `never`, or `no` immediately precedes a scored token, invert that token's score. Positive total yields `POSITIVE`, negative yields `NEGATIVE`, zero yields `NEUTRAL`.

- [ ] **Step 4: Verify analyzer GREEN**

```powershell
.\mvnw.cmd -pl labs/03-sentiment-analysis -Dtest=LexiconSentimentAnalyzerTest test
```

- [ ] **Step 5: Write batch behavior test, implement, and verify**

Test that input order is preserved and an immutable result list is returned. `ReviewBatchAnalyzer` receives `SentimentAnalyzer` through its constructor and maps each review exactly once.

```powershell
.\mvnw.cmd -pl labs/03-sentiment-analysis test
```

- [ ] **Step 6: Add CLI and commit**

The CLI accepts review text arguments, prints one `SENTIMENT score=N` line per review, and documents examples.

```powershell
git add labs/03-sentiment-analysis
git commit -m "feat: add deterministic sentiment analysis lab"
```

---

### Task 5: Lab 04 Image Recognition

**Files:**
- Create: `labs/04-image-recognition/pom.xml`
- Create: `labs/04-image-recognition/src/test/java/com/schwartzlizer/ai/image/ImageFeatureExtractorTest.java`
- Create: `labs/04-image-recognition/src/test/java/com/schwartzlizer/ai/image/ProductImageClassifierTest.java`
- Create: `labs/04-image-recognition/src/main/java/com/schwartzlizer/ai/image/ImageFeatures.java`
- Create: `labs/04-image-recognition/src/main/java/com/schwartzlizer/ai/image/Prediction.java`
- Create: `labs/04-image-recognition/src/main/java/com/schwartzlizer/ai/image/ImageFeatureExtractor.java`
- Create: `labs/04-image-recognition/src/main/java/com/schwartzlizer/ai/image/ProductImageClassifier.java`
- Create: `labs/04-image-recognition/src/main/java/com/schwartzlizer/ai/image/ImageRecognitionApplication.java`
- Create: `labs/04-image-recognition/README.md`

**Interfaces:**
- Produces: `ImageFeatureExtractor#extract(Path): ImageFeatures`.
- Produces: `ProductImageClassifier#classify(ImageFeatures): List<Prediction>`.
- Produces: `record ImageFeatures(int width, int height, double red, double green, double blue, double brightness)`.
- Produces: `record Prediction(String label, double confidence)`.
- Consumes: Java `ImageIO` and `BufferedImage` only.

- [ ] **Step 1: Write failing feature extraction tests**

Create 2x2 red and blue PNG fixtures programmatically inside `@TempDir` so no binary fixtures enter Git.

```java
@Test
void extractsNormalizedAverageColor() throws IOException {
    Path image = writeSolidImage(directory.resolve("red.png"), Color.RED);
    ImageFeatures features = new ImageFeatureExtractor().extract(image);

    assertThat(features.width()).isEqualTo(2);
    assertThat(features.red()).isCloseTo(1.0, within(0.001));
    assertThat(features.green()).isZero();
    assertThat(features.blue()).isZero();
}

@Test
void rejectsCorruptImage() throws IOException {
    Path image = directory.resolve("broken.png");
    Files.writeString(image, "not an image");
    assertThatThrownBy(() -> new ImageFeatureExtractor().extract(image))
        .hasMessage("Unsupported or corrupt image: broken.png");
}
```

- [ ] **Step 2: Run RED, implement extractor, verify GREEN**

Iterate every pixel, ignore alpha, normalize RGB and brightness to `0.0..1.0`, and reject missing, unreadable, or unsupported input.

```powershell
.\mvnw.cmd -pl labs/04-image-recognition -Dtest=ImageFeatureExtractorTest test
```

- [ ] **Step 3: Write failing deterministic classification tests**

```java
@Test
void ranksWarmProductForRedDominantImage() {
    var predictions = new ProductImageClassifier().classify(
        new ImageFeatures(100, 100, 0.9, 0.2, 0.1, 0.4));

    assertThat(predictions).first()
        .extracting(Prediction::label)
        .isEqualTo("warm-colored product");
    assertThat(predictions).isSortedAccordingTo(
        comparingDouble(Prediction::confidence).reversed());
}
```

- [ ] **Step 4: Run RED, implement classifier, verify GREEN**

Create four documented heuristic scores: `warm-colored product`, `cool-colored product`, `bright product`, and `dark product`. Clamp scores to `0.0..1.0`, return all four sorted by confidence descending, and break ties alphabetically.

```powershell
.\mvnw.cmd -pl labs/04-image-recognition test
```

- [ ] **Step 5: Add CLI, explain limitations, and commit**

The CLI accepts one image path and prints ordered labels and confidences. README must explicitly state this is deterministic feature-based classification for learning, not a trained commercial recognizer.

```powershell
git add labs/04-image-recognition
git commit -m "feat: add image feature recognition lab"
```

---

### Task 6: Plan 1 Integration Verification and Mapping

**Files:**
- Modify: `README.md`
- Create: `docs/coursera-lab-mapping.md`

**Interfaces:**
- Consumes: runnable modules from Tasks 2 through 5.
- Produces: repository-level navigation and verified commands for Modules 1 labs.

- [ ] **Step 1: Add Module 1 mapping rows**

For each lab, document course title, repository path, learning objective, `mvnw` test command, runnable entrypoint, and portfolio enhancement. Use neutral wording: repository evidence, not Coursera completion status.

- [ ] **Step 2: Expand root README**

Add a table for the four modules, Java 21 prerequisite, root verification command, and individual run commands. Do not add screenshots before a verified runtime exists.

- [ ] **Step 3: Run full verification**

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress verify
git diff --check
git status --short
```

Expected: four modules succeed with zero test failures; only intended documentation changes remain.

- [ ] **Step 4: Commit**

```powershell
git add README.md docs/coursera-lab-mapping.md
git commit -m "docs: map Java AI labs to repository evidence"
```
