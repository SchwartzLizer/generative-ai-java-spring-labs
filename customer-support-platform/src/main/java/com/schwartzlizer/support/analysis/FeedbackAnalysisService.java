package com.schwartzlizer.support.analysis;

import com.schwartzlizer.support.ai.AiProviderProperties;
import com.schwartzlizer.support.ai.CustomerSupportAiClient;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class FeedbackAnalysisService {
    private final FeedbackAnalysisTxOperations txOperations;
    private final CustomerSupportAiClient aiClient;
    private final Clock clock;

    public FeedbackAnalysisService(FeedbackAnalysisTxOperations txOperations,
                                   CustomerSupportAiClient aiClient,
                                   AiProviderProperties provider,
                                   java.util.function.Supplier<UUID> uuidSupplier,
                                   Clock clock) {
        this.txOperations = txOperations;
        this.aiClient = aiClient;
        this.clock = clock;
    }

    public FeedbackAnalysisResponse analyze(UUID feedbackId) {
        AnalysisInput input = txOperations.load(feedbackId);
        var result = aiClient.analyze(input.message());
        return txOperations.persist(feedbackId, result, Instant.now(clock));
    }
}
