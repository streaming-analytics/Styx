package com.styx.common

import java.time.Duration

case class InstanceGraphiteConfig(host: String, port: Int, environment: String, root: String, instance: String, pollingPeriod: Duration)

