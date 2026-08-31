package com.schwartzlizer.support.ai;

/**
 * Port to the AI provider that acts as an anti-corruption layer between the module and vendor SDKs.
 *
 * <p>Application and domain code depends only on this interface and on the {@code FeedbackAnalysisResult} and
 * {@code ResponseDraftResult} value types, never on vendor SDK types directly.
 *
 * <p>Implemented by {@code DemoCustomerSupportAiClient} (deterministic, rule-based, no network) and
 * {@code GeminiCustomerSupportAiClient} (remote call through {@code ChatCompletionGateway}). Implementations
 * must translate every vendor, transport, or parsing failure into
 * {@link com.schwartzlizer.support.common.AiProviderException} and must never return null. Calls are synchronous
 * and blocking.
 */
public interface CustomerSupportAiClient {
    /**
     * Classifies a feedback message into sentiment, category, urgency and a recommended action.
     *
     * @param feedbackMessage raw customer text to classify; must not be null or blank
     * @return the classification result, never null
     * @throws com.schwartzlizer.support.common.AiProviderException if the provider is unreachable, returns an
     *         empty body, or returns a payload that cannot be mapped to the result type
     */
    FeedbackAnalysisResult analyze(String feedbackMessage);
    /**
     * Generates a suggested agent reply for a message that has already been analysed.
     *
     * @param feedbackMessage raw customer text the reply must answer
     * @param analysis classification used to steer tone and content of the reply
     * @return the generated draft content, never null
     * @throws com.schwartzlizer.support.common.AiProviderException same conditions as {@code analyze}
     */
    ResponseDraftResult draftResponse(String feedbackMessage, FeedbackAnalysisResult analysis);
}
