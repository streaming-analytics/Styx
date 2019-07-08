package ai.styx.support.datagen

import java.util
import java.util.{Calendar, UUID}

import scala.collection.JavaConverters._
import scala.io.Source

class RawEventGenerator(maxRows: Option[Int], filename: String) {
  def createData(): Iterator[util.Map[String, Object]] = {
    val stream = getClass.getResourceAsStream(filename)
    var j = -1
    val allLines = Source.fromInputStream(stream).getLines()
      val selectedLines = maxRows match {
        case Some(n)=> allLines.take(n)
        case _ => allLines
      }
    for (line <- selectedLines) yield {
      j += 1
      if (j % 1000 == 0) print(".")
      RawEventGenerator.parseRawEvent(line).mapValues(_.asInstanceOf[Object]).asJava
    }
  }
}

object RawEventGenerator {
  val columnHeader = Array("TRS_TIME", "CARD_ID", "TRS_AMOUNT", "CARD_TYPE", "ACC_NUM")
  val castToDouble: util.HashSet[String] = new util.HashSet[String]()
  // add columnHeaders which need to be double: castToDouble.add()

  def parseRawEvent(line: String): Map[String, String] = {
    val columns = line.split(",")
    val dataToSend = columnHeader.zip(columns).toMap

    dataToSend +
      ("TIMESTAMPS" -> s"GEN_LAST=${Calendar.getInstance().getTimeInMillis.toString}") + // add timestamp and trace id for chain / performance testing
      ("trace_id" -> UUID.randomUUID().toString)
  }
}
