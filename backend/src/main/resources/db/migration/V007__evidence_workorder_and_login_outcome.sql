-- D17-T23 / DATA-002: retain upload resource ownership before round binding.
ALTER TABLE evidence_file ADD COLUMN work_order_id BIGINT UNSIGNED NULL;
ALTER TABLE evidence_file ADD KEY idx_evidence_work_order(work_order_id);
ALTER TABLE evidence_file ADD CONSTRAINT fk_evidence_work_order FOREIGN KEY(work_order_id) REFERENCES governance_work_order(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
-- D17-T30 / AUD-001: authentication success must not be recorded as ERROR.
ALTER TABLE security_event DROP CHECK ck_security_event_outcome;
ALTER TABLE security_event ADD CONSTRAINT ck_security_event_outcome CHECK(outcome IN ('DENIED','ERROR','SUCCESS'));
