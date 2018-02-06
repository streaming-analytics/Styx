package com.styx.dsl

import cats.implicits._
import fastparse.all._

import scala.reflect.ClassTag

object UsefulImplicits {

  implicit def resultToBoolean(result: Result): Boolean = result match {
    case True => true
    case False(_) => false
  }

  implicit def toDataSource(m: Map[String, Any]): DataSource =
    DataSource(m)

}

sealed trait Result {
  def &&(other: Result): Result
  def ||(other: Result): Result
}

case object True extends Result {

  override def &&(other: Result): Result = other

  override def ||(other: Result): Result = this
}

case class False(reasons: Seq[String]) extends Result {

  override def &&(other: Result): Result = other match {
    case True => this
    case False(theirReasons: Seq[String]) => False(reasons ++ theirReasons)
  }

  override def ||(other: Result): Result = other match {
    case True => True
    case False(_) => False(reasons) // discard their reasons
  }

}

object False {
  def apply(why: String): False = new False(Seq(why))
}

trait Expression {
  def evaluate(dataSource: DataSource): Either[String, Result]
}

case class DataSource(input: Map[String, Any]) {

  private val map = input.map {
    case (key: String, value: Any) => key.toLowerCase -> value
  }

  def fetch[T](key: String)(implicit tag: ClassTag[T]): Either[String, T] = {
    map.get(key.toLowerCase) match {
      case Some(v: T) => Right(v)
      case Some(v) => Left(s"value provided for $key is not the right type")
      case None => Left(s"the $key is not in the map")
    }
  }
}

case class IntComparisonExpression(identifier: String, op: String, valueToCompareAgainst: Int) extends Expression {
  override def evaluate(dataSource: DataSource): Either[String, Result] = {

    dataSource.fetch[Int](identifier).flatMap {
      (extractedValue: Int) => {
        op match {

          case "==" => Right {
            if (valueToCompareAgainst == extractedValue)
              True
            else
              False(s"$identifier (equal to $extractedValue) does not equal $valueToCompareAgainst")
          }

          case "<" => Right {
            if (extractedValue < valueToCompareAgainst)
              True
            else
              False(s"$identifier (equal to $extractedValue) is not less than $valueToCompareAgainst")
          }

          case ">" => Right {
            if (extractedValue > valueToCompareAgainst)
              True
            else
              False(s"$identifier (equal to $extractedValue) is not greater than $valueToCompareAgainst")
          }

          case ">=" => Right {
            if (extractedValue >= valueToCompareAgainst)
              True
            else
              False(s"$identifier (equal to $extractedValue) is not greater than or equal to $valueToCompareAgainst")
          }

          case "<=" => Right {
            if (extractedValue <= valueToCompareAgainst)
              True
            else
              False(s"$identifier (equal to $extractedValue) is not less than or equal to $valueToCompareAgainst")
          }

          case "!=" => Right {
            if (extractedValue != valueToCompareAgainst)
              True
            else
              False(s"$identifier (equal to $extractedValue) found to be equal to $valueToCompareAgainst")
          }

          case someOp => Left(s"Ints can't be compared with '$someOp'")
        }
      }
    }
  }
}

case class StringComparisonExpression(identifier: String, op: String, value: String) extends Expression {
  override def evaluate(dataSource: DataSource): Either[String, Result] = {
    val v: Either[String, String] = dataSource.fetch[String](identifier)
    v.flatMap {
      (g: String) => {
        op match {
          case "==" => Right {
            if (value == g)
              True
            else
              False(s"$identifier (equal to $g) is not equal to $value")
          }
          case "!="   => Right {
            if (value != g)
              True
            else
              False(s"$identifier (equal to $g) found to be equal to $value")
          }
          case someOp => Left(s"Strings can't be compared with '$someOp'")
        }
      }
    }
  }
}

case class ComplexExpression(expression1: Expression, op: String, expression2: Expression) extends Expression {
  override def evaluate(dataSource: DataSource): Either[String, Result] = {
    op.toLowerCase match {
      case "and" =>
        for {
          p <- expression1.evaluate(dataSource)
          q <- expression2.evaluate(dataSource)
        } yield p && q
      case "or" =>
        for {
          p <- expression1.evaluate(dataSource)
          q <- expression2.evaluate(dataSource)
        } yield p || q
      case someOtherOp =>
        Left(s"$someOtherOp does not apply to expressions")
    }
  }
}

object Parsing {

  import Values._

  val (lowerLetters, upperLetters, digits, someChars, moreChars) =
    ('a' to 'z', 'A' to 'Z', '0' to '9', Seq('_', '-'), Seq(' ', '*', '`', '!', '$', '&', '?'))

  val Identifier: Parser[String] =
    P(CharIn(lowerLetters, upperLetters, digits, someChars).rep(1).!).map(_.toLowerCase)

  val OptSpaces: Parser[Unit] = P(" ".rep(0))

  object Values {

    val StrContent: Parser[String] = P(CharIn(lowerLetters, upperLetters, digits, someChars, moreChars).rep(0).!)

    val Str: Parser[String] = P("'" ~ StrContent ~ "'")

    val Int: Parser[Int] = P(CharIn('0' to '9').rep(1).!.map(_.toInt))

  }

  object SimpleExprOps {

    val CompareOp: Parser[String] = P(OptSpaces ~ ("==".! | "!=".! | "<".! | ">".! | "<=".! | ">=".!) ~ OptSpaces)

  }

  object SimpleExprs {

    import SimpleExprOps._

    val IntCompareExpr: Parser[IntComparisonExpression] = P(Identifier ~ CompareOp ~ Int).map {
      case (identifier: String, compareOp: String, int: Int) =>
        IntComparisonExpression(identifier, compareOp, int)
    }

    val StringCompareExpr: Parser[StringComparisonExpression] =
      P(Identifier ~ CompareOp ~ Str).map {
        case (identifier: String, compareOp: String, str: String) =>
          StringComparisonExpression(identifier, compareOp, str)
      }

    val SimpleExpr: Parser[Expression] = P(IntCompareExpr | StringCompareExpr)

  }

  object ComplexExprsOps {

    val Or: Parser[Unit] = P(OptSpaces ~ IgnoreCase("OR") ~ OptSpaces)
    val And: Parser[Unit] = P(OptSpaces ~ IgnoreCase("AND") ~ OptSpaces)

  }

  object ComplexExprs {
    import ComplexExprsOps._
    import SimpleExprs._

    val parens:Parser[Expression] = P("(" ~ OptSpaces ~ q ~ OptSpaces ~ ")")

    val f: Parser[Expression] = P( SimpleExpr | parens)

    val p: Parser[Expression] = P(f ~ (And ~ f).rep()).map {
      (s: (Expression, Seq[Expression])) => s._2.fold(s._1)((e1, e2) => ComplexExpression(e1, "and", e2))
    }
    val q: Parser[Expression] = P(p ~ (Or ~ p).rep()).map {
      s => s._2.fold(s._1)((e1, e2) => ComplexExpression(e1, "or", e2))
    }
    val E: Parser[Expression] = P(q ~ End)

  }
}

