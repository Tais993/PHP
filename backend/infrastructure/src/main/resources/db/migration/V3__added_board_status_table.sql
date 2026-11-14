CREATE TABLE board_status
(
    id       BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    board_id BIGINT       NOT NULL REFERENCES board (id),
    name     VARCHAR(255) NOT NULL,
    position INTEGER      NOT NULL CHECK (position >= 0),

    UNIQUE (board_id, name),
    UNIQUE (board_id, id)
);

ALTER TABLE task
    ADD COLUMN status_id BIGINT,
    ADD CONSTRAINT task_board_status_fk
        FOREIGN KEY (board_id, status_id)
            REFERENCES board_status (board_id, id);