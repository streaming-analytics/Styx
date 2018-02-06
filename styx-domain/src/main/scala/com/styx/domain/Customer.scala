package com.styx.domain

case class Customer(
                     Flag: String,
                     Id: String,
                     Age: Int,
                     Address: String,
                     Email: String,
                     Gender: String,
                     Count: Int,
                     AverageMonthlyExpenditures: Int,
                     AccountNumber: Int,
                     all: Map[String, AnyRef],
                     Segment: String,
                     ContactDate: String,
                     Rating: String,
                     CreditCard: String,
                     Budget: String
                   ) {
  def getProperty(name: String): Any = {
    name match {
      case "AccountNumber" => AccountNumber
      case "Age" => Age
      case "Flag" => Flag
      case "Id" => Id
      case "Address" => Address
      case "Email" => Email
      case "Gender" => Gender
      case "Count" => Count
      case "AverageMonthlyExpenditures" => AverageMonthlyExpenditures
      case _ => throw new Exception(s"Invalid property of Customer: $name")
    }
  }
}

object Customer {
  implicit def customerToMap(cust: Customer): Map[String, Any] = {
    val fieldValues = cust.productIterator

    // This will contain "extra" fields, like for instance $outer (the outer class, if any)
    val fields = cust.getClass.getFields.map(_.getName)

    // Keep only internal fields
    val innerFields = cust.getClass.getDeclaredFields.map(_.getName) diff fields

    innerFields
      .map(_ -> fieldValues.next())
      .toMap
  }
}