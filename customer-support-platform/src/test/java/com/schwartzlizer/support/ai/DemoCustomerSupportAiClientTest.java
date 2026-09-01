package com.schwartzlizer.support.ai;

import com.schwartzlizer.support.analysis.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DemoCustomerSupportAiClientTest {
    private final CustomerSupportAiClient client=new DemoCustomerSupportAiClient();
    @Test
    void identifiesUrgentTechnicalFailure(){
        var r=client.analyze("Payment error blocks checkout and the app crashes");
        assertThat(r.sentiment()).isEqualTo(Sentiment.NEGATIVE);
        assertThat(r.category()).isEqualTo(SupportCategory.TECHNICAL);
        assertThat(r.urgency()).isEqualTo(Urgency.HIGH);
        assertThat(r.recommendedAction()).isNotBlank();
    }
    @Test
    void draftsSafeResponseWithoutInventedResolution(){
        var a=new FeedbackAnalysisResult(Sentiment.NEGATIVE,SupportCategory.BILLING,Urgency.HIGH,"Route to billing support");
        String content=client.draftResponse("Unexpected charge",a).content();
        assertThat(content).containsIgnoringCase("billing").doesNotContainIgnoringCase("refund approved");
    }
}
