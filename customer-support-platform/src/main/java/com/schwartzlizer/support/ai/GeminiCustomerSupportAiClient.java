package com.schwartzlizer.support.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schwartzlizer.support.analysis.*;
import com.schwartzlizer.support.common.AiProviderException;
import java.util.Objects;

public final class GeminiCustomerSupportAiClient implements CustomerSupportAiClient {
    private final ChatCompletionGateway gateway;
    private final ObjectMapper objectMapper;
    private final AnalysisPromptFactory analysisPrompts;
    private final ResponsePromptFactory responsePrompts;
    public GeminiCustomerSupportAiClient(ChatCompletionGateway gateway, ObjectMapper objectMapper, AnalysisPromptFactory analysisPrompts, ResponsePromptFactory responsePrompts) {
        this.gateway=Objects.requireNonNull(gateway);
        this.objectMapper=Objects.requireNonNull(objectMapper);
        this.analysisPrompts=Objects.requireNonNull(analysisPrompts);
        this.responsePrompts=Objects.requireNonNull(responsePrompts);
    }
    @Override
    public FeedbackAnalysisResult analyze(String message) {
        try {
            String raw=gateway.complete(analysisPrompts.create(message));
            if (raw == null || raw.isBlank()) throw new AiProviderException("AI provider returned an empty response");
            JsonNode json=objectMapper.readTree(stripFences(raw));
            return new FeedbackAnalysisResult(Sentiment.valueOf(json.required("sentiment").asText()), SupportCategory.valueOf(json.required("category").asText()), Urgency.valueOf(json.required("urgency").asText()), json.required("recommendedAction").asText());
        } catch (AiProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new AiProviderException("AI provider returned invalid analysis", e);
        }
    }
    @Override
    public ResponseDraftResult draftResponse(String message, FeedbackAnalysisResult analysis) {
        try {
            String raw=gateway.complete(responsePrompts.create(message, analysis));
            if (raw == null || raw.isBlank()) throw new AiProviderException("AI provider returned an empty response");
            return new ResponseDraftResult(raw);
        } catch (AiProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new AiProviderException("AI provider request failed", e);
        }
    }
    private String stripFences(String raw) {
        String value=raw.trim();
        if(value.startsWith("```")){ value=value.replaceFirst("^```(?:json)?\\s*","").replaceFirst("\\s*```$",""); }
        return value;
    }
}
