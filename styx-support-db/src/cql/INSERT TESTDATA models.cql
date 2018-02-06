INSERT INTO models (name, pmml) VALUES ('customer_profile_14_lr.pmml', textAsBlob('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<PMML xmlns="http://www.dmg.org/PMML-4_3" version="4.3">
	<Header>
		<Application name="JPMML-SkLearn" version="1.1.4"/>
		<Timestamp>2017-01-19T15:01:02Z</Timestamp>
	</Header>
	<MiningBuildTask>
		<Extension name="mapper" value="repr(mapper)">DataFrameMapper(default=False,
        features=[([''customer.Age'', ''customer.Gender'', ''customer.Count'', ''customer.AverageMonthlyExpenditures''], None), ([''customer.Flag''], LabelEncoder()), ([''customer.Id''], LabelEncoder()), ([''customer.Address''], LabelEncoder()), ([''customer.Email''], LabelEncoder()), ([''customer.Gender''], LabelEncoder()), (''Y'', None)],
        sparse=False)</Extension>
		<Extension name="estimator" value="repr(estimator)">LogisticRegression(C=1.0, class_weight=None, dual=False, fit_intercept=True,
          intercept_scaling=1, max_iter=100, multi_class=''ovr'', n_jobs=1,
          penalty=''l2'', random_state=None, solver=''liblinear'', tol=0.0001,
          verbose=0, warm_start=False)</Extension>
	</MiningBuildTask>
	<DataDictionary>
		<DataField name="customer.Age" optype="continuous" dataType="double"/>
		<DataField name="customer.Gender" optype="continuous" dataType="double"/>
		<DataField name="customer.Count" optype="continuous" dataType="double"/>
		<DataField name="customer.AverageMonthlyExpenditures" optype="continuous" dataType="double"/>
		<DataField name="customer.Flag" optype="categorical" dataType="string">
			<Value value="N"/>
			<Value value="Y"/>
		</DataField>
		<DataField name="customer.Id" optype="categorical" dataType="string">
			<Value value="N"/>
			<Value value="Y"/>
		</DataField>
		<DataField name="customer.Address" optype="categorical" dataType="string">
			<Value value="N"/>
			<Value value="Y"/>
		</DataField>
		<DataField name="customer.Email" optype="categorical" dataType="string">
			<Value value="N"/>
			<Value value="Y"/>
		</DataField>
		<DataField name="customer.Gender" optype="categorical" dataType="string">
			<Value value="N"/>
			<Value value="Y"/>
		</DataField>
		<DataField name="Y" optype="categorical" dataType="integer">
			<Value value="0"/>
			<Value value="1"/>
		</DataField>
	</DataDictionary>
	<TransformationDictionary>
		<DefineFunction name="logit" optype="continuous" dataType="double">
			<ParameterField name="value" optype="continuous" dataType="double"/>
			<Apply function="/">
				<Constant dataType="double">1</Constant>
				<Apply function="+">
					<Constant dataType="double">1</Constant>
					<Apply function="exp">
						<Apply function="*">
							<Constant dataType="double">-1</Constant>
							<FieldRef field="value"/>
						</Apply>
					</Apply>
				</Apply>
			</Apply>
		</DefineFunction>
		<DerivedField name="label_encoder(customer.Flag)" optype="continuous" dataType="double">
			<MapValues outputColumn="output">
				<FieldColumnPair field="customer.Flag" column="input"/>
				<InlineTable>
					<row>
						<input xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">N</input>
						<output xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">0</output>
					</row>
					<row>
						<input xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">Y</input>
						<output xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">1</output>
					</row>
				</InlineTable>
			</MapValues>
		</DerivedField>
		<DerivedField name="label_encoder(customer.Id)" optype="continuous" dataType="double">
			<MapValues outputColumn="output">
				<FieldColumnPair field="customer.Id" column="input"/>
				<InlineTable>
					<row>
						<input xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">N</input>
						<output xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">0</output>
					</row>
					<row>
						<input xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">Y</input>
						<output xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">1</output>
					</row>
				</InlineTable>
			</MapValues>
		</DerivedField>
		<DerivedField name="label_encoder(customer.Address)" optype="continuous" dataType="double">
			<MapValues outputColumn="output">
				<FieldColumnPair field="customer.Address" column="input"/>
				<InlineTable>
					<row>
						<input xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">N</input>
						<output xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">0</output>
					</row>
					<row>
						<input xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">Y</input>
						<output xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">1</output>
					</row>
				</InlineTable>
			</MapValues>
		</DerivedField>
		<DerivedField name="label_encoder(customer.Email)" optype="continuous" dataType="double">
			<MapValues outputColumn="output">
				<FieldColumnPair field="customer.Email" column="input"/>
				<InlineTable>
					<row>
						<input xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">N</input>
						<output xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">0</output>
					</row>
					<row>
						<input xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">Y</input>
						<output xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">1</output>
					</row>
				</InlineTable>
			</MapValues>
		</DerivedField>
		<DerivedField name="label_encoder(customer.Gender)" optype="continuous" dataType="double">
			<MapValues outputColumn="output">
				<FieldColumnPair field="customer.Gender" column="input"/>
				<InlineTable>
					<row>
						<input xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">N</input>
						<output xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">0</output>
					</row>
					<row>
						<input xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">Y</input>
						<output xmlns:ns2="http://www.dmg.org/PMML-4_3" xmlns="">1</output>
					</row>
				</InlineTable>
			</MapValues>
		</DerivedField>
	</TransformationDictionary>
	<MiningModel functionName="classification">
		<MiningSchema>
			<MiningField name="Y" usageType="target"/>
			<MiningField name="customer.Age"/>
			<MiningField name="customer.Gender"/>
			<MiningField name="customer.Count"/>
			<MiningField name="customer.AverageMonthlyExpenditures"/>
			<MiningField name="customer.Flag"/>
			<MiningField name="customer.Id"/>
			<MiningField name="customer.Address"/>
			<MiningField name="customer.Email"/>
			<MiningField name="customer.Gender"/>
		</MiningSchema>
		<Segmentation multipleModelMethod="modelChain">
			<Segment id="1">
				<True/>
				<RegressionModel functionName="regression">
					<MiningSchema>
						<MiningField name="customer.Age"/>
						<MiningField name="customer.Gender"/>
						<MiningField name="customer.Count"/>
						<MiningField name="customer.AverageMonthlyExpenditures"/>
						<MiningField name="customer.Flag"/>
						<MiningField name="customer.Id"/>
						<MiningField name="customer.Address"/>
						<MiningField name="customer.Email"/>
						<MiningField name="customer.Gender"/>
					</MiningSchema>
					<Output>
						<OutputField name="decisionFunction_1" optype="continuous" dataType="double" feature="predictedValue" isFinalResult="false"/>
					</Output>
					<RegressionTable intercept="6.716220774935685E-6">
						<NumericPredictor name="customer.Age" coefficient="-1.9250737481308245E-4"/>
						<NumericPredictor name="customer.Gender" coefficient="-5.480692705503138E-7"/>
						<NumericPredictor name="customer.Count" coefficient="5.301425581328437E-7"/>
						<NumericPredictor name="customer.AverageMonthlyExpenditures" coefficient="6.241578653394229E-6"/>
						<NumericPredictor name="label_encoder(customer.Flag)" coefficient="4.947328922040208E-5"/>
						<NumericPredictor name="label_encoder(customer.Id)" coefficient="5.383036690477135E-5"/>
						<NumericPredictor name="label_encoder(customer.Address)" coefficient="-8.634743034868645E-5"/>
						<NumericPredictor name="label_encoder(customer.Email)" coefficient="-5.648207501591915E-6"/>
						<NumericPredictor name="label_encoder(customer.Gender)" coefficient="-5.894823384660009E-5"/>
					</RegressionTable>
				</RegressionModel>
			</Segment>
			<Segment id="2">
				<True/>
				<RegressionModel functionName="classification" normalizationMethod="softmax">
					<MiningSchema>
						<MiningField name="Y" usageType="target"/>
						<MiningField name="decisionFunction_1"/>
					</MiningSchema>
					<Output>
						<OutputField name="probability_0" optype="continuous" dataType="double" feature="probability" value="0"/>
						<OutputField name="probability_1" optype="continuous" dataType="double" feature="probability" value="1"/>
					</Output>
					<RegressionTable intercept="0.0" targetCategory="0">
						<NumericPredictor name="decisionFunction_1" coefficient="-1.0"/>
					</RegressionTable>
					<RegressionTable intercept="0.0" targetCategory="1"/>
				</RegressionModel>
			</Segment>
		</Segmentation>
	</MiningModel>
</PMML>'));
