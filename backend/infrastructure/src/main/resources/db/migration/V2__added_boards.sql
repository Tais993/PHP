CREATE TABLE board
(
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    title       VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL
);

ALTER TABLE task
    ADD COLUMN board_id BIGINT REFERENCES board (id);


-- Create a board and assign all board_id's temporarily to that board
-- after that the board_id can be not null
WITH initial_board AS (
    INSERT INTO board (title, description)
        VALUES ('Inbox', 'Existing tasks')
        RETURNING id)
UPDATE task
SET board_id = (SELECT id FROM initial_board);

ALTER TABLE task
    ALTER COLUMN board_id SET NOT NULL;