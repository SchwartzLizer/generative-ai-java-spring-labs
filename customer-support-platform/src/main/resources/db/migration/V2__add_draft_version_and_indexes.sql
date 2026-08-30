ALTER TABLE response_draft
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

CREATE INDEX idx_draft_created_at ON response_draft(created_at);
