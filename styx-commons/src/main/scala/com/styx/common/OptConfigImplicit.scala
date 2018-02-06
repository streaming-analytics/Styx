package com.styx.common

import com.typesafe.config.Config

object OptConfigImplicit {

  implicit class OptConfig(val config: Config) extends AnyVal {

    /**
      * This will throw exception ConfigException.WrongType when the type is incorrect.
      *
      * @param path
      * @param func
      * @tparam T
      * @return
      */
    def opt[T](path: String, func: Config => String => T): Option[T] = {
      if (config.hasPath(path)) {
        Some(func(config)(path))
      } else {
        None
      }
    }

    def optConfig(path: String): Option[Config] = opt(path, _.getConfig)

    def optBoolean(path: String): Option[Boolean] = opt(path, _.getBoolean)

    def optString(path: String): Option[String] = opt(path, _.getString)

    def optInt(path: String): Option[Int] = opt(path, _.getInt)

    def optLong(path: String): Option[Long] = opt(path, _.getLong)
  }

}
