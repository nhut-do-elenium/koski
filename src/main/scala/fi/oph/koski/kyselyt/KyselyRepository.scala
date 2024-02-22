package fi.oph.koski.kyselyt

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{AuthenticationUser, KoskiSpecificSession, Käyttöoikeus, KäyttöoikeusRepository}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.validation.ValidatingAndResolvingExtractor
import org.json4s.jackson.JsonMethods
import slick.jdbc.GetResult

import java.net.InetAddress
import java.time.LocalDateTime
import java.util.UUID

class KyselyRepository(
  val db: DB,
  workerId: String,
  extractor: ValidatingAndResolvingExtractor,
)  extends QueryMethods with Logging  {

  def get(id: UUID)(implicit user: KoskiSpecificSession): Option[Query] =
    runDbSync(sql"""
      SELECT *
      FROM kysely
      WHERE id = ${id.toString}::uuid
        AND user_oid = ${user.oid}
      """.as[Query]
    ).headOption

  def getExisting(query: QueryParameters)(implicit user: KoskiSpecificSession): Option[Query] =
    runDbSync(sql"""
      SELECT *
      FROM kysely
      WHERE user_oid = ${user.oid}
        AND query = ${query.asJson}
        AND state IN (${QueryState.pending}, ${QueryState.running})
     """.as[Query]
    ).headOption

  def add(query: QueryParameters)(implicit user: KoskiSpecificSession): PendingQuery = {
    val session = JsonSerializer.serialize(StorableSession(user))
    runDbSync(sql"""
      INSERT INTO kysely(id, user_oid, session, query, state)
      VALUES (
        ${UUID.randomUUID().toString}::uuid,
        ${user.oid},
        $session,
        ${query.asJson},
        ${QueryState.pending}
       )
       RETURNING *
       """.as[Query])
      .collectFirst { case q: PendingQuery => q }
      .get
  }

  def numberOfRunningQueries: Int =
    runDbSync(sql"""
      SELECT count(*)
      FROM kysely
      WHERE state = ${QueryState.running}
        AND worker = $workerId
      """.as[Int]).head

  def takeNext: Option[RunningQuery] =
    runDbSync(sql"""
      UPDATE kysely
      SET
        state = ${QueryState.running},
        worker = $workerId,
        started_at = now()
      WHERE id IN (
        SELECT id
        FROM kysely
        WHERE state = ${QueryState.pending}
        ORDER BY created_at
        LIMIT 1
      )
      RETURNING *
      """.as[Query])
      .collectFirst { case q: RunningQuery => q }

  def setComplete(id: String, resultFiles: List[String]): Boolean =
    runDbSync(sql"""
      UPDATE kysely
      SET
        state = ${QueryState.complete},
        result_files = ${resultFiles},
        finished_at = now()
      WHERE id = ${id}::uuid
      """.asUpdate) != 0

  def setFailed(id: String, error: String): Boolean =
    runDbSync(
      sql"""
      UPDATE kysely
      SET
        state = ${QueryState.failed},
        error = $error,
        finished_at = now()
      WHERE id = ${id}::uuid
      """.asUpdate) != 0

  def setRunningTasksFailed(error: String): Boolean =
    runDbSync(
      sql"""
      UPDATE kysely
      SET
        state = ${QueryState.failed},
        error = $error,
        finished_at = now()
      WHERE worker = $workerId
        AND state = ${QueryState.running}
      """.asUpdate) != 0

  implicit private val getQueryResult: GetResult[Query] = GetResult[Query] { r =>
    val id = r.rs.getString("id")
    val userOid = r.rs.getString("user_oid")
    val session = r.rs.getString("session")
    val query = parseParameters(r.rs.getString("query"))
    val creationTime = r.rs.getTimestamp("created_at").toLocalDateTime

    r.rs.getString("state") match {
      case QueryState.pending => PendingQuery(
        queryId = id,
        userOid = userOid,
        session = session,
        query = query,
        createdAt = creationTime,
      )
      case QueryState.running => RunningQuery(
        queryId = id,
        userOid = userOid,
        session = session,
        query = query,
        createdAt = creationTime,
        startedAt = r.rs.getTimestamp("started_at").toLocalDateTime,
        worker = r.rs.getString("worker")
      )
      case QueryState.complete => CompleteQuery(
        queryId = id,
        userOid = userOid,
        session = session,
        query = query,
        createdAt = creationTime,
        startedAt = r.rs.getTimestamp("started_at").toLocalDateTime,
        finishedAt = r.rs.getTimestamp("finished_at").toLocalDateTime,
        worker = r.rs.getString("worker"),
        resultFiles = r.getArray("result_files").toList,
      )
      case QueryState.failed => FailedQuery(
        queryId = id,
        userOid = userOid,
        session = session,
        query = query,
        createdAt = creationTime,
        startedAt = r.rs.getTimestamp("started_at").toLocalDateTime,
        finishedAt = r.rs.getTimestamp("finished_at").toLocalDateTime,
        worker = r.rs.getString("worker"),
        error = r.rs.getString("error"),
      )
    }
  }

  private def parseParameters(parameters: String): QueryParameters = {
    val json = JsonMethods.parse(parameters)
    extractor.extract[QueryParameters](strictDeserialization)(json).right.get // TODO: parempi virheenhallinta siltä varalta että parametrit eivät deserialisoidukaan
  }
}

trait Query {
  def queryId: String
  def userOid: String
  def query: QueryParameters
  def state: String
  def createdAt: LocalDateTime
  def session: String

  def getSession(käyttöoikeudet: KäyttöoikeusRepository): Option[KoskiSpecificSession] =
    JsonSerializer
      .validateAndExtract[StorableSession](JsonMethods.parse(session))
      .map(_.toSession(käyttöoikeudet))
      .toOption
}
case class PendingQuery(
  queryId: String,
  userOid: String,
  query: QueryParameters,
  createdAt: LocalDateTime,
  session: String,
) extends Query {
  def state: String = QueryState.pending
}

case class RunningQuery(
  queryId: String,
  userOid: String,
  query: QueryParameters,
  createdAt: LocalDateTime,
  startedAt: LocalDateTime,
  worker: String,
  session: String,
) extends Query {
  def state: String = QueryState.running
}

case class CompleteQuery(
  queryId: String,
  userOid: String,
  query: QueryParameters,
  createdAt: LocalDateTime,
  startedAt: LocalDateTime,
  finishedAt: LocalDateTime,
  worker: String,
  resultFiles: List[String],
  session: String,
) extends Query {
    def state: String = QueryState.complete
}

case class FailedQuery(
  queryId: String,
  userOid: String,
  query: QueryParameters,
  createdAt: LocalDateTime,
  startedAt: LocalDateTime,
  finishedAt: LocalDateTime,
  worker: String,
  error: String,
  session: String,
) extends Query {
    def state: String = QueryState.failed
}

object QueryState {
  val pending = "pending"
  val running = "running"
  val complete = "complete"
  val failed = "failed"
  val * : Set[String] = Set(pending, running, complete, failed)
}

case class StorableSession(
  oid: String,
  username: String,
  name: String,
  lang: String,
  clientIp: String,
  userAgent: String,
) {
  def toSession(käyttöoikeudet: KäyttöoikeusRepository): KoskiSpecificSession = {
    val user = AuthenticationUser(
      oid = oid,
      username = username,
      name = name,
      serviceTicket = None,
    )
    new KoskiSpecificSession(
      user = AuthenticationUser(
        oid = oid,
        username = username,
        name = name,
        serviceTicket = None,
      ),
      lang = lang,
      clientIp = InetAddress.getByName(clientIp),
      userAgent = userAgent,
      lähdeKäyttöoikeudet = käyttöoikeudet.käyttäjänKäyttöoikeudet(user),
    )
  }
}

object StorableSession {
  def apply(session: KoskiSpecificSession): StorableSession = {
    StorableSession(
      oid = session.oid,
      username =  session.username,
      name = session.user.name,
      lang = session.lang,
      clientIp = session.clientIp.getHostAddress,
      userAgent = session.userAgent,
    )
  }
}
