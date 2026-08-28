package com.schwartzlizer.support.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schwartzlizer.support.analysis.*;
import com.schwartzlizer.support.common.AiProviderException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class GeminiCustomerSupportAiClientTest {
    @Test void mapsStrictJsonAndBuildsSafePrompt(){ var seen=new StringBuilder(); var client=new GeminiCustomerSupportAiClient(p->{seen.append(p); return "{\"sentiment\":\"NEGATIVE\",\"category\":\"TECHNICAL\",\"urgency\":\"HIGH\",\"recommendedAction\":\"Investigate\"}";},new ObjectMapper(),new AnalysisPromptFactory(),new ResponsePromptFactory()); var result=client.analyze("The app crashes"); assertThat(result.category()).isEqualTo(SupportCategory.TECHNICAL); assertThat(seen).contains("JSON only","SECURITY","POSITIVE"); }
    @Test void rejectsUnknownOrMalformedJson(){ var client=new GeminiCustomerSupportAiClient(p->"{\"sentiment\":\"UNKNOWN\"}",new ObjectMapper(),new AnalysisPromptFactory(),new ResponsePromptFactory()); assertThatThrownBy(()->client.analyze("Issue")).isInstanceOf(AiProviderException.class); }
    @Test void draftPromptProhibitsInventedClaims(){ var seen=new StringBuilder(); var client=new GeminiCustomerSupportAiClient(p->{seen.append(p); return "safe reply";},new ObjectMapper(),new AnalysisPromptFactory(),new ResponsePromptFactory()); client.draftResponse("Issue",new FeedbackAnalysisResult(Sentiment.NEGATIVE,SupportCategory.BILLING,Urgency.HIGH,"Review")); assertThat(seen.toString()).containsIgnoringCase("refunds").containsIgnoringCase("dates").containsIgnoringCase("account actions"); }
}
