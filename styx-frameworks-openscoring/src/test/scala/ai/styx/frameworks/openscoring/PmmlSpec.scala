package ai.styx.frameworks.openscoring

import java.io.{FileInputStream, InputStream}

import ai.styx.common.BaseSpec
import org.jpmml.evaluator.ModelEvaluatorFactory

import scala.io.Source

class PmmlSpec extends BaseSpec {

  "PMML Model Instance" should "" in {
    // val p = PmmlModel("test model", "")

    // load data file
    val pmmlFilePath = "pmml-test-1.xml"
    val pmmlSource = Source.fromResource(pmmlFilePath)

    val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()
    val p = "this is a test PMML string"
    val s = new FileInputStream("test")
    val pmml = org.jpmml.model.PMMLUtil.unmarshal(pmmlSource.reader().asInstanceOf[InputStream])  // InputStream

    val e = modelEvaluatorFactory.newModelEvaluator(pmml)

    val m = new NewModel(e)

    m.score(null)
  }
}
