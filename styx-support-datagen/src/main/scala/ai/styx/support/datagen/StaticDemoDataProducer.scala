package ai.styx.support.datagen

import java.util
import java.util.{Calendar, UUID}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.collection.JavaConverters._
import scala.util.Random

class StaticDemoDataProducer {
  val random = Random

  @volatile var running = true
  var i = 0L
  val maxSingleTransaction = 450

  def cancel() {
    running = false
  }

  def createData(): Iterator[util.Map[String, AnyRef]] = {
    Iterator.continually
    {
      i += 1
      val accnum = if (i % 2 == 0) "1661819" else "2343159"
      val cardid = if (i % 2 == 0) "3945919" else "402875"
      val columnHeader = Array("TRS_TIME", "CARD_ID", "TRS_AMOUNT", "CARD_TYPE", "ACC_NUM", "TIMESTAMPS", "trace_id")
      val amount = Math.floor(100 * Math.abs((random.nextInt % maxSingleTransaction) + random.nextDouble)) / 100
      val eventTime = DateTime.now()
      val dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
      val line = eventTime.toString(dtf) + "," + cardid + "," + amount + ",trs," + accnum + "," + Calendar.getInstance().getTimeInMillis.toString + "," + UUID.randomUUID().toString
      val columns = line.split(",")
      val dataToSend = columnHeader.zip(columns).toMap[String, AnyRef].asJava

      Thread.sleep(1000)
      println("Message: " + i)
      dataToSend
    }.takeWhile(_=>running)
  }

}
