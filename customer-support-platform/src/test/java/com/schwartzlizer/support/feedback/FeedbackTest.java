package com.schwartzlizer.support.feedback;

import com.schwartzlizer.support.common.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class FeedbackTest {
    private final Instant now=Instant.parse("2026-08-28T12:00:00Z");
    @Test
    void newFeedbackStartsInNewStatus(){
        Feedback f=Feedback.create(UUID.randomUUID(),"CUST-001","App crashes",now);
        assertThat(f.status()).isEqualTo(FeedbackStatus.NEW);
        assertThat(f.createdAt()).isEqualTo(now);
    }
    @Test
    void followsAllowedLifecycle(){
        Feedback f=Feedback.create(UUID.randomUUID(),"CUST-001","App crashes",now);
        f.changeStatus(FeedbackStatus.ANALYZED,now.plusSeconds(1));
        f.changeStatus(FeedbackStatus.IN_PROGRESS,now.plusSeconds(2));
        f.changeStatus(FeedbackStatus.RESOLVED,now.plusSeconds(3));
        f.changeStatus(FeedbackStatus.CLOSED,now.plusSeconds(4));
        assertThat(f.status()).isEqualTo(FeedbackStatus.CLOSED);
    }
    @Test
    void rejectsClosingNewFeedback(){
        Feedback f=Feedback.create(UUID.randomUUID(),"CUST-001","App crashes",now);
        assertThatThrownBy(()->f.changeStatus(FeedbackStatus.CLOSED,now)).isInstanceOf(InvalidStateTransitionException.class).hasMessage("Cannot change feedback status from NEW to CLOSED");
    }
}
