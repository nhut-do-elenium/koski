package fi.oph.koski.henkilo

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException, NoCache}
import fi.oph.koski.util.Timing
import org.scalatra._

class HenkilötiedotServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Logging with ContentEncodingSupport with NoCache with Timing {
  private val henkilötiedotFacade = HenkilötiedotFacade(application.henkilöRepository, application.opiskeluoikeusRepository, application.possu)

  // note: deprecated, use POST version instead, will be removed if nobody uses this
  get("/search") {
    params.get("query") match {
      case Some(query) if query.length >= 3 =>
        henkilötiedotFacade.search(query.toUpperCase)(koskiSession)
      case _ =>
        throw InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort)
    }
  }

  // uses POST to avoid having potentially sensitive data in URLs
  post("/search") {
    withJsonBody({ body =>
      val request = JsonSerializer.extract[HenkilötiedotSearchRequest](body)
      request.query match {
        case query: String if query.length >= 3 =>
          henkilötiedotFacade.search(query.toUpperCase)(koskiSession)
        case _ =>
          throw InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort)
      }
    })()
  }

  // note: deprecated, use POST version instead, will be removed if nobody uses this
  get("/hetu/:hetu") {
    renderEither(henkilötiedotFacade.findByHetu(params("hetu"))(koskiSession))
  }

  // uses POST to avoid having sensitive data in URLs
  post("/hetu") {
    withJsonBody({ body =>
      val request = JsonSerializer.extract[HenkilötiedotHetuRequest](body)
      renderEither(henkilötiedotFacade.findByHetu(request.hetu)(koskiSession))
    })()
  }

  get("/oid/:oid") {
    renderEither(henkilötiedotFacade.findByOid(params("oid"))(koskiSession).right.map(_.map(_.copy(hetu = None)))) // poistetaan hetu tuloksista, sillä käytössä ei ole organisaatiorajausta
  }
}

case class HenkilötiedotSearchRequest(query: String)

case class HenkilötiedotHetuRequest(hetu: String)
