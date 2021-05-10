package ai.styx.domain.events

import ai.styx.common.Logging
import ai.styx.common.StringHelper.parseOption

// TODO: Avro

case class Click (
                   raw_timestamp: String,
                   raw_user_id: Option[String] = None,
                   raw_url: String, // PDP: https://my_webshop.nl/en-US/phones/81919/, cart: https://my_webshop.nl/en-US/cart/
                   raw_ip: String,
                   raw_timezone: String,
                   raw_country: String,
                   raw_user_agent: String, // Mozilla/5.0 (Android 4.4; Tablet; rv:41.0) Gecko/41.0 Firefox/41.0
                   rich_city: Option[String] = None, // to be filled in; IP2Geo / geopip2
                   rich_latitude: Option[String] = None,  // to be filled in
                   rich_longitude: Option[String] = None,// to be filled in
                   rich_device: Option[String] = None,// to be filled in
                   rich_os_family: Option[String] = None, // to be filled in
                   rich_session_id: Option[String] = None, // to be filled in
                 ) {
  def category: Option[String] = {
    // /<language>/<category>/<remainder of the path>
    if (raw_url == null) None else {
      val parts = raw_url.split("/")
      if (parts.length < 3) None else Some(parts(2))
    }
  }

  override def toString: String = {
    s"${raw_timestamp};${raw_user_id};${raw_url};${raw_ip};${raw_timezone};${raw_country};${raw_user_agent};${rich_city};${rich_latitude};${rich_longitude};${rich_device};${rich_os_family};${rich_session_id}"
  }
}

object Click extends Logging {
  def fromString(line: String): Option[Click] = {
    try {
      val data = line.split(";")
      Some(Click(data(0), parseOption(data(1)), data(2), data(3), data(4), data(5), data(6), parseOption(data(7)), parseOption(data(8)), parseOption(data(9)), parseOption(data(10)), parseOption(data(11)), parseOption(data(12))))
    }
    catch {
      case _: Throwable =>
        LOG.error(s"Cannot parse click {$line}")
        None
    }
  }
}
