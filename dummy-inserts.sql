-- Dummy Inserts

insert into users values (
	'e5806d71-8b80-4159-bb53-b481e617ca95',
	'a@example.org',
	'John',
	'johndoe',
	'5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8',
	null,
	false,
	false
);

insert into users values (
	'b338c989-9074-4ab4-b4a5-d45e9ae8d8da',
	'b@example.com',
	'Alex',
	'friendlyalex',
	'7e0e2d248518efe1cf4cefb953b53665e7a8b1f5c60beea19cc764fddf981d0e',
	null,
	false,
	false
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
	'2015-05-02',
	'2015-05-04',
	'b6578372-628e-4987-a38c-ef7cacc44d0d'
);

insert into events values (
	'bd6d92e5-2abf-4289-83cf-e8926ca78e3f',
	'This is another title',
	'This is another description',
	'2015-04-01',
	null,
	'5af853c7-94bb-4ca9-b6a5-28782b89ee0f'
);

insert into events_attendees values (
	'e5806d71-8b80-4159-bb53-b481e617ca95',
	'8da83e0d-32dd-4b1a-968c-a79e13ee57bf'
);

insert into events_attendees values (
	'b338c989-9074-4ab4-b4a5-d45e9ae8d8da',
	'8da83e0d-32dd-4b1a-968c-a79e13ee57bf'
);

insert into events_author values (
	'e5806d71-8b80-4159-bb53-b481e617ca95',
	'8da83e0d-32dd-4b1a-968c-a79e13ee57bf'
);

insert into events_author values (
	'e5806d71-8b80-4159-bb53-b481e617ca95',
	'bd6d92e5-2abf-4289-83cf-e8926ca78e3f'
);

insert into comments values (
	'e857483c-4045-4c42-ba49-3ada5d269cc2',
	-- Reply to self
	'e857483c-4045-4c42-ba49-3ada5d269cc2',
	'This is a sample comment',
	20,
	0
);

insert into comments values (
	'90149e01-6b3a-4d17-98e5-7b1a09747e33',
	-- Paren is comment by John
	'e857483c-4045-4c42-ba49-3ada5d269cc2',
	'This is bait comment',
	5,
	-50
);

insert into events_comments values (
	'8da83e0d-32dd-4b1a-968c-a79e13ee57bf',
	'e857483c-4045-4c42-ba49-3ada5d269cc2'
);

insert into events_comments values (
	'8da83e0d-32dd-4b1a-968c-a79e13ee57bf',
	'90149e01-6b3a-4d17-98e5-7b1a09747e33'
);

insert into users_comments values (
	'e5806d71-8b80-4159-bb53-b481e617ca95',
	'e857483c-4045-4c42-ba49-3ada5d269cc2'
);

insert into users_comments values (
	'b338c989-9074-4ab4-b4a5-d45e9ae8d8da',
	'90149e01-6b3a-4d17-98e5-7b1a09747e33'
);

insert into reports values (
	'0aefe840-b854-4e81-b07e-0bc5d0f2def6',
	'e5806d71-8b80-4159-bb53-b481e617ca95',
	'bait comment'
);

insert into comments_reports values (
	'0aefe840-b854-4e81-b07e-0bc5d0f2def6',
	'90149e01-6b3a-4d17-98e5-7b1a09747e33'	
);
