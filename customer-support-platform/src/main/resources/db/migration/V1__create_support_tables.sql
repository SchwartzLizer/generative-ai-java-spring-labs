CREATE TABLE feedback (
    id UUID PRIMARY KEY,
    customer_reference VARCHAR(100) NOT NULL,
    message VARCHAR(4000) NOT NULL,
    status VARCHAR(32) NOT NULL CHECK (status IN ('NEW','ANALYZED','IN_PROGRESS','RESOLVED','CLOSED')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_feedback_status ON feedback(status);
CREATE INDEX idx_feedback_created_at ON feedback(created_at);

CREATE TABLE feedback_analysis (
    id UUID PRIMARY KEY,
    feedback_id UUID NOT NULL REFERENCES feedback(id) ON DELETE CASCADE,
    sentiment VARCHAR(32) NOT NULL CHECK (sentiment IN ('POSITIVE','NEUTRAL','NEGATIVE')),
    category VARCHAR(32) NOT NULL CHECK (category IN ('SECURITY','BILLING','TECHNICAL','DELIVERY','GENERAL')),
    urgency VARCHAR(32) NOT NULL CHECK (urgency IN ('HIGH','MEDIUM','LOW')),
    recommended_action VARCHAR(1000) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    model VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_analysis_feedback ON feedback_analysis(feedback_id);
CREATE INDEX idx_analysis_sentiment ON feedback_analysis(sentiment);
CREATE INDEX idx_analysis_category ON feedback_analysis(category);
CREATE INDEX idx_analysis_urgency ON feedback_analysis(urgency);

CREATE TABLE response_draft (
    id UUID PRIMARY KEY,
    feedback_id UUID NOT NULL REFERENCES feedback(id) ON DELETE CASCADE,
    content VARCHAR(4000) NOT NULL,
    decision VARCHAR(32) NOT NULL CHECK (decision IN ('PENDING','APPROVED','REJECTED')),
    provider VARCHAR(50) NOT NULL,
    model VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    decided_at TIMESTAMPTZ
);
CREATE INDEX idx_draft_feedback ON response_draft(feedback_id);
CREATE INDEX idx_draft_decision ON response_draft(decision);
