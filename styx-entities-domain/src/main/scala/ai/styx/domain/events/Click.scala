package ai.styx.domain.events

// "collector_tstamp","domain_sessionid","domain_userid","page_urlpath","os_timezone","geo_timezone","geo_country","geo_region","geo_city","geo_longitude","geo_latitude","geo_region_name","dvce_type","os_family"
// "2017-07-04 13:41:40.959","b1052a22-06de-4e5e-9566-2cd9aa74ec12","5aeb1bb8-9ada-419a-9191-391dafc7de1d","/nl/","Europe/Berlin","Europe/Amsterdam","NL","","","4.8995056","52.3824","","Computer","Windows"
case class Click (
                   // TODO: change to proper types, e.g. Date
                   // TODO: remove unneeded fields to gain performance and robustness
                   collector_tstamp: String = "",
                   domain_sessionid: String = null,
                   domain_userid: String = null,
                   page_urlpath: String = null,
                   os_timezone: String = null,
                   geo_timezone: String = null,
                   geo_country: String = null,
                   geo_region: String = null,
                   geo_city: String = null,
                   geo_longitude: String = null,
                   geo_latitude: String = null,
                   geo_region_name: String = null,
                   dvce_type: String = null,
                   os_family: String = null
                 ) {
  def category: Option[String] = {
    // /<language>/<category>/<remainder of the path>
    if (page_urlpath == null) None else {
      val parts = page_urlpath.split("/")
      if (parts.length < 3) None else Some(parts(2))
    }
  }
}

object Click {
  def fromString(line: String): Click = {
    // TODO: implement a proper csv parser
    val data = line.replace("\"", "").split(",")
    Click(data(0), data(1), data(2), data(3), data(4), data(5), data(6), data(7), data(8),data(9), data(10), data(11), data(12), data(12))
  }
}
