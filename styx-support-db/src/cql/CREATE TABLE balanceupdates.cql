CREATE TABLE balanceupdates (
	card_id text,
    timestamp timestamp,
	balance_delta double,
	PRIMARY KEY (card_id, timestamp)
) WITH CLUSTERING ORDER BY (timestamp ASC)
AND default_time_to_live = 0;
