CREATE TABLE actionable_insights.initialbalance (
    card_id text,
	acc_num int,
	card_balance double,
	card_type text,
	PRIMARY KEY (card_id)
) WITH default_time_to_live = 0;
