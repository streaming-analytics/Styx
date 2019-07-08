package ai.styx.frameworks.openscoring

import java.io.ByteArrayOutputStream
import java.util
import java.util.UUID

import ai.styx.domain.models.ModelInstance
import ai.styx.domain.{Customer, PmmlModel}
import ai.styx.common.Logging
import org.dmg.pmml.{DataField, FieldName, Model}
import org.jpmml.evaluator._

import scala.collection.JavaConverters._

class PmmlModelInstance(pmmlModel: ModelEvaluator[_ <: Model]) extends ModelInstance with Logging {

  def score(customer: Customer): Double = {

    val arguments = new util.LinkedHashMap[FieldName, FieldValue]

    for (inputField: InputField <- pmmlModel.getInputFields.asScala) {
      // get input fields from the pmml file
      val pmmlDataField = inputField.getField match {
        case dataField: DataField => dataField
        case _ => throw new ClassCastException(s"pmml model field was not of type ${classOf[DataField].getName}")
      }
      val fieldName = pmmlDataField.getName

      // get input values, match the names of the input fields to the customer profile / event data
      val customerPrefix = "customer."
      val fieldValue = fieldName.getValue
      if (fieldValue.startsWith(customerPrefix)) {
        arguments.put(fieldName, inputField.prepare(customer.all(fieldValue.stripPrefix(customerPrefix))))
      } else {
        createDummyArgument(arguments, inputField, pmmlDataField, fieldName)
      }
    }
    // return the notification with a relevancy score
    getResult(arguments)
  }

  private def createDummyArgument(arguments: util.LinkedHashMap[FieldName, FieldValue], inputField: InputField, pmmlDataField: DataField, fieldName: FieldName) = {
    LOG.warn(s"Unknown field name: ${fieldName.getValue} with data type ${inputField.getDataType.value}. Putting zero/null value in...")

    inputField.getOpType.value match {
      case "continuous" =>
        inputField.getDataType.value match {
          case "double" => arguments.put(fieldName, inputField.prepare(0.0))
          case "integer" => arguments.put(fieldName, inputField.prepare(0))
        }
      case "categorical" | "ordinal" =>
        val validArgumentValues = FieldValueUtil.getValidValues(pmmlDataField)
        validArgumentValues.asScala.headOption match {
          case None => LOG.error("No valid argument value available")
          case Some(value) =>
            inputField.getDataType.value match {
              case "string" => arguments.put(fieldName, inputField.prepare(value))
              case "integer" => arguments.put(fieldName, inputField.prepare(value))
            }
        }
    }
  }

  private def getResult(arguments: util.LinkedHashMap[FieldName, FieldValue]) = {
    // get predictions
    val results = pmmlModel.evaluate(arguments)
    // a model can predict multiple target outcomes: for (targetField: TargetField <- Pmml.modelEvaluator.getTargetFields) {
    // for now, get the first target outcome, which is the prediction of the relevancy score
    pmmlModel.getTargetFields.asScala.headOption match {
      case None =>
        LOG.error("No (first) targetfield available")
        0D
      case Some(targetField) =>
        val targetFieldName = targetField.getName
        val targetFieldValue = results.get(targetFieldName)
        LOG.debug(s"Predicted score: $targetFieldName = $targetFieldValue.")
        targetFieldValue match {
          // TODO: not sure if the type parameter of the propability distrubtion should be Double
          case probabilityDistribution: ProbabilityDistribution[Double] => probabilityDistribution.getProbability("1").doubleValue
          case _ => throw new ClassCastException(s"target field value in pmml model was not of type ${classOf[ProbabilityDistribution[Double]].getName}")
        }
    }
  }

  def toPmml: PmmlModel = {
    val stream = new ByteArrayOutputStream()
    org.jpmml.model.PMMLUtil.marshal(pmmlModel.getPMML, stream)
    LOG.info("PMML model marshalled to string")
    stream.flush()
    PmmlModel(UUID.randomUUID().toString, stream.toString)
  }
}
