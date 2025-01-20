package fi.oph.koski.massaluovutus.luokallejaaneet

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.fge.jsonpatch.JsonPatch
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, DatabaseConverters, QueryMethods}
import fi.oph.koski.history.OpiskeluoikeusHistoryPatch
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession, Rooli}
import fi.oph.koski.log.Logging
import fi.oph.koski.massaluovutus.MassaluovutusUtils.defaultOrganisaatio
import fi.oph.koski.massaluovutus.{MassaluovutusQueryParameters, QueryFormat, QueryResultWriter}
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, PerusopetuksenOpiskeluoikeus}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}
import org.json4s.jackson.JsonMethods
import org.json4s.{JArray, JValue}

import java.sql.Timestamp

@Title("Perusopetuksen luokalle jäämiset")
@Description("Tämä kysely on tarkoitettu opiskeluoikeusversioiden löytäiseksi KOSKI-varannoksi, joissa perusopetuksen opiskeluoikeuteen on merkitty tieto, että oppilas jää luokalle.")
@Description("Vastauksen skeema on saatavana <a href=\"/koski/json-schema-viewer/?schema=luokalle-jaaneet-result.json\">täältä.</a>")
case class MassaluovutusQueryLuokalleJaaneet (
  @EnumValues(Set("luokallejaaneet"))
  `type`: String = "luokallejaaneet",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  @Description("Kyselyyn otettavan koulutustoimijan tai oppilaitoksen oid. Jos ei ole annettu, päätellään käyttäjän käyttöoikeuksista.")
  organisaatioOid: Option[String],
) extends MassaluovutusQueryParameters with DatabaseConverters with Logging {
  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: KoskiSpecificSession): Either[String, Unit] = {
    val oppilaitosOids = application.organisaatioService.organisaationAlaisetOrganisaatiot(organisaatioOid.get)
    val oids = haeLuokalleJäämisenSisältävätOpiskeluoikeusOidit(application.raportointiDatabase.db, oppilaitosOids)

    oids.foreach { case (oppijaOid, opiskeluoikeusOid) =>
      application.historyRepository
        .findByOpiskeluoikeusOid(opiskeluoikeusOid)
        .map { patches =>
          patches
            .foldLeft(LuokalleJääntiAccumulator()) { (acc, diff) => acc.next(diff) }
            .matches
            .foreach { case (luokka, oo) =>
              val response = MassaluovutusQueryLuokalleJaaneetResult(oo, luokka, oppijaOid)
              writer.putJson(s"${opiskeluoikeusOid}_luokka_$luokka", response)
            }
        }
    }

    Right(Unit)
  }

  override def queryAllowed(application: KoskiApplication)(implicit user: KoskiSpecificSession): Boolean =
    user.hasGlobalReadAccess || (
      organisaatioOid.exists(user.organisationOids(AccessType.read).contains)
        && user.sensitiveDataAllowed(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
      )

  override def fillAndValidate(implicit user: KoskiSpecificSession): Either[HttpStatus, MassaluovutusQueryLuokalleJaaneet] =
    if (organisaatioOid.isEmpty) {
      defaultOrganisaatio.map(o => copy(organisaatioOid = Some(o)))
    } else {
      Right(this)
    }

  private def haeLuokalleJäämisenSisältävätOpiskeluoikeusOidit(raportointiDb: DB, oppilaitosOids: Seq[String]): Seq[(String, String)] =
    QueryMethods.runDbSync(raportointiDb, sql"""
      SELECT
        r_opiskeluoikeus.oppija_oid,
        r_opiskeluoikeus.opiskeluoikeus_oid
      FROM r_opiskeluoikeus
      JOIN r_paatason_suoritus ON r_paatason_suoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
      WHERE koulutusmuoto = 'perusopetus'
        AND jaa_luokalle
        AND oppilaitos_oid = any($oppilaitosOids)
      GROUP BY
        r_opiskeluoikeus.oppija_oid,
        r_opiskeluoikeus.opiskeluoikeus_oid
    """.as[Tuple2[String, String]])
}

case class LuokalleJääntiAccumulator(
  opiskeluoikeus: JsonNode = JsonNodeFactory.instance.objectNode(),
  invalidHistory: Boolean = false,
  matches: Map[String, LuokalleJääntiMatch] = Map(),
) {
  def next(diff: OpiskeluoikeusHistoryPatch): LuokalleJääntiAccumulator = {
    try {
      val oo = JsonPatch.fromJson(JsonMethods.asJsonNode(diff.muutos)).apply(opiskeluoikeus)
      LuokalleJääntiAccumulator(
        oo,
        invalidHistory,
        newMathes(oo, diff),
      )
    } catch {
      case _: Exception => LuokalleJääntiAccumulator(invalidHistory = true)
    }

  }

  private def newMathes(oo: JsonNode, diff: OpiskeluoikeusHistoryPatch): Map[String, LuokalleJääntiMatch] =
    jääLuokalleLuokilla(oo).foldLeft(Map() : Map[String, LuokalleJääntiMatch]) { (acc, m) =>
      val (luokka, ooJson) = m
      if (acc.keySet.contains(luokka)) {
        acc
      } else {
        acc + (luokka -> LuokalleJääntiMatch(ooJson, diff))
      }
    }

  private def jääLuokalleLuokilla(oo: JsonNode): List[(String, JValue)] =
    suoritukset(oo)
      .arr
      .filter { s => JsonSerializer.extract[Option[Boolean]](s \ "jääLuokalle").getOrElse(false) }
      .map { s => (
        JsonSerializer.extract[String](s \ "koulutusmoduuli" \ "tunniste" \ "koodiarvo"),
        JsonMethods.fromJsonNode(oo),
      ) }

  private def suoritukset(oo: JsonNode): JArray = (JsonMethods.fromJsonNode(oo) \ "suoritukset").asInstanceOf[JArray]
}

case class LuokalleJääntiMatch(
  opiskeluoikeus: JValue,
  aikaleima: Timestamp,
  versio: Int,
) {
  def perusopetuksenOpiskeluoikeus: PerusopetuksenOpiskeluoikeus =
    JsonSerializer
      .extract[PerusopetuksenOpiskeluoikeus](opiskeluoikeus)
      .copy(
        versionumero = Some(versio),
        aikaleima = Some(aikaleima.toLocalDateTime),
      )
}

object LuokalleJääntiMatch {
  def apply(opiskeluoikeus: JValue, diff: OpiskeluoikeusHistoryPatch): LuokalleJääntiMatch = LuokalleJääntiMatch(
    opiskeluoikeus = opiskeluoikeus,
    aikaleima = diff.aikaleima,
    versio = diff.versionumero,
  )
}
