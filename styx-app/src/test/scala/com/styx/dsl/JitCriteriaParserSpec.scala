package com.styx.dsl

import com.styx.common.BaseSpec
import com.styx.domain.Customer

class JitCriteriaParserSpec extends BaseSpec {

  def mockCustomer(accountNumber: Int) = Customer(
    Flag = "N",
    Id = "Y",
    Age = 50,
    Address = "Y",
    Email = "Y",
    Gender = "Y",
    Count = 1001,
    AverageMonthlyExpenditures = 3200,
    AccountNumber = accountNumber,
    all = Map(),
    "ME", "1970-01-01 01:01:01", "LO", "Y", "N")

  def mockFailCustomer(accountNumber: Int, age: Int, Gender: String="M") = Customer(
    Flag = "N",
    Id = "Y",
    Age = age,
    Address = "Y",
    Email = "Y",
    Gender = Gender,
    Count = 1001,
    AverageMonthlyExpenditures = 3200,
    AccountNumber = accountNumber,
    all = Map(),
    "ME", "1970-01-01 01:01:01", "LO", "Y", "N")

  val criteria: String = "" +
    "(Flag=='Y' OR Id=='N' OR Address=='Y' OR Email=='Y' OR Gender=='N') " +
    "AND (Flag=='N' OR Id=='N' OR Address=='N' OR Email=='N' OR Gender=='N') " +
    "AND (Flag=='N' OR Id=='N' OR Address=='Y' OR Email=='Y' OR Gender=='N')" +
    "AND (Flag=='Y' OR Id=='Y' OR Address=='N' OR Email=='Y' OR Gender=='Y')" +
    "AND Age>18" +
    "AND Age<119" +
    "AND Count > 1000" +
    "AND AverageMonthlyExpenditures>3100"

  val unparseableCriteria: String = "" +
    "(Flag=='Y' OR Id=='N' OR Address=='Y' OR Email=='Y' OR Gender=='N') " +
    "AND (Flag=='N' OR Id=='N' OR Address=='N' OR Email=='N' OR Gender=='N') " +
    "AND (Flag=='N' OR Id='N' OR Address=='Y' OR Email=='Y' OR Gender=='N')" +
    "AND (Flag=='Y' OR Id=='Y' OR Address=='N' OR Email=='Y' OR Gender=='Y')" +
    "AND Age>18" +
    "AND Age<119" +
    "AND Count > 1000" +
    "AND AverageMonthlyExpenditures>3100"

  val nonExistentKeyCriteria: String = "" +
    "(Flag=='Y' OR Id=='N' OR Address=='Y' OR Email=='Y' OR Gender=='N') " +
    "AND (Flag=='N' OR Id=='N' OR Address=='N' OR Email=='N' OR Gender=='N') " +
    "AND (Flag=='N' OR Id=='N' OR Address=='Y' OR Email=='Y' OR Gender=='N')" +
    "AND (Flag=='Y' OR Id=='Y' OR Address=='N' OR Email=='Y' OR Gender=='Y')" +
    "AND Age>18" +
    "AND Age<119" +
    "AND Count > 1000" +
    "AND AverageMonthlyExpenditures>3100"

  val incorrectTypeCriteria: String = "" +
    "(Flag=='Y' OR Id=='N' OR Address=='Y' OR Email=='Y' OR Gender=='N') " +
    "AND (Flag=='N' OR Id=='N' OR Address=='N' OR Email=='N' OR Gender=='N') " +
    "AND (Flag=='N' OR Id=='N' OR Address=='Y' OR Email=='Y' OR Gender=='N')" +
    "AND (Flag=='Y' OR Id=='Y' OR Address=='N' OR Email=='Y' OR Gender=='Y')" +
    "AND Age>18" +
    "AND Count > 1000" +
    "AND AverageMonthlyExpenditures>3100"

  "CriteriaParser" should "apply the notification criteria correctly" in {
    val filter = new JitCriteriaParser().parseCustomerCriteria(criteria)
    filter(mockCustomer(1)).right.get should be(true)
  }

  it should "display \"Failed to parse criteria at index 249.\" message when criteria can't be parsed" in {
    val filter = new JitCriteriaParser().parseCustomerCriteria(unparseableCriteria)
    filter(mockCustomer(1)).left.get should be(Seq("Failed to parse criteria at index 249."))
  }

  it should "display the message saying why there is no match." in {
    val filter = new JitCriteriaParser().parseCustomerCriteria(criteria)
    filter(mockFailCustomer(1,120)).left.get should be(Seq("age (equal to 120) is not less than 119"))
  }

  it should "display the sequence of messages saying why there is no match." in {
    val filter = new JitCriteriaParser().parseCustomerCriteria(criteria)
    filter(mockFailCustomer(1,17,"536")).left.get should be(Seq("age (equal to 17) is not greater than 18", "Gender (equal to 536) is not greater than 1000"))
  }

  it should "display a message saying the key does not exist." in {
    val filter = new JitCriteriaParser().parseCustomerCriteria(nonExistentKeyCriteria)
    filter(mockFailCustomer(1,17,"536")).left.get should contain("the Age is not in the map")
  }

  it should "display a message saying that strings cannot be compared." in {
    val filter = new JitCriteriaParser().parseCustomerCriteria(incorrectTypeCriteria)
    filter(mockFailCustomer(1,17,"536")).left.get should contain("Strings can't be compared with the '>'")
  }
}
