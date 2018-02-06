TRUNCATE cep;
INSERT INTO cep ( name, raw_event, business_event, criteria, model)
 VALUES ('Shopping', 'UpdateCardBalance', 'Shopping',  'Age > 16', 'Shopping.pmml')
