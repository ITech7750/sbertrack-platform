ALTER TABLE agent_messages ADD COLUMN sequence_number BIGINT;

-- Historical ties have no recorded insertion order. Use the best available
-- chronology, placing a question before its answer when their timestamps tie.
WITH ordered AS (
    SELECT id, ROW_NUMBER() OVER (
        PARTITION BY session_id
        ORDER BY created_at, CASE WHEN role = 'USER' THEN 0 ELSE 1 END, id
    ) AS position
    FROM agent_messages
)
UPDATE agent_messages m SET sequence_number = ordered.position
FROM ordered WHERE m.id = ordered.id;

ALTER TABLE agent_messages
    ALTER COLUMN sequence_number SET NOT NULL,
    ADD CONSTRAINT chk_agent_message_sequence_positive CHECK (sequence_number > 0),
    ADD CONSTRAINT uq_agent_message_sequence UNIQUE (session_id, sequence_number);
