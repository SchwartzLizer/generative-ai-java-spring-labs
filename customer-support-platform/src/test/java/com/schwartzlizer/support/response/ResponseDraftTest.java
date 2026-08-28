package com.schwartzlizer.support.response;

import com.schwartzlizer.support.feedback.Feedback;
import com.schwartzlizer.support.common.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class ResponseDraftTest {
    @Test void approveAndRejectAreTerminal(){ Feedback f=Feedback.create(UUID.randomUUID(),"CUST-001","Issue",Instant.now()); ResponseDraft d=ResponseDraft.create(UUID.randomUUID(),f,"Reply","demo","v1",Instant.now()); d.approve(Instant.now()); assertThat(d.decision()).isEqualTo(DraftDecision.APPROVED); assertThatThrownBy(()->d.reject(Instant.now())).isInstanceOf(InvalidStateTransitionException.class).hasMessage("Draft has already been decided"); }
    @Test void newDraftIsPending(){ Feedback f=Feedback.create(UUID.randomUUID(),"CUST-001","Issue",Instant.now()); assertThat(ResponseDraft.create(UUID.randomUUID(),f,"Reply","demo","v1",Instant.now()).decision()).isEqualTo(DraftDecision.PENDING); }
}
