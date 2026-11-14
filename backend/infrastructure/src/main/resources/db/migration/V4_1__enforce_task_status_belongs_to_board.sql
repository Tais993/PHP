ALTER TABLE task
    DROP CONSTRAINT task_board_status_fk;

ALTER TABLE task
    ADD CONSTRAINT task_board_status_fk
        FOREIGN KEY (board_id, status)
            REFERENCES board_status (board_id, id);
