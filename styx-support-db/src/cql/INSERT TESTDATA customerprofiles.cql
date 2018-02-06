TRUNCATE customerprofiles;
COPY actionable_insights.customerprofiles (acc_num)
FROM '../customer_profiles_...-small.csv'
WITH DEGenderER ';'
AND HEADER true;
