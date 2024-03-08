package fi.oph.koski.queuedqueries

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.util.UuidUtils
import fi.oph.scalaschema.annotation.{Description, EnumValue, SyntheticProperty, Title}
import org.json4s.jackson.JsonMethods

import java.time.LocalDateTime

object QueryServletUrls {
  def root(rootUrl: String): String = s"$rootUrl/api/kyselyt"
  def query(rootUrl: String, queryId: String): String = s"${root(rootUrl)}/$queryId"
  def file(rootUrl: String, queryId: String, fileKey: String): String = s"${root(rootUrl)}/$queryId/$fileKey"
}

class QueryServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with JsonMethods with NoCache
{
  val kyselyt: QueryService = application.kyselyService
  val rootUrl: String = application.config.getString("koski.root.url")

  post("/") {
    withJsonBody { body =>
      renderEither {
        application
          .validatingAndResolvingExtractor
          .extract[QueryParameters](strictDeserialization)(body)
          .flatMap(kyselyt.add)
          .map(q => QueryResponse(rootUrl, q))
      }
    } (parseErrorHandler = jsonErrorHandler)
  }

  get("/:id") {
    renderEither {
      UuidUtils.optionFromString(getStringParam("id"))
        .toRight(KoskiErrorCategory.badRequest.queryParam("Epävalidi tunniste"))
        .flatMap(kyselyt.get)
        .map(q => QueryResponse(rootUrl, q))
    }
  }

  get("/:id/:file") {
    UuidUtils.optionFromString(getStringParam("id"))
      .toRight(KoskiErrorCategory.badRequest.queryParam("Epävalidi tunniste"))
      .flatMap(kyselyt.get)
      .flatMap {
        case q: CompleteQuery =>
          kyselyt.getDownloadUrl(q, getStringParam("file"))
            .toRight(KoskiErrorCategory.badRequest("Tiedostoa ei löydy tai tapahtui virhe sen jakamisessa"))
        case _ =>
          Left(KoskiErrorCategory.badRequest("Tulostiedostot eivät ole vielä ladattavissa"))
      }
      .fold(renderStatus, redirect)
  }

  private def jsonErrorHandler(status: HttpStatus) = {
    haltWithStatus(status)
  }
}

trait QueryResponse {
  @Description("Kyselyn tunniste")
  def queryId: String
  @Description("Kyselyn tila")
  @SyntheticProperty
  def status: String
  @Description("Kyselyn luoman käyttäjän oid")
  def requestedBy: String
  @Description("Määrittää tehtävän kyselyn sekä sen parametrit.")
  def query: QueryParameters
  @Description("Kyselyn luontiaika")
  def createdAt: LocalDateTime
}

@Title("Odottava kysely")
@Description("Kysely on luotu, mutta sen käsittelyä ei ole vielä aloitettu.")
case class PendingQueryResponse(
  queryId: String,
  requestedBy: String,
  query: QueryParameters,
  createdAt: LocalDateTime,
  @Description("Osoite josta kyselyn tilaa voi kysellä")
  resultsUrl: String,
) extends QueryResponse {
  def status: String = QueryState.pending
}

case class RunningQueryResponse(
  queryId: String,
  requestedBy: String,
  query: QueryParameters,
  createdAt: LocalDateTime,
  @Description("Kyselyn käsittelyn aloitusaika")
  startedAt: LocalDateTime,
  @Description("Osoite josta kyselyn tilaa voi kysellä")
  resultsUrl: String,
) extends QueryResponse {
  def status: String = QueryState.running
}

case class FailedQueryResponse(
  queryId: String,
  requestedBy: String,
  query: QueryParameters,
  createdAt: LocalDateTime,
  @Description("Kyselyn käsittelyn aloitusaika")
  startedAt: LocalDateTime,
  @Description("Kyselyn epäonnistumisen aika")
  finishedAt: LocalDateTime,
) extends QueryResponse {
  def status: String = QueryState.failed
}

case class CompleteQueryResponse(
  queryId: String,
  requestedBy: String,
  query: QueryParameters,
  createdAt: LocalDateTime,
  @Description("Kyselyn käsittelyn aloitusaika")
  startedAt: LocalDateTime,
  @Description("Kyselyn valmistumisaika")
  finishedAt: LocalDateTime,
  @Description("Lista osoitteista, joista tulostiedostot voi ladata. Tiedostojen määrä riippuu kyselyn tyypistä.")
  files: List[String],
  @Description(s"Tiedostojen avaamiseen tarvittava salasana. Käytössä vain xlsx-tiedostojen (${QueryFormat.xlsx}) kanssa.")
  password: Option[String],
) extends QueryResponse {
  def status: String = QueryState.complete
}

object QueryResponse {
  def apply(rootUrl: String, query: Query): QueryResponse = query match {
    case q: PendingQuery => PendingQueryResponse(
      queryId = q.queryId,
      requestedBy = q.userOid,
      query = q.query,
      createdAt = q.createdAt,
      resultsUrl = q.externalResultsUrl(rootUrl),
    )
    case q: RunningQuery => RunningQueryResponse(
      queryId = q.queryId,
      requestedBy = q.userOid,
      query = q.query,
      createdAt = q.createdAt,
      startedAt = q.startedAt,
      resultsUrl = q.externalResultsUrl(rootUrl),
    )
    case q: FailedQuery => FailedQueryResponse(
      queryId = q.queryId,
      requestedBy = q.userOid,
      query = q.query,
      createdAt = q.createdAt,
      startedAt = q.startedAt,
      finishedAt = q.finishedAt,
    )
    case q: CompleteQuery => CompleteQueryResponse(
      queryId = q.queryId,
      requestedBy = q.userOid,
      query = q.query,
      createdAt = q.createdAt,
      startedAt = q.startedAt,
      finishedAt = q.finishedAt,
      files = q.filesToExternal(rootUrl),
      password = q.meta.flatMap(_.password),
    )
  }
}
