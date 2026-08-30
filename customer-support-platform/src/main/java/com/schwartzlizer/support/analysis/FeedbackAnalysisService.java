package com.schwartzlizer.support.analysis;

import com.schwartzlizer.support.ai.AiProviderProperties;
import com.schwartzlizer.support.ai.CustomerSupportAiClient;
import com.schwartzlizer.support.ai.FeedbackAnalysisResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class FeedbackAnalysisService {
    private final FeedbackAnalysisTxOperations txOperations;
    private final CustomerSupportAiClient aiClient;
    private final Clock clock;
    private final Timer aiProviderCallDuration;

    public FeedbackAnalysisService(
        FeedbackAnalysisTxOperations txOperations,
        CustomerSupportAiClient aiClient,
        Clock clock,
        AiProviderProperties provider,
        MeterRegistry meterRegistry
    ) {
        this.txOperations = txOperations;
        this.aiClient = aiClient;
        this.clock = clock;
        this.aiProviderCallDuration = Timer.builder("ai.provider.call.duration")
            .description("AI provider call duration")
            .tag("provider", provider.provider())
            .register(meterRegistry);
    }

    public FeedbackAnalysisResponse analyze(UUID feedbackId) {
        AnalysisInput input = txOperations.loadInput(feedbackId);
        FeedbackAnalysisResult result = aiProviderCallDuration.record(() -> aiClient.analyze(input.message()));
        Instant now = Instant.now(clock);
        return txOperations.persist(feedbackId, result, now);
    }
}
