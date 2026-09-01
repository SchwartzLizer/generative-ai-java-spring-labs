package com.schwartzlizer.support.ai;

import com.schwartzlizer.support.analysis.*;
import java.util.Locale;

public final class DemoCustomerSupportAiClient implements CustomerSupportAiClient {
    @Override
    public FeedbackAnalysisResult analyze(String message) {
        String text=message.toLowerCase(Locale.ROOT);
        Sentiment sentiment = containsAny(text,"excellent","helpful","resolved","fast","satisfied") && !containsAny(text,"broken","error","crash","fraud") ? Sentiment.POSITIVE : containsAny(text,"broken","error","crash","late","difficult","frustrated","fraud") ? Sentiment.NEGATIVE : Sentiment.NEUTRAL;
        SupportCategory category = containsAny(text,"fraud","password","login","security","hack") ? SupportCategory.SECURITY : containsAny(text,"crash","bug","technical") ? SupportCategory.TECHNICAL : containsAny(text,"payment","charge","billing","invoice","refund") ? SupportCategory.BILLING : containsAny(text,"error","app") ? SupportCategory.TECHNICAL : containsAny(text,"delivery","shipping","package","arrived","late") ? SupportCategory.DELIVERY : SupportCategory.GENERAL;
        Urgency urgency = containsAny(text,"blocked","crash","fraud","cannot pay","can't pay") ? Urgency.HIGH : sentiment == Sentiment.NEGATIVE ? Urgency.MEDIUM : Urgency.LOW;
        String action = switch (category) { case SECURITY -> "Route to security support and verify the account safely"; case BILLING -> "Route to billing support for transaction review"; case TECHNICAL -> "Request reproducible steps and route to technical support"; case DELIVERY -> "Check shipment status and coordinate delivery support"; case GENERAL -> "Acknowledge the feedback and route to the support queue"; };
        return new FeedbackAnalysisResult(sentiment, category, urgency, action);
    }
    @Override
    public ResponseDraftResult draftResponse(String message, FeedbackAnalysisResult analysis) {
        String next = switch (analysis.category()) { case SECURITY -> "Our security support team will review the account safely."; case BILLING -> "Our billing support team will review the transaction details."; case TECHNICAL -> "Our technical support team will review the reported steps."; case DELIVERY -> "Our delivery support team will check the shipment details."; case GENERAL -> "Our support team will review the details."; };
        return new ResponseDraftResult("Thanks for letting us know about this issue. " + next + " We will share an update after that review; this message does not promise a refund, date, or policy outcome.");
    }
    private boolean containsAny(String text,String... terms) {
        for(String t:terms) if(text.contains(t)) return true;
        return false;
    }
}
