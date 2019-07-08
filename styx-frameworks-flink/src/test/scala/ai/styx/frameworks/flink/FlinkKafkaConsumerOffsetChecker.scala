package ai.styx.frameworks.flink

import ai.styx.common.Logging
import org.apache.flink.test.util.TestBaseUtils
import org.json4s.JsonAST
import org.json4s.JsonAST.{JArray, JString, JValue}
import org.json4s.jackson.JsonMethods._

class FlinkKafkaConsumerOffsetChecker(flinkWebAddress: String) extends Logging {

  def isInitializedForJob(jobId: String, cepName: String, topic: String, partition: Int = 0): Boolean = {
    val verticesUrl = s"http://$flinkWebAddress/jobs/$jobId/vertices" // vertices are simply parts of the job that are not chained together, e.g. separated by keyBy operation
    val jsonResponse: JValue = getJsonFromUrl(verticesUrl)
    val jobVertices = extractVertices(jsonResponse)
    val vertexId: String = getKafkaConsumerJobVertexId(jobVertices)

    val metricName = currentOffsetMetricName(cepName, topic, partition)

    metricsUrlAvailable(jobId, vertexId, metricName) &&
      isKafkaConsumerOffsetInitialized(jobId, vertexId, metricName)
  }

  def metricsUrlAvailable(jobId: String, vertexId: String, metricName: String): Boolean = {
    val allMetricsUrl = s"http://$flinkWebAddress/jobs/$jobId/vertices/$vertexId/metrics"
    val allMetricsContent = TestBaseUtils.getFromHTTP(allMetricsUrl)
    allMetricsContent.contains(metricName)
  }

  def isKafkaConsumerOffsetInitialized(jobID: String, vertexId: String, metricName: String): Boolean = {
    val metricsUrl = s"http://$flinkWebAddress/jobs/$jobID/vertices/$vertexId/metrics?get=$metricName"
    val JString(offsetString) = getJsonFromUrl(metricsUrl) \\ "value"

    // when initialized then it is set to at least -1,
    // before that, the default starting value is org.apache.flink.streaming.connectors.kafka.internals.KafkaTopicPartitionState.OFFSET_NOT_SET
    offsetString.toLong > -2
  }

  def currentOffsetMetricName(cepName: String, topic: String, partition: Int): String = {
    val operatorName: String = s"Source: Source stream of $cepName from $topic"
    val cleanedOperatorName = operatorName.replaceAll(" ", "_").replaceAll(":", "_").take(80)
    s"1.$cleanedOperatorName.KafkaConsumer.current-offsets.$topic-$partition"
  }

  def getKafkaConsumerJobVertexId(vertices: List[Any]): String = {
    def namePredicate: String => Boolean = _.startsWith("Source: Source stream")

    val filteredVertices: List[Any] = filterVertices(vertices, namePredicate)
    val vertexId = getId(filteredVertices)
    vertexId
  }

  def getId(vertex: List[Any]): String = {
    val vertexIdOpt: Option[AnyRef] = vertex head match {
      case map: Map[String, AnyRef]@unchecked => map.get("id") // don't use unchecked in prod code, here this is only test, so it's fine
      case _ => None
    }
    val vertexId = vertexIdOpt.get
    vertexId.toString
  }

  def filterVertices(vertices: List[Any], namePredicate: String => Boolean): List[Any] = {
    vertices.filter(jobVertexWithName(namePredicate))
  }

  def extractVertices(jsonResponse: JValue): List[Any] = {
    val vertices = jsonResponse \\ "vertices"
    (vertices \\ classOf[JArray]).head
  }

  def getJsonFromUrl(url: String): JsonAST.JValue = {
    val response = TestBaseUtils.getFromHTTP(url)
    //    logger.trace(s"Received response from Flink web: ${if (response.isEmpty) "<empty>" else response}")
    val parsed: JValue = parse(response)
    parsed
  }

  def jobVertexWithName(namePredicate: String => Boolean)(vertex: Any): Boolean = vertex match {
    case map: Map[String, AnyRef]@unchecked => map.get("name").exists { // don't use unchecked in prod code, here this is only test, so it's fine
      case vertexName: String => namePredicate(vertexName)
    }
    case _ => false
  }
}
