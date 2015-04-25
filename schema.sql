-- h2

drop table if exists users cascade;
drop table if exists users_users cascade;
drop table if exists events cascade;
drop table if exists coordinates cascade;
drop table if exists events_coordinates cascade;
drop table if exists comments cascade;

create table users (
	usersId uuid primary key,
	email varchar(250) not null,
	name varchar,
	username varchar(50) not null,
	-- TODO: Change after we truly know the hash length - Using SHA(256) for now
	password varchar(128) not null
);

create table users_users (
	usersId uuid references users(usersId),
	friendId uuid references users(usersId)
);

create table coordinates (
	coordinatesId uuid primary key,
	latitude decimal not null,
	longitude decimal not null
);

create table events (
	eventsId uuid primary key,
	title varchar(50) not null,
	description varchar not null,
	author uuid references users(usersId),
	attending int not null,
	initialDate date not null,
	finalDate date,
	coordinatesId uuid references coordinates (coordinatesId)
);

create table comments (
	commentsId uuid primary key,
	eventId uuid references events(eventsId),
	-- ID of parent comment - if it's the same as self, there are no parents
	parentId uuid references comments(commentsId) on delete cascade,
	content varchar not null,
	author uuid references users(usersId),
	positiveVotes int,
	negativeVotes int
);

-- Dummy Inserts

insert into users values (
	'e5806d71-8b80-4159-bb53-b481e617ca95',
	'a@example.org',
	'John',
	'johndoe',
	'5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8'
);

insert into users values (
	'b338c989-9074-4ab4-b4a5-d45e9ae8d8da',
	'b@example.com',
	'Alex',
	'friendlyalex',
	'7e0e2d248518efe1cf4cefb953b53665e7a8b1f5c60beea19cc764fddf981d0e'
);

insert into users_users values (
	'e5806d71-8b80-4159-bb53-b481e617ca95',
	'b338c989-9074-4ab4-b4a5-d45e9ae8d8da'
);

insert into users_users values (
	'b338c989-9074-4ab4-b4a5-d45e9ae8d8da',
	'e5806d71-8b80-4159-bb53-b481e617ca95'
);

insert into coordinates values (
	'b6578372-628e-4987-a38c-ef7cacc44d0d',
	-40.32,
	50.24
);

insert into coordinates values (
	'5af853c7-94bb-4ca9-b6a5-28782b89ee0f',
	20,
	-60.5
);

insert into events values (
	'8da83e0d-32dd-4b1a-968c-a79e13ee57bf',
	'This is a title',
	'This is a description',
	-- author: John
	'e5806d71-8b80-4159-bb53-b481e617ca95',
	20,
	'2015-05-02',
	'2015-05-04',
	'b6578372-628e-4987-a38c-ef7cacc44d0d'
);

insert into events values (
	'bd6d92e5-2abf-4289-83cf-e8926ca78e3f',
	'This is another title',
	'This is another description',
	-- author: Alex
	'b338c989-9074-4ab4-b4a5-d45e9ae8d8da',
	20,
	'2015-04-01',
	'2015-05-29',
	'5af853c7-94bb-4ca9-b6a5-28782b89ee0f'
);

insert into comments values (
	'e857483c-4045-4c42-ba49-3ada5d269cc2',
	'bd6d92e5-2abf-4289-83cf-e8926ca78e3f',
	-- Self
	'e857483c-4045-4c42-ba49-3ada5d269cc2',
	'This is a sample comment',
	-- Author is John
	'e5806d71-8b80-4159-bb53-b481e617ca95',
	20,
	0
);

insert into comments values (
	'90149e01-6b3a-4d17-98e5-7b1a09747e33',
	'bd6d92e5-2abf-4289-83cf-e8926ca78e3f',
	-- Paren is comment by John
	'e857483c-4045-4c42-ba49-3ada5d269cc2',
	'This is bait comment',
	-- Author is Alex
	'b338c989-9074-4ab4-b4a5-d45e9ae8d8da',
	5,
	-50
);
