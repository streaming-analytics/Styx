CREATE TABLE actionable_insights.models (
	name text,
	pmml blob,
	PRIMARY KEY (name)
) WITH default_time_to_live = 0;
