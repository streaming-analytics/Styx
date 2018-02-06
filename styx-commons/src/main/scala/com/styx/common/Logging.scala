package com.styx.common

import org.slf4j.{Logger, LoggerFactory, Marker}

/**
  * Specific trait which adds the slf4j scala logging, but in a serializable way to prevent
  * the "java.io.NotSerializableException: org.slf4j.helpers.SubstituteLogger" during a Flink run (sometimes).
  * If still not working, we should go back to Grizzled.
  */
trait Logging {
  // The logger. Instantiated the first time it's used. It is made transient to make this trait serializable;
  // see: http://fdahms.com/2015/10/14/scala-and-the-transient-lazy-val-pattern/
  @transient private lazy val _logger = LoggerFactory.getLogger(getClass)

  /** Get the `Logger` for the class that mixes this trait in. The `Logger`
    * is created the first time this method is call. The other methods (e.g.,
    * `error`, `info`, etc.) call this method to get the logger.
    *
    * @return the `Logger`
    */
  protected def logger: Logger = _logger

  /** Get the name associated with this logger.
    *
    * @return the name.
    */
  protected def loggerName: String = logger.getName

  /** Determine whether trace logging is enabled.
    */
  protected def isTraceEnabled: Boolean = logger.isTraceEnabled

  /** Issue a trace logging message.
    *
    * @param msg the message object. `toString()` is called to convert it
    *            to a loggable string.
    */
  protected def trace(msg: => String): Unit = logger.trace(msg) //.trace(msg)

  /** Issue a trace logging message, with an exception.
    *
    * @param msg the message object. `toString()` is called to convert it
    *            to a loggable string.
    * @param t   the exception to include with the logged message.
    */
  protected def trace(msg: => String, t: => Throwable): Unit = logger.trace(msg, t)

  /** Issue a trace logging message, with a marker and an exception.
    *
    * @param mkr the slf4j marker object.
    * @param msg the message object. `toString()` is called to convert it
    *            to a loggable string.
    * @param t   the exception to include with the logged message.
    */
  protected def trace(mkr: Marker, msg: => String, t: => Throwable): Unit = logger.trace(mkr, msg, t)

  /** Determine whether debug logging is enabled.
    */
  protected def isDebugEnabled: Boolean = logger.isDebugEnabled

  /** Issue a debug logging message.
    *
    * @param msg the message object. `toString()` is called to convert it
    *            to a loggable string.
    */
  protected def debug(msg: => String): Unit = logger.debug(msg)

  /** Issue a debug logging message, with an exception.
    *
    * @param msg the message object. `toString()` is called to convert it
    *            to a loggable string.
    * @param t   the exception to include with the logged message.
    */
  protected def debug(msg: => String, t: => Throwable): Unit = logger.debug(msg, t)

  /** Issue a debug logging message, with a marker and an exception.
    *
    * @param mkr the slf4j marker object.
    * @param msg the message object. `toString()` is called to convert it
    *            to a loggable string.
    * @param t   the exception to include with the logged message.
    */
  protected def debug(mkr: Marker, msg: => String, t: => Throwable): Unit = logger.debug(mkr, msg, t)

  /** Determine whether trace logging is enabled.
    */
  protected def isErrorEnabled: Boolean = logger.isErrorEnabled

  /** Issue a trace logging message.
    *
    * @param msg the message object. `toString()` is called to convert it
    *            to a loggable string.
    */
  protected def error(msg: => String): Unit = logger.error(msg)

  /** Issue a trace logging message, with an exception.
    *
    * @param msg the message object. `toString()` is called to convert it
    *            to a loggable string.
    * @param t   the exception to include with the logged message.
    */
  protected def error(msg: => String, t: => Throwable): Unit = logger.error(msg, t)

  /** Issue a error logging message, with a marker and an exception.
    *
    * @param mkr the slf4j marker object.
    * @param msg the message object. `toString()` is called to convert it
    *            to a loggable string.
    * @param t   the exception to include with the logged message.
    */
  protected def error(mkr: Marker, msg: => String, t: => Throwable): Unit = logger.error(mkr, msg, t)

  /** Determine whether trace logging is enabled.
    */
  protected def isInfoEnabled = logger.isInfoEnabled

  /** Issue a trace logging message.
    *
    * @param msg the message object. `toString()` is called to convert it
    *            to a loggable string.
    */
  protected def info(msg: => String): Unit = logger.info(msg)

  /** Issue a trace logging message, with an exception.
    *
    * @param msg the message object. `toString()` is called to convert it
    *            to a loggable string.
    * @param t   the exception to include with the logged message.
    */
  protected def info(msg: => String, t: => Throwable): Unit = logger.info(msg, t)

  /** Issue a info logging message, with a marker and an exception.
    *
    * @param mkr the slf4j marker object.
    * @param msg the message object. `toString()` is called to convert it
    *            to a loggable string.
    * @param t   the exception to include with the logged message.
    */
  protected def info(mkr: Marker, msg: => String, t: => Throwable): Unit = logger.info(mkr, msg, t)

  /** Determine whether trace logging is enabled.
    */
  protected def isWarnEnabled: Boolean = logger.isWarnEnabled

  /** Issue a trace logging message.
    *
    * @param msg the message object. `toString()` is called to convert it
    *            to a loggable string.
    */
  protected def warn(msg: => String): Unit = logger.warn(msg)

  /** Issue a trace logging message, with an exception.
    *
    * @param msg the message object. `toString()` is called to convert it
    *            to a loggable string.
    * @param t   the exception to include with the logged message.
    */
  protected def warn(msg: => String, t: => Throwable): Unit = logger.warn(msg, t)

  /** Issue a warn logging message, with a marker and an exception.
    *
    * @param mkr the slf4j marker object.
    * @param msg the message object. `toString()` is called to convert it
    *            to a loggable string.
    * @param t   the exception to include with the logged message.
    */
  protected def warn(mkr: Marker, msg: => String, t: => Throwable): Unit = logger.warn(mkr, msg, t)

}
