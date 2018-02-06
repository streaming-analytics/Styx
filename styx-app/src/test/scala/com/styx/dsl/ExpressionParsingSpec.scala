package com.styx.dsl

import cats.implicits._
import com.styx.dsl._
import fastparse.core.ParseError
import org.scalatest.{FunSuite, Matchers}
import UsefulImplicits._

class ExpressionParsingSpec extends FunSuite with Matchers {

  test("test fetching an Int") {
    val m = DataSource(Map("customer_id" -> 42, "customer_gender" -> "F"))
    val result: Either[String, Int] = m.fetch[Int]("customer_id")
    assert(result.isRight)
    assert(!result.isLeft)
    result.foreach((v: Int) => assert(v == 42))
  }

  test("test fetching a String ") {
    val m = DataSource(Map("customer_id" -> 42, "customer_gender" -> "F"))
    val result: Either[String, String] = m.fetch[String]("customer_gender")
    assert(result.isRight)
    assert(!result.isLeft)
    result.foreach((v: String) => assert(v == "F"))
  }

  test("test fetching and getting an error message") {
    val m = DataSource(Map("customer_id" -> 42, "customer_gender" -> "F"))
    val result = m.fetch[Int]("customer_age")
    assert(result.isLeft)
    assert(!result.isRight)
    result.swap.foreach((msg: String) => assert(msg.contains("not in the map")))
  }

  test("test fetching wrong type and getting an error message") {
    val m = DataSource(Map("customer_id" -> 42, "customer_gender" -> "F"))
    val result: Either[String, Int] = m.fetch[Int]("customer_gender")
    assert(result.isLeft)
    result.swap.foreach(msg => assert(msg.contains("not the right type")))
  }

  test("integer comparison expression") {
    val m = Map("customer_id" -> 42, "customer_gender" -> "F")

    // we don't support <> as an operator so this one should return a "left" (an error message...)
    assert(IntComparisonExpression("customer_id", "<>", 42).evaluate(m).isLeft)

    val result = IntComparisonExpression("customer_id", "==", 42).evaluate(m)
    assert(result.isRight)
    result.foreach(v => assert(v == True))
  }

  test("we can parse an identifier") {
    // parsing converts identifiers to lower case
    assert(Parsing.Identifier.parse("CUSTOMER_ID").get.value == "customer_id")
    assert(Parsing.Identifier.parse("CUSTOMER_GENDER").get.value == "customer_gender")
  }

  test("we can parse a string") {
    val str = "'ABCabc'"
    // note that when parsing a string we lose the single quotes and just get the value
    assert(Parsing.Values.Str.parse(str).get.value == "ABCabc")
  }

  test("we can parse an int") {
    assert(Parsing.Values.Int.parse("89").get.value == 89)
    intercept[ParseError[_,_]] {
      Parsing.Values.Int.parse("abc").get
    }
  }

  test("we can parse a simple expression (int comparison) and evaluate it") {
    val m = Map("CUSTOMER_ID" -> 42, "customer_gender" -> "F")
    val expr = "CUSTOMER_ID==42"
    val parsed = Parsing.SimpleExprs.IntCompareExpr.parse(expr).get.value
    assert(parsed.identifier == "customer_id")
    assert(parsed.op == "==")
    assert(parsed.valueToCompareAgainst == 42)
    val evaluationResult = parsed.evaluate(m)
    assert(evaluationResult.isRight)
    evaluationResult.foreach((result: Result) => assert(result == True))
  }

  test("we can parse a simple expression (string comparison) and evaluate it") {
    val m = Map("CUSTOMER_ID" -> 42, "CUSTOMER_GENDER" -> "F")
    val expr = "CUSTOMER_GENDER=='F'"
    val parsed = Parsing.SimpleExprs.StringCompareExpr.parse(expr).get.value
    assert(parsed.identifier == "customer_gender")
    assert(parsed.op == "==")
    assert(parsed.value == "F")
    val evaluationResult = parsed.evaluate(m)
    assert(evaluationResult.isRight)
    evaluationResult.foreach((result: Result) => assert(result == True))
  }

  test("AND-ing two expressions") {
    val e1 = IntComparisonExpression("customer_id", "==", 42)
    val e2 = StringComparisonExpression("customer_gender", "==", "F")
    val e = ComplexExpression(e1, "AND", e2).evaluate(
      Map("customer_id" -> 42, "customer_gender" -> "F")
    )
    assert(e.isRight)
    e.foreach(result => assert(result == True))
  }

  test("a more complicated expression") {
    val expr = "(CUSTOMER_GENDER=='F') AND (CUSTOMER_AGE==42)"
    val parsed = Parsing.ComplexExprs.E.parse(expr)
    val m = Map("CUSTOMER_AGE" -> 42, "CUSTOMER_GENDER" -> "F")
    val result = parsed.get.value.evaluate(m)
    assert(result.isRight)
    result.foreach(r => assert(r == True))
  }

  test("can the evaluator show us why the expression is false?") {
    val m = Map("CUSTOMER_AGE" -> 24,
      "CUSTOMER_GENDER" -> "f",
      "CUSTOMER_NAME" -> "Vivian",
      "IS_FUN" -> "true",
      "IS_RELAXING" -> "false",
      "LISTENS_TO_MUSIC" -> "false"
    )
    val expr = "customer_gender == 'm' and is_fun == 'false'"
    val evaluated = Parsing.ComplexExprs.E.parse(expr).get.value.evaluate(m)
    assert(evaluated.isRight)
    evaluated.foreach(result => {
      assert(result.isInstanceOf[False])
      assert(result.asInstanceOf[False].reasons.size == 2)
    })
  }

  test("most complicated") {
    val m = Map("CUSTOMER_AGE" -> 24,
      "CUSTOMER_GENDER" -> "f",
      "CUSTOMER_NAME" -> "Vivian",
      "IS_FUN" -> "true",
      "IS_RELAXING" -> "false",
      "LISTENS_TO_MUSIC" -> "false"
    )
    val expr = "(((   customer_gender == 'f'   )) and        (customer_age == 25)) or (is_fun == 'true' or is_relaxing == 'true' or listens_to_music == 'true')"
    val r  = Parsing.ComplexExprs.E.parse(expr).get.value
    r.evaluate(m) shouldBe Right(True)
  }

  test("Actual criteria") {
    val cust = Map(
      "Flag" -> "N",
      "Id" -> "Y",
      "Age" -> 50,
      "Address" -> "Y",
      "Email" -> "Y",
      "Gender" -> "Y",
      "Gender" -> 1001,
      "Count" -> 1001,
      "AverageMonthlyExpenditures" -> 3200,
      "AccountNumber" -> 26)

    val expr =
      """ (Flag=='Y' OR Id=='N' OR Address=='Y' OR Email=='Y' OR Gender=='N')
        | AND
        | (Flag=='N' OR Id=='N' OR Address=='N' OR Email=='N' OR Gender=='N')
        | AND
        | (Flag=='N' OR Id=='N' OR Address=='Y' OR Email=='Y' OR Gender=='N')
        | AND
        | (Flag=='Y' OR Id=='Y' OR Address=='N' OR Email=='Y' OR Gender=='Y')
        | AND
        | Age>18 AND Age<119 AND Count>1000 AND AverageMonthlyExpenditures>3100""".stripMargin.replace("\n", "").replace("\r", "").trim

    val r  = Parsing.ComplexExprs.E.parse(expr).get.value
    r.evaluate(cust) shouldBe Right(True)
  }

  test("Inverse actual criteria") {
    val cust = Map(
      "Flag" -> "N",
      "Id" -> "Y",
      "Age" -> 50,
      "Address" -> "Y",
      "Email" -> "Y",
      "Gender" -> "Y",
      "Count" -> 1001,
      "AverageMonthlyExpenditures" -> 3200,
      "AccountNumber" -> 26)

    val expr =
      """ (Flag=='Y' AND Id=='N' AND Address=='Y' AND Email=='Y' AND Gender=='N')
        | OR
        | (Flag=='N' AND Id=='N' AND Address=='N' AND Email=='N' AND Gender=='N')
        | OR
        | (Flag=='N' AND Id=='N' AND Address=='Y' AND Email=='Y' AND Gender=='N')
        | OR
        | (Flag=='Y' AND Id=='Y' AND Address=='N' AND Email=='Y' AND Gender=='Y')
        | AND
        | Age>18 AND Age<119 AND Count > 1000 AND AverageMonthlyExpenditures>3100""".stripMargin.replace("\n", "").replace("\r", "").trim

    val parsed = Parsing.ComplexExprs.E.parse(expr).get.value
    val evaluated = parsed.evaluate(cust)
    assert(evaluated.isRight)
    evaluated.foreach(result => assert(result.isInstanceOf[False]))
  }

}
