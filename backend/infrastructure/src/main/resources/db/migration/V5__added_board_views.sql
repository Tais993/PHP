CREATE TABLE board_view
(
    id             BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    board_id       BIGINT       NOT NULL REFERENCES board (id) ON DELETE CASCADE,
    name           VARCHAR(255) NOT NULL CHECK (btrim(name) <> ''),
    type           VARCHAR(20)  NOT NULL CHECK (type IN ('BOARD', 'TABLE', 'ROADMAP')),
    group_by       VARCHAR(20)  CHECK (group_by IN ('STATUS')),
    sort_by        VARCHAR(20)  NOT NULL CHECK (sort_by IN ('TITLE', 'CREATED_AT')),
    sort_direction VARCHAR(4)   NOT NULL CHECK (sort_direction IN ('ASC', 'DESC')),
    position       INTEGER      NOT NULL CHECK (position >= 0)
);

CREATE INDEX board_view_board_position_idx ON board_view (board_id, position, id);
