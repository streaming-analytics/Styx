package ai.styx.common

object StringHelper {
  def toInt(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case e: Exception => None
    }
  }

  def parseOption(s: String): Option[String] = {
    if (s == "None") None else Some(s)
  }
}
