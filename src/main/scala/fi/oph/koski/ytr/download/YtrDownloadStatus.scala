package fi.oph.koski.ytr.download

import fi.oph.koski.db.{DB, DatabaseExecutionContext, KoskiTables, QueryMethods, YtrDownloadStatusRow}
import fi.oph.koski.log.Logging
import org.json4s.{DefaultFormats, JValue}
import org.json4s.jackson.JsonMethods

import java.sql.Timestamp
import java.time.LocalDateTime
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import org.json4s._
import slick.jdbc.GetResult

class YtrDownloadStatus(val db: DB) extends QueryMethods with Logging with DatabaseExecutionContext{
  implicit val formats = DefaultFormats

  private val tietokantaStatusRivinNimi = "ytr_download"

  def isLoading: Boolean = getDownloadStatus == "loading"
  def isComplete: Boolean = getDownloadStatus == "complete"
  def setLoading(totalCount: Int, errorCount: Int = 0) = setStatus("loading", totalCount, errorCount)
  def setComplete(totalCount: Int, errorCount: Int = 0) = setStatus("complete", totalCount, errorCount)
  def setError(totalCount: Int, errorCount: Int = 0) = setStatus("error", totalCount, errorCount)

  private def getDownloadStatus: String = {
    (getDownloadStatusJson \ "current" \ "status").extract[String]
  }

  def getDownloadStatusJson: JValue = {
    runDbSync(KoskiTables.YtrDownloadStatus.filter(_.nimi === tietokantaStatusRivinNimi).result).headOption.map(_.data)
      .getOrElse(constructStatusJson("idle", None, 0, 0))
  }

  def getReplayLagSeconds: Int = {
    runDbSync(
      sql"""
        select extract(epoch from replay_lag) as replay_lag from pg_stat_replication;
      """.as[Double](GetResult(_.nextDouble))
    ).headOption.map(_.toInt).getOrElse(0)
  }

  private def setStatus(currentStatus: String, totalCount: Int, errorCount: Int = 0) = {
    runDbSync(KoskiTables.YtrDownloadStatus.insertOrUpdate(
      YtrDownloadStatusRow(
        tietokantaStatusRivinNimi,
        Timestamp.valueOf(LocalDateTime.now),
        constructStatusJson(currentStatus, Some(LocalDateTime.now), totalCount, errorCount)
      )
    ))
  }

  private def constructStatusJson(currentStatus: String, timestamp: Option[LocalDateTime], totalCount: Int, errorCount: Int): JValue = {
    val timestampPart = timestamp.map(Timestamp.valueOf).map(t =>
      s"""
         |, "timestamp": "${t.toString}"
         |""".stripMargin).getOrElse("")

    JsonMethods.parse(s"""
                         | {
                         |   "current": {
                         |     "status": "${currentStatus}",
                         |     "totalCount": ${totalCount},
                         |     "errorCount": ${errorCount}
                         |     ${timestampPart}
                         |   }
                         | }""".stripMargin
    )
  }
}
