CREATE TABLE actionable_insights.customerprofiles (
    Flag text,
    Id text,
    Age text,
    AccountNumber text,
    Address text,
    Email text,
    Gender text,
    Budget text,
    CreditCard text,
    AverageMonthlyExpenditures text,
    Segment text,
    Rating text,
    ContactDate text,
	PRIMARY KEY (Id)
) WITH default_time_to_live = 0;
