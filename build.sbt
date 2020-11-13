import sbt.Keys._

val slf4jV = "1.7.30" // Our logging framework
val logbackV = "1.2.3" // Our logging implementation
val jodatimeV = "2.10.5"
val jodaConvertV = "2.2.1"
val typesafeV = "1.4.0"
val jacksonV = "3.6.7"
val scalatestV = "3.1.1"
val circeV = "0.11.1"
val flinkV = "1.10.0"
val flinkKafkaV = "0.11"
val cassandraV = "3.11.6"
val cassandraDriverV = "4.0.0"
val cassandraUnitV = "4.3.1.0"
val kafkaV = "2.5.0"
val sparkV = "2.4.5"
val sparkKafkaV = "0-10"
val embeddedKafkaV = "1.0.0"
val codehaleMetricsV = "3.0.2"
val jpmmlV = "1.4.11"
val igniteV = "2.8.0"
val ow2V = "5.0.3"

lazy val commonSettings = Seq(
  organization := "github.com/streaming-analytics",
  version := "0.1",
  scalaVersion := "2.13.3",
  sbtVersion := "1.4.2",
  crossPaths := false,
  scalacOptions := Seq(
    "-encoding", "utf8",
    "-feature",
    "-unchecked",
    "-deprecation",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:reflectiveCalls"
  ),
  libraryDependencies ++= commonDependencies
)

lazy val commonDependencies = Seq(
  "com.typesafe" % "config" % typesafeV,
  "org.slf4j" % "slf4j-api" % slf4jV,
  "org.slf4j" % "log4j-over-slf4j" % slf4jV,
  "org.slf4j" % "jcl-over-slf4j" % slf4jV,
  "ch.qos.logback" % "logback-classic" % logbackV,
  "ch.qos.logback" % "logback-core" % logbackV,
  "org.scalactic" %% "scalactic" % scalatestV,
  "org.scalatest" %% "scalatest" % scalatestV % Test,
  "org.joda" % "joda-convert" % jodaConvertV, // to prevent warnings like class org.joda.convert.FromString not found - continuing with a stub.
  "joda-time" % "joda-time" % jodatimeV,
  "org.json4s" %% "json4s-jackson" % jacksonV % "test"
)

lazy val cassandraDependencies = Seq(
  "com.typesafe" % "config" % typesafeV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"),
  "com.datastax.cassandra" % "cassandra-driver-core" % cassandraDriverV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"),
  "org.apache.cassandra" % "cassandra-all" % cassandraV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"),
  "io.reactivex" % "rxjava" % "1.1.6" exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"),
  "org.cassandraunit" % "cassandra-unit" % cassandraUnitV % "test" exclude("org.apache.cassandra", "cassandra-all"),
  "com.codahale.metrics" % "metrics-core" % codehaleMetricsV % "test"
)

lazy val igniteDependencies = Seq(
  "org.apache.ignite" % "ignite-core" % igniteV,
  "org.apache.ignite" % "ignite-indexing" % igniteV
)

lazy val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-scala" % flinkV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"),
  "org.apache.flink" %% "flink-streaming-scala" % flinkV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"),
  "org.apache.flink" %% "flink-clients" % flinkV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"),
  "org.apache.flink" % "flink-metrics-dropwizard" % flinkV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"),
  "org.apache.flink" %% "flink-test-utils" % flinkV % Test exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12")
)

lazy val sparkDependencies = Seq (
  "org.apache.spark" %% "spark-core" % sparkV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"),
  "org.apache.spark" %% "spark-sql" % sparkV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"),
  "org.apache.spark" %% "spark-mllib" % sparkV % "runtime" exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"),
  "org.apache.spark" %% "spark-streaming" % sparkV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12")  // % "provided"
)

lazy val kafkaDependencies = Seq(
  "org.apache.kafka" %% "kafka" % kafkaV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"),
  "org.apache.kafka" % "kafka-clients" % kafkaV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"),
  "net.manub" %% "scalatest-embedded-kafka" % embeddedKafkaV % "test",
  "org.apache.flink" %% ("flink-connector-kafka-" + flinkKafkaV) % flinkV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"),
  "org.apache.spark" %% ("spark-sql-kafka-" + sparkKafkaV) % sparkV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12")
)


lazy val overrideDependencies = Set(
  "joda-time" % "joda-time" % jodatimeV,
   "org.ow2.asm" % "asm-tree" % ow2V,
   "org.ow2.asm" % "asm-commons" % ow2V,
  "org.slf4j" % "slf4j-api" % slf4jV,
  "org.slf4j" % "log4j-over-slf4j" % slf4jV,
  "org.slf4j" % "jcl-over-slf5j" % slf4jV,
  "org.apache.kafka" %% "kafka" % kafkaV,
  "org.apache.kafka" %% "kafka-clients" % kafkaV,
  "org.scalatest" %% "scalatest" % scalatestV
)

lazy val styxCommon = (project in file("styx-common"))
  .settings(
  commonSettings,
  name := "styx-common",
  libraryDependencies ++= commonDependencies)

lazy val styxEntitiesDomain = (project in file("styx-entities-domain"))
  .dependsOn(styxCommon)
  .settings(
    commonSettings,
    name := "styx-entities-domain",
    libraryDependencies ++= commonDependencies ++ flinkDependencies)

lazy val styxFrameworksInterfaces = (project in file("styx-frameworks-interfaces"))
  .dependsOn(styxEntitiesDomain)
  .settings(
    commonSettings,
    name := "styx-frameworks-interfaces")

lazy val styxUseCasesInterfaces = (project in file("styx-usecases-interfaces"))
  .dependsOn(styxFrameworksInterfaces)
  .settings(
    commonSettings,
    name := "styx-usecases-interfaces"
  )

lazy val styxFrameworksCassandra = (project in file("styx-frameworks-cassandra"))
  .dependsOn(styxFrameworksInterfaces)
  .settings(
    name := "styx-frameworks-cassandra",
    libraryDependencies ++= commonDependencies ++ cassandraDependencies)

lazy val styxFrameworksIgnite = (project in file("styx-frameworks-ignite"))
  .dependsOn(styxFrameworksInterfaces)
  .settings(
    name := "styx-frameworks-ignite",
    libraryDependencies ++= commonDependencies ++ igniteDependencies)

lazy val styxFrameworksFlink = (project in file("styx-frameworks-flink"))
  .dependsOn(styxFrameworksInterfaces)
  .settings(
  commonSettings,
  name := "styx-frameworks-flink",
  libraryDependencies ++= flinkDependencies)
// TODO: remove spark, move to new module

lazy val styxFrameworksSpark = (project in file("styx-frameworks-spark"))
  .dependsOn(styxFrameworksInterfaces)
  .settings(
    commonSettings,
    name := "styx-frameworks-spark",
    libraryDependencies ++= sparkDependencies)

lazy val styxFrameworksKafka = (project in file("styx-frameworks-kafka"))
  .dependsOn(styxFrameworksInterfaces, styxFrameworksFlink)
  .settings(
    commonSettings,
    name := "styx-frameworks-kafka",
    libraryDependencies ++= kafkaDependencies)

lazy val styxFrameworksOpenscoring = (project in file("styx-frameworks-openscoring"))
  .dependsOn(styxFrameworksInterfaces)
  .settings(
    commonSettings,
    name := "styx-frameworks-openscoring",
    libraryDependencies ++= 
      Seq("org.jpmml" % "pmml-evaluator" % jpmmlV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"))
    )

lazy val styxUseCasesShopping = (project in file("styx-usecases-shopping"))
  .dependsOn(styxUseCasesInterfaces, styxFrameworksFlink)
  .settings(
    commonSettings,
    name := "styx-usecases-shopping"
  )

lazy val styxUseCasesClickStream = (project in file("styx-usecases-clickstream"))
  .dependsOn(styxUseCasesInterfaces, styxFrameworksFlink)
  .settings(
    commonSettings,
    name := "styx-usecases-clickstream"
  )

lazy val styxUseCasesTwitterSentiment = (project in file("styx-usecases-twitter-sentiment"))
  .dependsOn(styxUseCasesInterfaces, styxFrameworksFlink)
  .settings(
    commonSettings,
    name := "styx-usecases-twitter-sentiment"
  )

lazy val styxUseCasesFraud = (project in file("styx-usecases-fraud"))
  .dependsOn(styxUseCasesInterfaces, styxFrameworksFlink)
  .settings(
    commonSettings,
    name := "styx-usecases-fraud"
  )

lazy val styxAppFlinkPipeline = (project in file("styx-app-flink-pipeline"))
  .dependsOn(styxUseCasesShopping, styxUseCasesClickStream, styxUseCasesTwitterSentiment, styxUseCasesFraud, styxFrameworksKafka)
  .settings(
    commonSettings,
    name := "styx-app-flink-pipeline")

lazy val styxAppSparkPipeline = (project in file("styx-app-spark-pipeline"))
  .dependsOn(styxUseCasesShopping, styxUseCasesClickStream, styxUseCasesTwitterSentiment, styxUseCasesFraud, styxFrameworksSpark, styxFrameworksKafka, styxFrameworksIgnite)
  .settings(
    commonSettings,
    name := "styx-app-spark-pipeline")

lazy val styxAppDemo = (project in file("styx-app-demo"))
  .dependsOn(styxEntitiesDomain, styxFrameworksKafka)
  .settings(
    commonSettings,
    name := "styx-app-demo")

lazy val root = (project in file("."))
  .aggregate(
    styxAppDemo,
    styxAppFlinkPipeline,
    styxAppSparkPipeline,
    styxUseCasesInterfaces,
    styxUseCasesShopping,
    styxUseCasesTwitterSentiment,
    styxUseCasesClickStream,
    styxUseCasesFraud,
    styxFrameworksCassandra,
    styxFrameworksIgnite,
    styxFrameworksFlink,
    styxFrameworksSpark,
    styxFrameworksKafka,
    styxFrameworksInterfaces,
    styxCommon,
    styxEntitiesDomain)
  .settings(
    commonSettings,
    Defaults.itSettings,
    name := "Styx"
  )

parallelExecution in Test := false
logBuffered in Test := false

coverageEnabled in Test := true
test in assembly := {}
coverageExcludedPackages := ""

assemblyJarName in assembly := "styx.jar"

assemblyMergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf")          => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$")      => MergeStrategy.discard
  case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
  case m if m.toLowerCase.endsWith(".dct")                 => MergeStrategy.first
  case m if m.toLowerCase.endsWith(".conf")                => MergeStrategy.concat
  case _                                                   => MergeStrategy.first
}

fork in run := true
