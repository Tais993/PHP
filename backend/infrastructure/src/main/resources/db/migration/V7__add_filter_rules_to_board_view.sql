ALTER TABLE board_view
    ADD COLUMN filter_rules JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD CONSTRAINT board_view_filter_rules_array CHECK (jsonb_typeof(filter_rules) = 'array');

CREATE INDEX task_board_status_idx ON task (board_id, status);
CREATE INDEX task_board_due_date_idx ON task (board_id, due_date);
