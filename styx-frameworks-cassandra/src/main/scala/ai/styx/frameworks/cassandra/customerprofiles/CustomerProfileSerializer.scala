package ai.styx.frameworks.cassandra.customerprofiles

import ai.styx.domain.Customer

class CustomerProfileSerializer {

  val customerFields = List(
    "Flag",
    "Id",
    "Age",
    "Address",
    "Email",
    "Gender",
    "Count",
    "AverageMonthlyExpenditures",
    "Segment",
    "ContactDate",
    "AccountNumber",
    "Rating",
    "CreditCard",
    "Budget"
  )

  val intColumnIds = Set(200)

  def toObjectList(customer: Customer): List[AnyRef] = {
    customerFields.map(customer.all(_)).zipWithIndex.map { case (value, index) =>
      if (intColumnIds.contains(index))
        value.toString.toInt.asInstanceOf[Object]
      else
        value
    }
  }

  def fromStringList(values: List[String]): Customer = {
    Customer(
      Flag = values(18),
      Id = values(21),
      Age = values(37).toInt,
      Address = values(38),
      Email = values(43),
      Gender = values(45),
      Count = values(114).toInt,
      AverageMonthlyExpenditures = values(153).toInt,
      AccountNumber = values(200).toInt,
      all = customerFields.zipWithIndex.map{ case (name, index)=>name->values(index)}.toMap,
      Segment = values(159),
      ContactDate = values(186),
      Rating = values(182),
      CreditCard = values(98),
      Budget = values(96)
    )
  }
}
