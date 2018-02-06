CREATE TABLE cep (
	raw_event text,
	business_event text,
	criteria text,
	model text,
	name text,
	PRIMARY KEY (raw_event, name)
) WITH default_time_to_live = 0;