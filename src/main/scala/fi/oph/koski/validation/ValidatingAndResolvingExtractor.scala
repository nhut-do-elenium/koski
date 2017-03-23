package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.{KoodistoResolvingCustomDeserializer, KoodistoViitePalvelu}
import fi.oph.koski.organisaatio.{OrganisaatioRepository, OrganisaatioResolvingCustomDeserializer}
import fi.oph.koski.schema._
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.json4s._

object ValidatingAndResolvingExtractor {
  import KoskiSchema.deserializationContext
  /**
   *  Extracts object from json value, and validates/resolves all KoodistoKoodiViite objects on the way.
   */
  def extract[T](json: JValue, context: ValidationAndResolvingContext)(implicit mf: Manifest[T]): Either[HttpStatus, T] = {
    //ContextualExtractor.extract[T, ValidationAndResolvingContext](json, context)(mf, Json.jsonFormats + KoodistoResolvingDeserializer + OrganisaatioResolvingDeserializer)
    val klass = mf.asInstanceOf[ClassManifest[_]].runtimeClass
    SchemaValidatingExtractor.extract(json, KoskiSchema.schema.getSchema(klass.getName).get, Nil)(deserializationContext.copy(customDeserializers = List(
      OrganisaatioResolvingCustomDeserializer(context.organisaatioRepository),
      KoodistoResolvingCustomDeserializer(context.koodistoPalvelu)
    ))) match {
      case Right(t: T) => Right(t)
      case Left(errors) => Left(KoskiErrorCategory.badRequest.validation.jsonSchema(errors))
    }
  }
}

case class ValidationAndResolvingContext(koodistoPalvelu: KoodistoViitePalvelu, organisaatioRepository: OrganisaatioRepository)



