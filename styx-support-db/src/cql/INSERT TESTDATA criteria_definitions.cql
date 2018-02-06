INSERT INTO criteria_definitions (name, event, criteria)
VALUES ('TransferMoneyFromSavingToChecking2', 'Shopping', 'not(Flag=''Y'' and Id=''N'' and Address=''Y'' and Email=''Y'' and Gender=''N'')
                                                           and not(Flag=''N'' and Id=''N'' and Address=''N'' and Email=''N'' and Gender=''N'')
                                                           and not(Flag=''N'' and Id=''N'' and Address=''Y'' and Email=''Y'' and Gender=''N'')
                                                           and not(Flag=''Y'' and Id=''Y'' and Address=''N'' and Email=''Y'' and Gender=''Y'')
                                                           and Age>18
                                                           and Age<119
                                                           and Count > 1000
                                                           and AverageMonthlyExpenditures>3100'
);

INSERT INTO criteria_definitions (name, event, criteria)
VALUES ('TestNotificationDefinition', 'Dummy', '');

INSERT INTO criteria_definitions (name, event, criteria)
VALUES ('TransactionFraud', 'Fraud', 'AMOUNT > 0.5');

INSERT INTO criteria_definitions (name, event, criteria)
VALUES ('CarLoanOffer', 'InterestedInCar', 'AGE > 18');
