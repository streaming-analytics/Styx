package com.styx.common

import java.io.File
import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions, ConfigValueType}

import scala.collection.JavaConverters._

object ConfigUtils extends Logging {

  def propertiesFromMap(properties: Map[String, String]): Properties =
    (new Properties /: properties) {
      case (a, (k, v)) =>
        a.put(k, v)
        a
    }

  def propertiesFromConfig(config: Config): Properties = {
    propertiesFromMap(config.entrySet().asScala.map(entry => {
      val value = entry.getValue
      val valueStr = value.valueType() match {
        case ConfigValueType.OBJECT => value.unwrapped().toString
        case ConfigValueType.LIST => value.unwrapped().toString.stripPrefix("[").stripSuffix("]")
        case ConfigValueType.NUMBER => value.unwrapped().toString
        case ConfigValueType.BOOLEAN => value.unwrapped().toString
        case ConfigValueType.NULL => value.unwrapped().toString
        case ConfigValueType.STRING => value.unwrapped().toString
      }
      entry.getKey -> valueStr
    }).toMap)
  }

  def loadConfig(args: Array[String]): Config = loadConfig(parseReferenceFilenameFrom(args), Map.empty)

  def parseReferenceFilenameFrom(args: Array[String]): Option[String] = args.zipWithIndex.find(_._1 == "--config").map(label_idx => args(label_idx._2 + 1))

  def readConfigFromFile(filenameOpt: Option[String]): Config = {
    filenameOpt match{
      case None => ConfigFactory.empty()
      case Some(filename) => ConfigFactory.parseFile(new File(filename))
    }
  }

  def loadConfig(referenceConfigFilename: Option[String] = None, overrides: Map[String, _]): Config = {
    val referenceConfig = readConfigFromFile(referenceConfigFilename)
    val config = ConfigFactory.parseMap(overrides.asJava)
      .withFallback(referenceConfig)
      // should be handled by typesafe oonfig itself based on -Dkey=val environment settings: .withFallback(ConfigFactory.parseProperties(ParameterTool.fromArgs(args).getProperties))
      .withFallback(ConfigFactory.load())
      .getConfig("styx")
    logger.info(s"Finished loading config from application.conf and from file ${referenceConfigFilename.getOrElse("(no filename provided)")} and with overrides: $overrides")
    logger.info(s"MERGED, FINAL config values:\n${config.root().render(ConfigRenderOptions.defaults().setOriginComments(false))}")
    config
  }

}
