CREATE TABLE students (
    id              SERIAL          PRIMARY KEY,
    full_name       VARCHAR(255)    NOT NULL,
    promotion_id    INT             NOT NULL    REFERENCES promotions(year),
    genealogy       ltree
);

CREATE INDEX genealogy_tree_idx ON students USING GIST (genealogy);

CREATE OR REPLACE FUNCTION students_default_genealogy_is_self_on_insert_or_update() RETURNS trigger AS
$BODY$
  BEGIN
    IF NEW.genealogy IS NULL OR NEW.genealogy = '' THEN
      NEW.genealogy = NEW.id :: TEXT;
    END IF;
    RETURN NEW;
  END;
$BODY$
LANGUAGE plpgsql;

CREATE TRIGGER people_insert
  BEFORE INSERT OR UPDATE ON students
  FOR EACH ROW
  EXECUTE PROCEDURE students_default_genealogy_is_self_on_insert_or_update();


CREATE OR REPLACE FUNCTION validate_student_is_last_of_own_genealogy() RETURNS trigger AS
$BODY$
  BEGIN
    IF NOT NEW.genealogy ~ ('*.' || NEW.id :: text)::lquery THEN
      RAISE EXCEPTION 'Own id % is not last of genealogy %', NEW.id, NEW.genealogy;
    END IF;
    RETURN NEW;
  END;
$BODY$
IMMUTABLE
LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER genealogy_tree_has_self_as_last
  AFTER INSERT OR UPDATE on students
  FOR EACH ROW
  EXECUTE PROCEDURE validate_student_is_last_of_own_genealogy();


/*
WITH RECURSIVE children AS (
    SELECT id, parent_id, 1 AS depth
        FROM students
        WHERE id = 10
    UNION
    SELECT rc.id, rc.parent_id, depth + 1
        FROM students rc
        JOIN children c
            ON rc.parent_id = c.id
)
SELECT * FROM children;
*/
