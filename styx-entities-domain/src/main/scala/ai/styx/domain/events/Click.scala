package ai.styx.domain.events

import ai.styx.common.Logging

// TODO: Avro

// "collector_tstamp","domain_sessionid","domain_userid","page_urlpath","os_timezone","geo_timezone","geo_country","geo_region","geo_city","geo_longitude","geo_latitude","geo_region_name","dvce_type","os_family"
// "2017-07-04 13:41:40.959","b1052a22-06de-4e5e-9566-2cd9aa74ec12","5aeb1bb8-9ada-419a-9191-391dafc7de1d","/nl/","Europe/Berlin","Europe/Amsterdam","NL","","","4.8995056","52.3824","","Computer","Windows"
case class Click (
                   raw_timestamp: String = "",
                   raw_user_id: String = null,
                   raw_url: String = null, // PDP: https://my_webshop.nl/en-US/phones/81919/, cart: https://my_webshop.nl/en-US/cart/
                   raw_ip: String = null,
                   raw_timezone: String = null,
                   raw_country: String = null,
                   raw_user_agent: String = null, // Mozilla/5.0 (Android 4.4; Tablet; rv:41.0) Gecko/41.0 Firefox/41.0
                   rich_city: String = null, // to be filled in; IP2Geo / geopip2
                   rich_latitude: String = null,  // to be filled in
                   rich_longitude: String = null,// to be filled in
                   rich_device: String = null,// to be filled in
                   rich_os_family: String = null, // to be filled in
                   rich_session_id: String = null, // to be filled in
                 ) {
  def category: Option[String] = {
    // /<language>/<category>/<remainder of the path>
    if (raw_url == null) None else {
      val parts = raw_url.split("/")
      if (parts.length < 3) None else Some(parts(2))
    }
  }

  override def toString: String = {
    s"${raw_timestamp};${raw_user_id};${raw_url};${raw_ip};${raw_timezone};${raw_country};${rich_city};${rich_latitude};${rich_longitude};${rich_device};${rich_os_family};${rich_session_id}"
  }
}

object Click extends Logging {
  def fromString(line: String): Click = {
    // TODO: implement a proper csv parser
    try {
      val data = line.split(";")
      Click(data(0), data(1), data(2), data(3), data(4), data(5), data(6), data(7), data(8),data(9), data(10), data(11), data(12))
    }
    catch {
      case x: Exception =>
        LOG.error(s"Cannot parse click {$line}")
        null
    }
  }
}
