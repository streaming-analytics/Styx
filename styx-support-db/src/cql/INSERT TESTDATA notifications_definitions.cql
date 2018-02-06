INSERT INTO notification_definitions (name, event, model, threshold, message)
VALUES ('TransferMoneyFromSavingToChecking2', 'Shopping', 'customer_profile_14_lr.pmml', 0.01,
	'We noticed that you are [event.EVENT], but your balance is dropping below zero soon. Your current balance is [customer.BALANCE] Do you want to transfer [notification.AMOUNT] euro from your savings to your checking account?'
);

INSERT INTO notification_definitions (name, event, model, threshold, message)
VALUES ('TestNotificationDefinition', 'Dummy', 'test1.pmml', 0.5, 'This is a test message!');
