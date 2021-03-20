package ai.styx.frameworks.openscoring

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import ai.styx.common.BaseSpec
import org.jpmml.evaluator.{ModelEvaluator, ModelEvaluatorFactory}

import scala.io.Source

class PmmlSpec extends BaseSpec {

  "PMML Model Instance" should "" in {
    // load data file
    val pmmlFilePath = "pmml-test-1.xml"
    val pmmlSource = Source.fromResource(pmmlFilePath).getLines()

    val pmmlStream = new ByteArrayOutputStream()
    for (line <- pmmlSource) {
      pmmlStream.write(line.getBytes)
    }

    val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()

    val pmml = org.jpmml.model.PMMLUtil.unmarshal(new ByteArrayInputStream(pmmlStream.toByteArray))

    val e: ModelEvaluator[_] = modelEvaluatorFactory.newModelEvaluator(pmml)

    val m = new NewModel(e)

    m.score(null)
  }
}
