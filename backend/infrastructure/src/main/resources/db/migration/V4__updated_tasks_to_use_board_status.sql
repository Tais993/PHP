ALTER TABLE task DROP status;

ALTER TABLE task ADD
    status BIGINT REFERENCES board_status (id);