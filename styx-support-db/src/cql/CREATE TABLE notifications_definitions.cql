CREATE TABLE notification_definitions (
	event text,
	message text,
	model text,
	name text,
	threshold double,
	PRIMARY KEY (event, name)
) WITH default_time_to_live = 0;