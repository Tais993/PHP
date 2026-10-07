CREATE TABLE task
(
    id          INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name        VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    created_at  DATE NOT NULL,

    status      VARCHAR(255) NOT NULL
);
