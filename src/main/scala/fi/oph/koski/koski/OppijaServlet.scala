package fi.oph.koski.koski

import javax.servlet.http.HttpServletRequest

import fi.oph.koski.db.GlobalExecutionContext
import fi.oph.koski.henkilo.HenkiloOid
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser._
import fi.oph.koski.log.AuditLog.{log => auditLog}
import fi.oph.koski.log._
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.{HenkilöWithOid, Oppija}
import fi.oph.koski.servlet.RequestDescriber.logSafeDescription
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException, NoCache, RequestDescriber}
import fi.oph.koski.util.Timing
import fi.vm.sade.security.ldap.DirectoryClient
import org.json4s.JsonAST.JArray
import org.scalatra.GZipSupport
import rx.lang.scala.Observable

class OppijaServlet(rekisteri: KoskiFacade, val käyttöoikeudet: KäyttöoikeusRepository, val directoryClient: DirectoryClient, val validator: KoskiValidator, val historyRepository: OpiskeluoikeusHistoryRepository)
  extends ApiServlet with RequiresAuthentication with Logging with GlobalExecutionContext with ObservableSupport with GZipSupport with NoCache with Timing {

  put("/") {
    timed("PUT /oppija", thresholdMs = 10) {
      withJsonBody { parsedJson =>
        val validationResult: Either[HttpStatus, Oppija] = validator.extractAndValidate(parsedJson)(koskiUser, AccessType.write)
        val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = UpdateContext(koskiUser, rekisteri, request).putSingle(validationResult)
        renderEither(result)
      }
    }
  }

  put("/batch") {
    timed("PUT /oppija/batch", thresholdMs = 10) {
      withJsonBody { parsedJson =>
        val putter = UpdateContext(koskiUser, rekisteri, request)

        val validationResults: List[Either[HttpStatus, Oppija]] = validator.extractAndValidateBatch(parsedJson.asInstanceOf[JArray])(koskiUser, AccessType.write)
        val batchResults: List[Either[HttpStatus, HenkilönOpiskeluoikeusVersiot]] = validationResults.par.map(putter.putSingle).toList

        response.setStatus(batchResults.map {
          case Left(status) => status.statusCode
          case _ => 200
        }.max)

        batchResults
      }
    }
  }

  get("/") {
    query
  }

  get("/:oid") {
    renderEither(findByOid(params("oid"), koskiUser))
  }

  get("/validate") {
    val context = ValidateContext(koskiUser, validator)
    query.map(context.validateOppija)
  }

  get("/validate/:oid") {
    renderEither(
      findByOid(params("oid"), koskiUser)
        .right.flatMap(validateHistory)
        .right.map(ValidateContext(koskiUser, validator).validateOppija)
    )
  }

  get("/search") {
    contentType = "application/json;charset=utf-8"
    params.get("query") match {
      case Some(query) if (query.length >= 3) =>
        rekisteri.findOppijat(query.toUpperCase)(koskiUser)
      case _ =>
        throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort)
    }
  }



  private def validateHistory(oppija: Oppija): Either[HttpStatus, Oppija] = {
    HttpStatus.fold(oppija.opiskeluoikeudet.map { oikeus =>
      historyRepository.findVersion(oikeus.id.get, oikeus.versionumero.get)(koskiUser) match {
        case Right(latestVersion) =>
          HttpStatus.validate(latestVersion == oikeus) {
            KoskiErrorCategory.internalError(Json.toJValue(HistoryInconsistency(oikeus + " versiohistoria epäkonsistentti", Json.jsonDiff(oikeus, latestVersion))))
          }
        case Left(error) => error
      }
    }) match {
      case HttpStatus.ok => Right(oppija)
      case status: HttpStatus => Left(status)
    }
  }

  private def query: Observable[Oppija] = {
    logger(koskiUser).info("Haetaan opiskeluoikeuksia: " + Option(request.getQueryString).getOrElse("ei hakuehtoja"))

    rekisteri.findOppijat(params.toList, koskiUser) match {
      case Right(oppijat) => oppijat
      case Left(status) => haltWithStatus(status)
    }
  }

  private def findByOid(oid: String, user: KoskiUser): Either[HttpStatus, Oppija] = {
    HenkiloOid.validateHenkilöOid(oid).right.flatMap { oid =>
      rekisteri.findOppija(oid)(user)
    }
  }
}

/**
  *  Operating context for data updates. Operates outside the lecixal scope of OppijaServlet to ensure that none of the
  *  Scalatra threadlocals are used. This must be done because in batch mode, we are running in several threads.
  */
case class UpdateContext(user: KoskiUser, facade: KoskiFacade, request: HttpServletRequest) extends Logging {
  def putSingle(validationResult: Either[HttpStatus, Oppija]): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {
    val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = validationResult.right.flatMap(facade.createOrUpdate(_)(user))
    result.left.foreach { case HttpStatus(code, errors) =>
      logger(user).warn("Opinto-oikeuden päivitys estetty: " + code + " " + errors + " for request " + logSafeDescription(request))
    }
    result
  }
}
/**
  *  Operating context for data validation. Operates outside the lecixal scope of OppijaServlet to ensure that none of the
  *  Scalatra threadlocals are used. This must be done because in batch mode, we are running in several threads.
  */
case class ValidateContext(user: KoskiUser, validator: KoskiValidator) {
  def validateOppija(oppija: Oppija): ValidationResult = {
    val oppijaOid: Oid = oppija.henkilö.asInstanceOf[HenkilöWithOid].oid
    val validationResult = validator.validateAsJson(oppija)(user, AccessType.read)
    validationResult match {
      case Right(oppija) =>
        ValidationResult(oppijaOid, Nil)
      case Left(status) =>
        ValidationResult(oppijaOid, status.errors)
    }
  }
}