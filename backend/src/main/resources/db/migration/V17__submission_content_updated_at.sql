ALTER TABLE submissions ADD COLUMN content_updated_at TIMESTAMPTZ;

-- Earlier versions did not record draft edit times. Submission time is the best
-- available historical value; undated drafts sort before dated/new work.
UPDATE submissions
SET content_updated_at = COALESCE(submitted_at, TIMESTAMPTZ '1970-01-01 00:00:00+00');

ALTER TABLE submissions
    ALTER COLUMN content_updated_at SET NOT NULL,
    ALTER COLUMN content_updated_at SET DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX idx_submissions_agent_context
    ON submissions (student_id, case_id, content_updated_at DESC, id ASC);
