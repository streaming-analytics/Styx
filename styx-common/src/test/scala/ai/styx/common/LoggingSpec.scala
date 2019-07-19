package ai.styx.common

class LoggingSpec extends BaseSpec {
  "Logging framework" should "log debug, info, warning, and error messages" in {
    LOG.debug("Test debug message")
    LOG.info("Test info message")
    LOG.warn("Test warning message")
    LOG.error("Test error message")
  }
}
