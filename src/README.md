```
create table MULTISTEPS
(
     STEP NUMBER not null
          constraint MULTISTEPS_PK
          primary key,
     DESCRIPTION VARCHAR2(100),
     GROUP_ID NUMBER
)

delete from MULTISTEPS;
insert into MULTISTEPS VALUES (1, 'abc', 1);
insert into MULTISTEPS VALUES (3, 'abc', 2);
insert into MULTISTEPS VALUES (4, 'abc', 2);
insert into MULTISTEPS VALUES (2, 'abc', 1);
insert into MULTISTEPS VALUES (5, 'abc', 2);

select
     t.STEP,
     (
     Select count(m.GROUP_ID)
     from MULTISTEPS m
     where m.GROUP_ID = t.GROUP_ID
     group by m.GROUP_ID
     ) AS "GRP_CNT"
from
     MULTISTEPS t
;
```
