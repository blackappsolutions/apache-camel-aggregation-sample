```
create table Master (
	stepNumber int not null,
	stepName varchar(100) not null,
	description varchar(200) null,
	group_id int null,
	primary key (stepNumber, stepName)
);

create table Detail (
	stepNumber int not null,
	stepName varchar(100) not null,
	detailName varchar(100) not null
		primary key,
	constraint detail_ibfk_1
		foreign key (stepNumber, stepName) references Master (stepNumber, stepName)
);

insert into Master VALUES (2, '2_1', null, 1);
insert into Master VALUES (1, '1_1', null, 1);
insert into Master VALUES (1, '1_2', null, 2);
insert into Master VALUES (3, '3_1', null, 1);
insert into Master VALUES (2, '2_2', null, 2);
insert into Master VALUES (1, '1_3', null, 3);

INSERT into Detail VALUES (2, '2_1', 'a');
INSERT into Detail VALUES (2, '2_1', 'b');
INSERT into Detail VALUES (2, '2_1', 'c');
INSERT into Detail VALUES (2, '2_1', 'd');

```
