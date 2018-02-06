CREATE TABLE criteria_definitions (
	criteria text,
	event text,
	name text,
	PRIMARY KEY (event, name)
) WITH default_time_to_live = 0;