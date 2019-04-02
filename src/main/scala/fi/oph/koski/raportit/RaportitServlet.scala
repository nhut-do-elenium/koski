package fi.oph.koski.raportit

import java.time.LocalDate
import java.time.format.DateTimeParseException

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.log.KoskiMessageField.hakuEhto
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_RAPORTTI
import fi.oph.koski.log.{AuditLog, AuditLogMessage, Logging}
import fi.oph.koski.organisaatio.OrganisaatioOid
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import org.scalatra.{ContentEncodingSupport, Cookie, CookieOptions}

class RaportitServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Logging with NoCache with ContentEncodingSupport {

  private lazy val raportointiDatabase = application.raportointiDatabase
  private lazy val raportitService = new RaportitService(application)

  before() {
    val loadCompleted = raportointiDatabase.fullLoadCompleted(raportointiDatabase.statuses)
    if (loadCompleted.isEmpty) {
      haltWithStatus(KoskiErrorCategory.unavailable.raportit())
    }
  }

  get("/mahdolliset-raportit/:oppilaitosOid") {
    val oppilaitosOid = OrganisaatioOid.validateOrganisaatioOid(getStringParam("oppilaitosOid")) match {
      case Left(error) => haltWithStatus(error)
      case Right(oid) => oid
    }
    raportitService.resolveRaportitOppilaitokselle(oppilaitosOid)
  }

  get("/opiskelijavuositiedot") {
    val parsedRequest = parseAikajaksoRaporttiRequest
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=opiskelijavuositiedot&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}")))
    excelResponse(raportitService.opiskelijaVuositiedot(parsedRequest))
  }

  get("/suoritustietojentarkistus") {
    val parsedRequest = parseAikajaksoRaporttiRequest
    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=suoritustietojentarkistus&oppilaitosOid=${parsedRequest.oppilaitosOid}&alku=${parsedRequest.alku}&loppu=${parsedRequest.loppu}")))
    excelResponse(raportitService.suoritustietojenTarkistus(parsedRequest))
  }

  private def excelResponse(raportti: OppilaitosRaporttiResponse) = {
    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    response.setHeader("Content-Disposition", s"""attachment; filename="${raportti.filename}"""")
    raportti.downloadToken.foreach { t => response.addCookie(Cookie("koskiDownloadToken", t)(CookieOptions(path = "/", maxAge = 600))) }
    ExcelWriter.writeExcel(
      raportti.workbookSettings,
      raportti.sheets,
      response.getOutputStream
    )
  }

  private def parseAikajaksoRaporttiRequest: AikajaksoRaporttiRequest = {
    val oppilaitosOid = getOppilaitosParamAndCheckAccess
    val (alku, loppu) = getAlkuLoppuParams
    val password = getStringParam("password")
    val downloadToken = params.get("downloadToken")

    AikajaksoRaporttiRequest(oppilaitosOid, downloadToken, password, alku, loppu)
  }

  private def getOppilaitosParamAndCheckAccess: Organisaatio.Oid = {
    if (!koskiSession.hasRaportitAccess) {
      haltWithStatus(KoskiErrorCategory.forbidden.organisaatio())
    }
    val oppilaitosOid = OrganisaatioOid.validateOrganisaatioOid(getStringParam("oppilaitosOid")) match {
      case Left(error) => haltWithStatus(error)
      case Right(oid) if !koskiSession.hasReadAccess(oid) => haltWithStatus(KoskiErrorCategory.forbidden.organisaatio())
      case Right(oid) => oid
    }
    oppilaitosOid
  }

  private def getAlkuLoppuParams: (LocalDate, LocalDate) = {
    try {
      val alku = LocalDate.parse(getStringParam("alku"))
      val loppu = LocalDate.parse(getStringParam("loppu"))
      if (loppu.isBefore(alku)) {
        haltWithStatus(KoskiErrorCategory.badRequest.format.pvm("loppu ennen alkua"))
      }
      (alku, loppu)
    } catch {
      case e: DateTimeParseException => haltWithStatus(KoskiErrorCategory.badRequest.format.pvm())
    }
  }
}
