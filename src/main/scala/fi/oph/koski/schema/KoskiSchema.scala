package fi.oph.koski.schema

import fi.oph.scalaschema._
import org.json4s.JValue
import org.json4s.jackson.JsonMethods

object KoskiSchema {
  private val metadataTypes = SchemaFactory.defaultAnnotations ++ List(
    classOf[KoodistoUri], classOf[KoodistoKoodiarvo], classOf[ReadOnly], classOf[OksaUri], classOf[Hidden], classOf[Representative], classOf[ComplexObject], classOf[Flatten], classOf[Tabular],
    classOf[ClassName], classOf[MultiLineString], classOf[UnitOfMeasure], classOf[Example], classOf[RequiresRole], classOf[OnlyWhen])
  lazy val schemaFactory: SchemaFactory = SchemaFactory(metadataTypes)
  lazy val schema = createSchema(classOf[Oppija]).asInstanceOf[ClassSchema]
  lazy val schemaJson: JValue = SchemaToJson.toJsonSchema(schema)
  lazy val schemaJsonString = JsonMethods.compact(schemaJson)
  lazy implicit val deserializationContext = ExtractionContext(schemaFactory, allowEmptyStrings = false)

  def createSchema(clazz: Class[_]) = schemaFactory.createSchema(clazz) match {
    case s: AnyOfSchema => s
    case s: ClassSchema => s.moveDefinitionsToTopLevel
  }
}