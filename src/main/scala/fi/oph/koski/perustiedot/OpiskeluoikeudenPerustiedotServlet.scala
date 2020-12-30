package fi.oph.koski.perustiedot

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.util.SortOrder.Ascending
import fi.oph.koski.util.{PaginatedResponse, Pagination, PaginationSettings, SortOrder}

class OpiskeluoikeudenPerustiedotServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Pagination with NoCache {
  // TODO: Pitäisikö näistäkin katseluista tehdä auditlog-merkintä?
  get("/") {
    renderEither[PaginatedResponse[OpiskeluoikeudenPerustiedotResponse]]({
      val sort = SortOrder.parseSortOrder(params.get("sort"), Ascending("nimi"))
      println("tääl")
      val thing: Either[HttpStatus, PaginatedResponse[OpiskeluoikeudenPerustiedotResponse]] = OpiskeluoikeusQueryFilter.parse(multiParams)(application.koodistoViitePalvelu, application.organisaatioService, koskiSession) match {
        case Right(filters) =>
          println("ei vielä ricci0")
          val pagination: PaginationSettings = paginationSettings.getOrElse(PaginationSettings(0, 100))
          println("ei vielä ricci1")
          val result: OpiskeluoikeudenPerustiedotResponse = application.perustiedotRepository.find(filters, sort, pagination)(koskiSession)
          println("ei vielä ricci2")
          Right(PaginatedResponse(Some(pagination), result, result.tiedot.length))
        case Left(HttpStatus(404, _)) =>
          Right(PaginatedResponse(None, OpiskeluoikeudenPerustiedotResponse(None, List[OpiskeluoikeudenPerustiedot]()), 0))
        case Left(err) =>
          Left(err)
      }
      println("ei pääse tänne asti")
      println(thing)
      thing
    })
  }
}
