CREATE TABLE customers (
	acc_num int,
	card_id text,
	card_balance double,
	Age int,
	Gender int,
	Count int,
	AverageMonthlyExpenditures int,
	Flag text,
	Id text,
	Address text,
	Email text,
	Gender text,
	PRIMARY KEY (acc_num, card_id)
) WITH default_time_to_live = 0;
