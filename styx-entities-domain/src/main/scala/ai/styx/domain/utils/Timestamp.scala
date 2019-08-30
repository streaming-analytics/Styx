package ai.styx.domain.utils

//import java.time.format.DateTimeFormatter
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object Timestamp {
  val TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  def stamp() : DateTime = DateTime.now  // TIMESTAMP_PATTERN

  def decodeFromString(dateString: String) : DateTime = DateTime.parse(dateString, DateTimeFormat.forPattern(TIMESTAMP_PATTERN))
}
