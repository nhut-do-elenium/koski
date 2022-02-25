package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.organisaatio.{Opetushallitus, OrganisaatioHierarkia, OrganisaatioHierarkiaJaKayttooikeusrooli}
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.util.ChainingSyntax._
import fi.oph.koski.util.UuidUtils
import fi.oph.koski.valpas.db.ValpasSchema.OpiskeluoikeusLisätiedotKey
import fi.oph.koski.valpas.log.ValpasAuditLog._
import fi.oph.koski.valpas.opiskeluoikeusrepository.{HakeutumisvalvontaTieto, ValpasOppilaitos}
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasrepository.{OppivelvollisuudenKeskeytyksenMuutos, UusiOppivelvollisuudenKeskeytys}
import fi.oph.koski.valpas.valpasuser.RequiresValpasSession
import org.json4s.JValue

class ValpasRootApiServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with RequiresValpasSession {
  private lazy val organisaatioService = application.organisaatioService
  private lazy val oppijaLaajatTiedotService = application.valpasOppijaLaajatTiedotService
  private lazy val oppijaSuppeatTiedotService = application.valpasOppijaSuppeatTiedotService
  private lazy val oppijaSearchService = application.valpasOppijaSearchService

  get("/user") {
    session.user
  }

  get("/organisaatiot-ja-kayttooikeusroolit") {
    val globaalit = session.globalKäyttöoikeudet.toList.flatMap(_.globalPalveluroolit.map(palvelurooli =>
      OrganisaatioHierarkiaJaKayttooikeusrooli(
        OrganisaatioHierarkia(Opetushallitus.organisaatioOid, Opetushallitus.nimi, List.empty, List.empty),
        palvelurooli.rooli
      )
    )).sortBy(r => (r.organisaatioHierarkia.nimi.get(session.lang), r.kayttooikeusrooli))

    val organisaatiokohtaiset = organisaatioService.omatOrganisaatiotJaKayttooikeusroolit

    globaalit ++ organisaatiokohtaiset
  }

  get("/oppijat/:organisaatio") {
    val oppilaitosOid: ValpasOppilaitos.Oid = params("organisaatio")
    renderEither(
      oppijaSuppeatTiedotService.getHakeutumisvalvottavatOppijatSuppeatTiedot(oppilaitosOid, HakeutumisvalvontaTieto.Perusopetus)
        .tap(_ => auditLogOppilaitosKatsominen(oppilaitosOid))
    )
  }

  get("/oppijat-nivelvaihe/:organisaatio") {
    val oppilaitosOid: ValpasOppilaitos.Oid = params("organisaatio")
    renderEither(
      oppijaSuppeatTiedotService.getHakeutumisvalvottavatOppijatSuppeatTiedot(oppilaitosOid, HakeutumisvalvontaTieto.Nivelvaihe)
        .tap(_ => auditLogOppilaitosKatsominen(oppilaitosOid))
    )
  }

  get("/oppijat/:organisaatio/ilmoitukset") {
    val oppilaitosOid: ValpasOppilaitos.Oid = params("organisaatio")
    renderEither(
      oppijaSuppeatTiedotService.getHakeutumisenvalvonnanKunnalleTehdytIlmoituksetSuppeatTiedot(oppilaitosOid)
        .tap(_ => auditLogOppilaitosKatsominen(oppilaitosOid))
    )
  }

  get("/oppijat-suorittaminen/:organisaatio") {
    val oppilaitosOid: ValpasOppilaitos.Oid = params("organisaatio")
    renderEither(
      oppijaSuppeatTiedotService.getSuorittamisvalvottavatOppijatSuppeatTiedot(oppilaitosOid)
        .tap(_ => auditLogOppilaitosKatsominen(oppilaitosOid))
    )
  }

  get("/oppijat-suorittaminen/:organisaatio/ilmoitukset") {
    val oppilaitosOid: ValpasOppilaitos.Oid = params("organisaatio")
    renderEither(
      oppijaSuppeatTiedotService.getSuorittamisvalvonnanKunnalleTehdytIlmoituksetSuppeatTiedot(oppilaitosOid)
        .tap(_ => auditLogOppilaitosKatsominen(oppilaitosOid))
    )
  }

  get("/oppija/:oid") {
    renderEither(
      oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(params("oid"))
        .tap(result => auditLogOppijaKatsominen(result.oppija.henkilö.oid))
    )
  }

  get("/henkilohaku/maksuttomuus/:query") {
    val query = params("query")
    renderEither(
      oppijaSearchService.findHenkilöMaksuttomuus(query)
        .map(_.cleanUpForUserSearch)
        .tap(auditLogHenkilöHaku(query))
    )
  }

  get("/henkilohaku/suorittaminen/:query") {
    val query = params("query")
    renderEither(
      oppijaSearchService.findHenkilöSuorittaminen(query)
        .map(_.cleanUpForUserSearch)
        .tap(auditLogHenkilöHaku(query))
    )
  }

  get("/henkilohaku/kunta/:query") {
    val query = params("query")
    renderEither(
      oppijaSearchService.findHenkilöKunta(query)
        .map(_.cleanUpForUserSearch)
        .tap(auditLogHenkilöHaku(query))
    )
  }

  put("/oppija/:oid/set-muu-haku") {
    val oppijaOid = params("oid")
    val ooOid = getStringParam("opiskeluoikeusOid")
    val oppilaitosOid = getStringParam("oppilaitosOid")
    val value = getBooleanParam("value")

    val key = OpiskeluoikeusLisätiedotKey(
      oppijaOid = oppijaOid,
      opiskeluoikeusOid = ooOid,
      oppilaitosOid = oppilaitosOid
    )
    oppijaLaajatTiedotService.setMuuHaku(key, value)
  }

  post("/oppija/ovkeskeytys") {
    withJsonBody { (body: JValue) => {
      val keskeytys = application
        .validatingAndResolvingExtractor
        .extract[UusiOppivelvollisuudenKeskeytys](strictDeserialization)(body)

      val result = keskeytys
        .flatMap(oppijaLaajatTiedotService.addOppivelvollisuudenKeskeytys)
        .tap(_ => keskeytys.tap(auditLogOppivelvollisuudenKeskeytys))

      renderEither(result)
    } } (parseErrorHandler = handleUnparseableJson)
  }

  put("/oppija/ovkeskeytys") {
    withJsonBody { (body: JValue) => {
      val keskeytys = application
        .validatingAndResolvingExtractor
        .extract[OppivelvollisuudenKeskeytyksenMuutos](strictDeserialization)(body)

      val result = keskeytys
        .flatMap(oppijaLaajatTiedotService.updateOppivelvollisuudenKeskeytys)
        .tap(k => auditLogOppivelvollisuudenKeskeytysUpdate(k._1.oppijaOid, k._1.tekijäOrganisaatioOid))
        .map(k => k._2)

      renderEither(result)
    } } (parseErrorHandler = handleUnparseableJson)
  }

  delete("/oppija/ovkeskeytys/:uuid") {
    val result = UuidUtils.optionFromString(params("uuid"))
      .toRight(ValpasErrorCategory.badRequest.validation.epävalidiUuid())
      .flatMap(oppijaLaajatTiedotService.deleteOppivelvollisuudenKeskeytys)
      .tap(k => auditLogOppivelvollisuudenKeskeytysDelete(k._1.oppijaOid, k._1.tekijäOrganisaatioOid))
      .map(_ => "ok")

    renderEither(result)
  }

  private def handleUnparseableJson(status: HttpStatus) = {
    haltWithStatus(status)
  }
}
