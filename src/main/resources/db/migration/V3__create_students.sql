CREATE TABLE students (
    id              SERIAL          PRIMARY KEY,
    full_name       VARCHAR(255)    NOT NULL,
    promotion_id    INT             REFERENCES promotions(id)
)

/*
WITH RECURSIVE children AS (
    SELECT id, parent_id, 1 AS depth
      FROM recfoo
      WHERE id = 10
    UNION
    SELECT rc.id, rc.parent_id, depth + 1
      FROM recfoo rc
      JOIN children c
        ON rc.parent_id = c.id
)
SELECT * FROM children;
*/