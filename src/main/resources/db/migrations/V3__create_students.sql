CREATE TABLE students (

)

/*
with recursive children as (
select id, parent_id, 1 as depth from recfoo where id =10
union
select rc.id, rc.parent_id, depth + 1 from recfoo rc join children c on rc.parent_id = c.id
)
select * from children;
*/