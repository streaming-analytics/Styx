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
                   rich_page_type: Option[String] = None,  // home, search, pdp, cart -> to be filled in (derive from url)
                   rich_product_category: Option[String] = None,  // to be filled in (derive from url)
                   rich_city: Option[String] = None, // to be filled in (derive from ip; IP2Geo / geopip2)
                   rich_latitude: Option[String] = None,  // to be filled in
                   rich_longitude: Option[String] = None,// to be filled in
                   rich_device: Option[String] = None,// to be filled in (derive from user_agent)
                   rich_os_family: Option[String] = None, // to be filled in (derive from user_agent)
                   rich_session_id: Option[String] = None, // to be filled in (session algorithm)
                 ) {
  override def toString: String = {
    s"${raw_timestamp},${raw_user_id},${raw_url},${raw_ip},${raw_timezone},${raw_country},${raw_user_agent},${rich_page_type},${rich_product_category},${rich_city},${rich_latitude},${rich_longitude},${rich_device},${rich_os_family},${rich_session_id}"
  }
}

object Click extends Logging {
  def fromString(line: String): Option[Click] = {
    try {
      val data = line.split(",")
      Some(Click(data(0), parseOption(data(1)), data(2), data(3), data(4), data(5), data(6), parseOption(data(7)), parseOption(data(8)), parseOption(data(9)), parseOption(data(10)), parseOption(data(11)), parseOption(data(12)), parseOption(data(13)), parseOption(data(14))))
    }
    catch {
      case _: Throwable =>
        LOG.error(s"Cannot parse click {$line}")
        None
    }
  }
}

object ClickDataEnricher {
  def getPageType(click: Click): Option[String] = {
    // <main>/<language>/<type>/<category>/<remainder of the path>
    if (click.raw_url == null || click.raw_url.isBlank) None else {
      val parts = click.raw_url.replace("https://", "").split("/")
      if (parts.length < 3) None else Some(parts(2))
    }
  }

  def getProductCategory(click: Click): Option[String] = {
    // <main>/<language>/<type>/<category>/<remainder of the path>
    if ( click.raw_url == null || click.raw_url.isBlank) None else {
      val parts = click.raw_url.replace("https://", "").split("/")
      if (parts.length < 4) None else Some(parts(3))
    }
  }
}