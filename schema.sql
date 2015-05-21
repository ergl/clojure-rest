-- h2

drop table if exists users cascade;
drop table if exists sessions cascade;
drop table if exists users_users cascade;

drop table if exists coordinates cascade;
drop table if exists events cascade;
drop table if exists events_attendees cascade;
drop table if exists events_author cascade;

drop table if exists comments cascade;
drop table if exists events_comments cascade;
drop table if exists users_comments cascade;

drop table if exists reports cascade;
drop table if exists events_reports cascade;
drop table if exists comments_reports cascade;


create table users (
	usersId uuid primary key,
	email varchar(250) not null,
	name varchar,
	username varchar(50) not null,
	password varchar(162) not null,
	profileImage blob,
	deleted boolean not null,
	moderator boolean not null
);

create table sessions (
	token varchar(150) primary key,
	usersId uuid references users(usersId),
	createdAt timestamp not null
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
	initialDate date not null,
	finalDate date,
	coordinatesId uuid references coordinates (coordinatesId)
	-- TODO: Payments, fee?
);

create table events_attendees (
	usersId uuid references users(usersId),
	eventsId uuid references events(eventsId)
);

create table events_author (
	usersId uuid references users(usersId),
	eventsId uuid references events(eventsId)
);

create table comments (
	commentsId uuid primary key,
	-- ID of parent comment - if it's the same as self, there are no parents
	parentId uuid references comments(commentsId) on delete cascade,
	content varchar not null,
	positiveVotes int not null default 0,
	negativeVotes int not null default 0
);

create table events_comments (
	eventId uuid references events(eventsId),
	commentsId uuid references comments(commentsId)
	-- TODO: Deleting all comments once event is deleted?
);

create table users_comments (
	author uuid references users(usersId),
	commentsId uuid references comments(commentsId)
);

create table reports (
	reportsId uuid primary key,
	author uuid references users(usersId),
	content varchar
);

create table events_reports (
	reportsId uuid references reports(reportsId) on delete cascade,
	eventsId uuid references events(eventsId) on delete cascade
);

create table comments_reports (
	reportsId uuid references reports(reportsId) on delete cascade,
	commentsId uuid references comments(commentsId) on delete cascade
);


-- Constraints
create index idxusername on users(username);
create index idxemails on users(email);
