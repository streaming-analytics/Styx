package ai.styx.common

import org.slf4j.{Logger, LoggerFactory}

trait Logging {
  /** The logger. Instantiated the first time it's used. It is made transient to make this trait serializable;
    * see: http://fdahms.com/2015/10/14/scala-and-the-transient-lazy-val-pattern/
    */
  @transient private lazy val _logger = LoggerFactory.getLogger(getClass)

  /** Get the `Logger` for the class that mixes this trait in. The `Logger`
    * is created the first time this method is call. The other methods (e.g.,
    * `error`, `info`, etc.) call this method to get the logger.
    */
  protected def LOG: Logger = _logger
}
