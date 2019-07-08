package ai.styx.frameworks.cassandra.customerprofiles

import ai.styx.frameworks.cassandra.CassandraRepository
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy
import com.datastax.driver.core.{ConsistencyLevel, PreparedStatement, Row, Statement}
import ai.styx.domain.{Customer, InitialBalance}
import org.joda.time.DateTime

import scala.collection.JavaConverters._

class CassandraCustomerProfileQueries(repo: CassandraRepository) {

  val customerSerializer = new CustomerProfileSerializer()

  val get_customer_profile: PreparedStatement = repo.session.prepare(
    "SELECT Flag,Id,Age,Address,Email,Gender,Gender,Count,AverageMonthlyExpenditures,acc_num" +
      ",Segment,ContactDate,Rating,CreditCard, Budget" +
      " FROM customerprofiles WHERE acc_num=?;")

  val add_customer_profile: PreparedStatement = repo.session.prepare(
    "INSERT INTO customerprofiles (Flag, Id, Age, Address, Email, Gender, Budget, CreditCard, Count, AverageMonthlyExpenditures, Segment, Rating, ContactDate) " +
      "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);")

  def createCustomer(row: Row) = {
    val colNames = row.getColumnDefinitions.asList().asScala.map(_.getName.toUpperCase)
    val colValues = colNames.indices.map(row.getObject)
    Customer(
      Flag = row.getString(0),
      Id = row.getString(1),
      Age = row.getString(2).toInt,
      Address = row.getString(3),
      Email = row.getString(4),
      Gender = row.getString(5),
      Count = row.getString(6).toInt,
      AverageMonthlyExpenditures = row.getString(7).toInt,
      AccountNumber = row.getInt(8),
      all = colNames.map(_.toUpperCase()).zip(colValues).toMap,
      Segment = row.getString(9),
      ContactDate =  row.getString(10),
      Rating = row.getString(11),
      CreditCard =row.getString(12),
      Budget = row.getString(13)
      )
  }

  def createInitialBalance(row: Row) = InitialBalance(
    AccountNumber = row.getInt(0),
    CardId = row.getString(1),
    Balance = row.getDouble(2)
  )

  def bindGetCustomerProfile(accNum: Int): Statement = {
    get_customer_profile.bind(
      accNum.asInstanceOf[java.lang.Integer]
    ).setIdempotent(true).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM).setRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
  }

  def bindGetInitialBalance(cardId: String): Statement = {
    get_initial_balance.bind(
      cardId
    ).setIdempotent(true).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM).setRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
  }

  def bindAddInitialBalance(card_id: String, acc_num: Int, balance: Double, card_type: String): Statement = {
    add_initial_balance.bind(
      card_id,
      acc_num.asInstanceOf[Integer],
      balance.asInstanceOf[Object],
      card_type
    ).setIdempotent(true).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM).setRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
  }

  def bindAddCustomerProfile(customer: Customer): Statement = {
    add_customer_profile.bind(
      customerSerializer.toObjectList(customer): _*
    ).setIdempotent(true).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM).setRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
  }

  val get_transaction_values: PreparedStatement = repo.session.prepare(
    "SELECT balance_delta FROM balanceupdates WHERE card_id=? and timestamp<?;"
  )

  val get_initial_balance: PreparedStatement = repo.session.prepare(
    "SELECT card_balance FROM initialbalance WHERE card_id=?;"
  )

  def bindGetTransactionValues(cardId: String, maxDateTime: DateTime): Statement = {
    get_transaction_values.bind(
      cardId,
      maxDateTime.toDate
    ).setIdempotent(true).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM).setRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
  }

  val add_initial_balance: PreparedStatement = repo.session.prepare(
    "INSERT INTO initialbalance (card_id,acc_num, card_balance, card_type) VALUES (?,?,?,?);"
  )

  val add_transaction: PreparedStatement = repo.session.prepare(
    "INSERT INTO balanceupdates (card_id, timestamp, balance_delta) VALUES (?, ?, ?);"
  )

  def bindAddTransaction(cardId: String, timestamp: DateTime, balanceDelta: Double): Statement = {
    add_transaction.bind(
      cardId,
      timestamp.toDate,
      balanceDelta.asInstanceOf[Object]
    )
  }

  // TODO: make more generic, add use case parameter??
  val get_cep_parameters: PreparedStatement = repo.session.prepare(
    "SELECT param_value FROM cepparameters WHERE param_name = ?;"
  )

  def bindGetCepPeriod: Statement = {
    get_cep_parameters.bind("trs_window")
  }

  def bindGetCepMinCountTrigger: Statement = {
    get_cep_parameters.bind("event_count")
  }

  def bindGetCepBalanceThreshold: Statement = {
    get_cep_parameters.bind("balance_threshold")
  }
}
