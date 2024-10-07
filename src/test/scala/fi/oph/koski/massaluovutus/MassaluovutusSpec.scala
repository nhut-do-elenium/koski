package fi.oph.koski.massaluovutus

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSpecificSession, MockUsers, UserWithPassword}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.{MassaluovutusQueryOrganisaationOpiskeluoikeudet, MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv, MassaluovutusQueryOrganisaationOpiskeluoikeudetJson, QueryOrganisaationOpiskeluoikeudetCsvDocumentation}
import fi.oph.koski.massaluovutus.paallekkaisetopiskeluoikeudet.MassaluovutusQueryPaallekkaisetOpiskeluoikeudet
import fi.oph.koski.massaluovutus.suoritusrekisteri.SuoritusrekisteriQuery
import fi.oph.koski.massaluovutus.valintalaskenta.ValintalaskentaQuery
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportit.RaportitService
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.util.Wait
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.scalaschema.Serializer.format
import org.json4s.jackson.JsonMethods
import org.json4s.{JArray, JInt, JNothing, JObject, JValue}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import java.net.URL
import java.sql.Timestamp
import java.time.{Duration, LocalDate, LocalDateTime}
import java.util.UUID

class MassaluovutusSpec extends AnyFreeSpec with KoskiHttpSpec with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {
  val app = KoskiApplicationForTests

  override protected def beforeAll(): Unit = {
    resetFixtures()
  }

  override protected def afterEach(): Unit = {
    Wait.until { !app.massaluovutusService.hasWork }
  }

  "Kyselyiden skedulointi" - {
    val user = defaultUser
    implicit val session: KoskiSpecificSession = user.toKoskiSpecificSession(app.käyttöoikeusRepository)

    def createRunningQuery(worker: String) =
      RunningQuery(
        queryId = UUID.randomUUID().toString,
        userOid = user.oid,
        query = QueryOrganisaationOpiskeluoikeudetCsvDocumentation.example,
        createdAt = LocalDateTime.now().minusMinutes(1),
        startedAt = LocalDateTime.now(),
        worker = worker,
        resultFiles = List.empty,
        session = StorableSession(user).toJson,
        meta = None,
        progress = None,
      )

    "Orpo kysely vapautetaan takaisin jonoon" in {
      withoutRunningQueryScheduler {
        val orphanedQuery = createRunningQuery("dead-worker")

        app.massaluovutusService.addRaw(orphanedQuery)
        app.massaluovutusCleanupScheduler.trigger()

        val query = app.massaluovutusService.get(UUID.fromString(orphanedQuery.queryId))
        query.map(_.state) should equal(Right(QueryState.pending))
      }
    }

    "Toistuvasti orpoutuva kysely merkitään epäonnistuneeksi" in {
      val crashingQuery = createRunningQuery("dead-worker")
        .copy(meta = Some(QueryMeta(restarts = Some(List("1", "2", "3")))))

      app.massaluovutusService.addRaw(crashingQuery)
      app.massaluovutusCleanupScheduler.trigger()

      val query = app.massaluovutusService.get(UUID.fromString(crashingQuery.queryId))
      query.map(_.state) should equal(Right(QueryState.failed))
    }

    "Ajossa olevaa kyselyä ei vapauteta takaisin jonoon" in {
      withoutRunningQueryScheduler {
        val worker = app.ecsMetadata.currentlyRunningKoskiInstances.head
        val runningQuery = createRunningQuery(worker.taskArn)

        app.massaluovutusService.addRaw(runningQuery)
        app.massaluovutusCleanupScheduler.trigger()

        val query = app.massaluovutusService.get(UUID.fromString(runningQuery.queryId))
        query.map(_.state) should equal(Right(QueryState.running))
      }
    }
  }

  "Organisaation opiskeluoikeudet" - {
    "JSON" - {
      val query = MassaluovutusQueryOrganisaationOpiskeluoikeudetJson(
        alkanutAikaisintaan = LocalDate.of(2020, 1, 1),
      )

      "Ei onnistu väärän organisaation tietoihin" in {
        addQuery(query.withOrganisaatioOid(MockOrganisaatiot.jyväskylänNormaalikoulu), MockUsers.helsinkiKatselija) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Ei onnistu, jos organisaatiota ei ole annettu, eikä sitä voida päätellä yksiselitteisesti" in {
        addQuery(query, MockUsers.kahdenOrganisaatioPalvelukäyttäjä) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.massaluovutus.eiYksiselitteinenOrganisaatio())
        }
      }

      "Ei onnistu ilman oikeuksia sensitiivisen datan lukemiseen" in {
        addQuery(query.withOrganisaatioOid(MockOrganisaatiot.jyväskylänNormaalikoulu), MockUsers.tallentajaEiLuottamuksellinen) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        AuditLogTester.clearMessages()
        val user = MockUsers.helsinkiKatselija
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryOrganisaationOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 18
        complete.files.foreach(verifyResult(_, user))

        AuditLogTester.verifyAuditLogMessage(Map(
          "operation" -> "OPISKELUOIKEUS_HAKU",
          "target" -> Map(
            "hakuEhto" -> "alkanutAikaisintaan=2020-01-01&organisaatio=1.2.246.562.10.346830761110",
          ),
        ))
      }

      "Toisen käyttäjän kyselyn tietoja ei voi hakea" in {
        val queryId = addQuerySuccessfully(query, MockUsers.helsinkiKatselija)(_.queryId)
        getQuery(queryId, MockUsers.jyväskylänKatselijaEsiopetus) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
      }

      "Esiopetuksen lukuoikeuksilla saa vain esiopetuksen opiskeluoikeudet" in {
        val user = MockUsers.esiopetusTallentaja
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryOrganisaationOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
      }
    }

    "CSV" - {
      val query = MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv(
        alkanutAikaisintaan = LocalDate.of(2020, 1, 1),
      )

      "Ei onnistu väärän organisaation tietoihin" in {
        addQuery(query.withOrganisaatioOid(MockOrganisaatiot.jyväskylänNormaalikoulu), MockUsers.helsinkiKatselija) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Ei onnistu, jos organisaatiota ei ole annettu, eikä sitä voida päätellä yksiselitteisesti" in {
        addQuery(query, MockUsers.kahdenOrganisaatioPalvelukäyttäjä) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.massaluovutus.eiYksiselitteinenOrganisaatio())
        }
      }

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        AuditLogTester.clearMessages()
        val user = MockUsers.helsinkiKatselija
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryOrganisaationOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 5
        complete.files.foreach(verifyResult(_, user))

        AuditLogTester.verifyAuditLogMessage(Map(
          "operation" -> "OPISKELUOIKEUS_HAKU",
          "target" -> Map(
            "hakuEhto" -> "alkanutAikaisintaan=2020-01-01&organisaatio=1.2.246.562.10.346830761110",
          ),
        ))
      }

      "Toisen käyttäjän kyselyn tietoja ei voi hakea" in {
        val queryId = addQuerySuccessfully(query, MockUsers.helsinkiKatselija)(_.queryId)
        getQuery(queryId, MockUsers.jyväskylänKatselijaEsiopetus) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
      }

      "Esiopetuksen lukuoikeuksilla saa vain esiopetuksen opiskeluoikeudet" in {
        val user = MockUsers.esiopetusTallentaja
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryOrganisaationOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 3
        complete.files.foreach(verifyResult(_, user))
      }
    }
  }

  "Päällekkäiset opiskeluoikeudet" - {
    "CSV" - {
      val query = MassaluovutusQueryPaallekkaisetOpiskeluoikeudet(
        format = QueryFormat.csv,
        alku = LocalDate.of(2000, 1, 1),
        loppu = LocalDate.of(2020, 1, 1),
      )

      "Ei onnistu väärän organisaation tietoihin" in {
        addQuery(query.copy(organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)), MockUsers.helsinkiKatselija) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Ei onnistu, jos organisaatiota ei ole annettu, eikä sitä voida päätellä yksiselitteisesti" in {
        addQuery(query, MockUsers.kahdenOrganisaatioPalvelukäyttäjä) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.massaluovutus.eiYksiselitteinenOrganisaatio())
        }
      }

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        AuditLogTester.clearMessages()
        val user = MockUsers.helsinkiKatselija
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryPaallekkaisetOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        AuditLogTester.verifyAuditLogMessage(Map(
          "operation" -> "OPISKELUOIKEUS_RAPORTTI",
          "target" -> Map(
            "hakuEhto" -> "alku=2000-01-01&lang=fi&loppu=2020-01-01&oppilaitosOid=1.2.246.562.10.346830761110&raportti=paallekkaisetopiskeluoikeudet",
          ),
        ))
      }

      "Toisen käyttäjän kyselyn tietoja ei voi hakea" in {
        val queryId = addQuerySuccessfully(query, MockUsers.helsinkiKatselija)(_.queryId)
        getQuery(queryId, MockUsers.jyväskylänKatselijaEsiopetus) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
      }
    }

    "Spreadsheet" - {
      val query = MassaluovutusQueryPaallekkaisetOpiskeluoikeudet(
        format = QueryFormat.xlsx,
        alku = LocalDate.of(2000, 1, 1),
        loppu = LocalDate.of(2020, 1, 1),
      )

      "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
        val user = MockUsers.helsinkiKatselija
        val queryId = addQuerySuccessfully(query, user) { response =>
          response.status should equal(QueryState.pending)
          response.query.asInstanceOf[MassaluovutusQueryPaallekkaisetOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        complete.files should have length 1
        complete.files.foreach(verifyResult(_, user))

        val raportitService = new RaportitService(app)
        complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
      }
    }
  }

  "Valintalaskenta" - {
    val user = MockUsers.paakayttaja
    val ammattikoululainen = "1.2.246.562.24.00000000001"
    val olematon = "1.2.246.562.25.1010101010101"

    def getQuery(oid: String, rajapäivä: LocalDate = LocalDate.now()) =  ValintalaskentaQuery(
      rajapäivä = rajapäivä,
      oppijaOids = List(oid)
    )

    "Kysely ei palauta vastausta, jos oppijasta ei ole tietoja" in {
      val queryId = addQuerySuccessfully(getQuery(olematon), user)(_.queryId)
      val complete = waitForCompletion(queryId, user)
      complete.files should have length 0
    }

    "Kysely hakee viimeisimmän opiskeluoikeusversion" in {
      val queryId = addQuerySuccessfully(getQuery(ammattikoululainen), user) { response =>
        response.status should equal(QueryState.pending)
        response.query.asInstanceOf[ValintalaskentaQuery].koulutusmuoto should equal(Some("ammatillinenkoulutus"))
        response.queryId
      }
      val complete = waitForCompletion(queryId, user)

      complete.files should have length 1
      complete.files.foreach(verifyResult(_, user))
    }

    "Kysely hakee opiskeluoikeusversion historiasta" in {
      val db = KoskiApplicationForTests.masterDatabase.db
      val id = QueryMethods.runDbSync(db, sql"""
        UPDATE opiskeluoikeus
        SET versionumero = 2
        WHERE oppija_oid = '1.2.246.562.24.00000000001'
          AND koulutusmuoto = 'ammatillinenkoulutus'
        RETURNING id
      """.as[Int]).head

      QueryMethods.runDbSync(db, sql"""
        INSERT INTO opiskeluoikeushistoria
          ("opiskeluoikeus_id", "aikaleima", "kayttaja_oid", "muutos", "versionumero")
          VALUES(
            $id,
            ${Timestamp.valueOf(LocalDateTime.now().plusDays(1))},
            '1.2.246.562.10.00000000001',
            '[]',
            2
          )
      """.asUpdate)

      val queryId = addQuerySuccessfully(getQuery(ammattikoululainen), user) { response =>
        response.status should equal(QueryState.pending)
        response.query.asInstanceOf[ValintalaskentaQuery].koulutusmuoto should equal(Some("ammatillinenkoulutus"))
        response.queryId
      }
      val complete = waitForCompletion(queryId, user)

      complete.files should have length 1
      verifyResultAndContent(complete.files.head, user) {
        val json = JsonMethods.parse(response.body)
        (json \ "opiskeluoikeudet")(0) \ "versionumero" should equal(JInt(1))
      }
    }

    "Kysely ei palauta opiskeluoikeutta, jos rajapäivä on sitä ennen" in {
      val eilen = LocalDate.now().minusDays(1)
      val queryId = addQuerySuccessfully(getQuery(ammattikoululainen, eilen), user)(_.queryId)
      val complete = waitForCompletion(queryId, user)
      complete.files should have length 0
    }

    "Duplikaattioidit siivotaan kyselystä pois" in {
      val query = ValintalaskentaQuery(
        rajapäivä = LocalDate.now(),
        oppijaOids = List(ammattikoululainen, olematon, olematon, ammattikoululainen)
      )

      val storedQuery = addQuerySuccessfully(query, user) { response =>
        response.status should equal(QueryState.pending)
        response.query.asInstanceOf[ValintalaskentaQuery]
      }

      storedQuery.oppijaOids should equal(List(ammattikoululainen, olematon))
    }
  }

  "Sure" - {
    val user = MockUsers.paakayttaja

    def getQuery(muuttuneetJälkeen: LocalDateTime) = SuoritusrekisteriQuery(
      muuttuneetJälkeen = muuttuneetJälkeen
    )

    "Käyttöoikeudet ja logitus" - {
      val query = getQuery(LocalDateTime.now().plusHours(1))

      "Kyselyä voi käyttää pääkäyttäjänä" in {
        addQuerySuccessfully(query, MockUsers.paakayttaja) {
          _.status should equal(QueryState.pending)
        }
      }

      "Kyselyä voi käyttää oph-palvelukäyttäjänä" in {
        addQuerySuccessfully(query, MockUsers.ophkatselija) {
          _.status should equal(QueryState.pending)
        }
      }

      "Kyselyä ei voi käyttää muuna palvelukäyttäjänä" in {
        addQuery(query, MockUsers.omniaPalvelukäyttäjä) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Kyselystä jää audit log -merkinnät" in {
        AuditLogTester.clearMessages()
        val queryId = addQuerySuccessfully(getQuery(LocalDateTime.now().minusHours(1)), user) { response =>
          response.status should equal(QueryState.pending)
          response.queryId
        }
        val complete = waitForCompletion(queryId, user)

        val (oppijaOid, opiskeluoikeusOid) = verifyResultAndContent(complete.files.last, user) {
          val json = JsonMethods.parse(response.body)
          (
            (json \ "oppijaOid").extract[String],
            (json \ "opiskeluoikeus" \ "oid").extract[String],
          )
        }

        AuditLogTester.verifyAuditLogMessage(Map(
          "operation" -> "SUORITUSREKISTERI_OPISKELUOIKEUS_HAKU",
          "target" -> Map(
            "oppijaHenkiloOid" -> oppijaOid,
            "opiskeluoikeusOid" -> opiskeluoikeusOid,
          ),
        ))

      }
    }

    "Palautuneen datan filtteröinti" - {
      val query = getQuery(LocalDateTime.now().minusHours(1))
      val queryId = addQuerySuccessfully(query, user) { response =>
        response.status should equal(QueryState.pending)
        response.queryId
      }
      val complete = waitForCompletion(queryId, user)

      val jsonFiles = complete.files.map { file =>
        verifyResultAndContent(file, user) {
          JsonMethods.parse(response.body)
        }
      }

      def getOpiskeluoikeudet(tyyppi: Option[String] = None): List[JValue] = {
        val oos = jsonFiles.map(_ \ "opiskeluoikeus")
        tyyppi match {
          case Some(t) => oos.filter(oo => (oo \ "tyyppi" \ "koodiarvo").extract[String] == t)
          case None => oos
        }
      }

      def getSuoritukset(ooTyyppi: Option[String] = None): List[JValue] =
        getOpiskeluoikeudet(ooTyyppi)
          .map(_ \ "suoritukset")
          .collect { case JArray(list) => list }
          .flatten

      def extractStrings(values: List[JValue], path: JValue => JValue): List[String] =
        values
          .map(path(_).extract[String])
          .distinct
          .sorted

      def tyyppi(v: JValue) = v \ "tyyppi" \ "koodiarvo"
      def suorituksenTunniste(v: JValue) = v \ "koulutusmoduuli" \ "tunniste" \ "koodiarvo"
      def viimeisinTila(v: JValue) = v \ "tila" \ "opiskeluoikeusjaksot" \ "tila" \ "koodiarvo" match {
        case JArray(list) => list.last
        case _ => JNothing
      }

      "Sisältää kaikkia tuettuja suoritustyyppejä" in {
        extractStrings(
          getSuoritukset(),
          tyyppi
        ) should equal(List(
          "aikuistenperusopetuksenoppimaara",
          "ammatillinentutkinto",
          "diatutkintovaihe",
          "ebtutkinto",
          "ibtutkinto",
          "internationalschooldiplomavuosiluokka",
          "nuortenperusopetuksenoppiaineenoppimaara",
          "perusopetuksenoppiaineenoppimaara",
          "perusopetuksenoppimaara",
          "perusopetuksenvuosiluokka",
          "telma",
          "tuvakoulutuksensuoritus",
          "vstoppivelvollisillesuunnattukoulutus",
        ))
      }

      "Nuorten perusopetuksesta palautetaan vain perusopetuksen päättötodistus" in {
        extractStrings(
          getSuoritukset(Some("perusopetus")).filterNot(s => tyyppi(s).extract[String] == "nuortenperusopetuksenoppiaineenoppimaara"),
          suorituksenTunniste
        ) should equal(List(
          "201101", // Oppimäärän suoritus
          "9", // Ysiluokka
        ))
      }

      "Ammatillisesta koulutuksesta ei palautetaan vain kokonainen ammatillinen tutkinto ja telma" in {
        extractStrings(
          getSuoritukset(Some("ammatillinenkoulutus")),
          tyyppi
        ) should equal(List(
          "ammatillinentutkinto",
          "telma"
        ))
      }

      "DIA-tutkinnoista palautetaan vain valmistuneet tutkinnot" in {
        extractStrings(
          getOpiskeluoikeudet(Some("diatutkinto")),
          viimeisinTila
        ) should equal(List(
          "valmistunut"
        ))
      }

      "EB-tutkinnoista palautetaan vain valmistuneet tutkinnot" in {
        extractStrings(
          getOpiskeluoikeudet(Some("ebtutkinto")),
          viimeisinTila
        ) should equal(List(
          "valmistunut"
        ))
      }

      "IB-tutkinnoista palautetaan vain valmistuneet tutkinnot" in {
        extractStrings(
          getOpiskeluoikeudet(Some("ibtutkinto")),
          viimeisinTila
        ) should equal(List(
          "valmistunut"
        ))
     }
    }
  }

  "Vihjeet käyttäjälle virhetilanteiden korjaamiseen" - {
    "Liian iso tulostiedosto" in {
      val queryId = UUID.randomUUID().toString
      val failedQuery = FailedQuery(
        queryId = queryId,
        userOid = MockUsers.helsinkiKatselija.oid,
        query = MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv(
          alkanutAikaisintaan = LocalDate.of(2000, 1, 1),
        ),
        createdAt = LocalDateTime.now(),
        startedAt = LocalDateTime.now(),
        finishedAt = LocalDateTime.now(),
        worker = "dummy-1",
        resultFiles = List(),
        error = "Your proposed upload exceeds the maximum allowed size (Service: S3, Status Code: 400, Request ID: XYZ, Extended Request ID: xyz)",
        session = JObject(),
        meta = None,
      )

      KoskiApplicationForTests.massaluovutusService.addRaw(failedQuery)

      getQuerySuccessfully(queryId, MockUsers.helsinkiKatselija) { response =>
        val failResponse = response.asInstanceOf[FailedQueryResponse]
        failResponse.hint should equal(Some("Kyselystä syntyneen tulostiedoston koko kasvoi liian suureksi. Pienennä tulosjoukon kokoa esimerkiksi rajaamalla kysely lyhyemmälle aikavälille."))
      }
    }
  }

  def addQuery[T](query: MassaluovutusQueryParameters, user: UserWithPassword)(f: => T): T =
    post("api/massaluovutus", JsonSerializer.writeWithRoot(query), headers = authHeaders(user) ++ jsonContent)(f)

  def addQuerySuccessfully[T](query: MassaluovutusQueryParameters, user: UserWithPassword)(f: QueryResponse => T): T = {
    addQuery(query, user) {
      f(parsedResponse)
    }
  }

  def getQuery[T](queryId: String, user: UserWithPassword)(f: => T): T =
    get(s"api/massaluovutus/$queryId", headers = authHeaders(user) ++ jsonContent)(f)

  def getQuerySuccessfully[T](queryId: String, user: UserWithPassword)(f: QueryResponse => T): T = {
    getQuery(queryId, user) {
      f(parsedResponse)
    }
  }

  def getResult[T](url: String, user: UserWithPassword)(f: => T): T = {
    val rootUrl = KoskiApplicationForTests.config.getString("koski.root.url")
    get(url.replace(rootUrl, ""), headers = authHeaders(user))(f)
  }

  def verifyResult(url: String, user: UserWithPassword): Unit =
    getResult(url, user) {
      verifyResponseStatus(302) // 302: Found (redirect)
    }

  def verifyResultAndContent[T](url: String, user: UserWithPassword)(f: => T): T = {
    val location = new URL(getResult(url, user) {
      verifyResponseStatus(302) // 302: Found (redirect)
      response.header("Location")
    })
    withBaseUrl(location) {
      get(s"${location.getPath}?${location.getQuery}") {
        verifyResponseStatusOk()
        f
      }
    }
  }

  def waitForStateTransition(queryId: String, user: UserWithPassword)(states: String*): QueryResponse = {
    var lastResponse: Option[QueryResponse] = None
    Wait.until {
      getQuerySuccessfully(queryId, user) { response =>
        states should contain(response.status)
        lastResponse = Some(response)
        response.status == states.last
      }
    }
    lastResponse.get
  }

  def waitForCompletion(queryId: String, user: UserWithPassword): CompleteQueryResponse =
    waitForStateTransition(queryId, user)(QueryState.pending, QueryState.running, QueryState.complete).asInstanceOf[CompleteQueryResponse]

  def parsedResponse: QueryResponse = {
    verifyResponseStatusOk()
    val json = JsonMethods.parse(response.body)
    val result = KoskiApplicationForTests.validatingAndResolvingExtractor.extract[QueryResponse](json, strictDeserialization)
    result should not be Left
    result.right.get
  }

  def withoutRunningQueryScheduler[T](f: => T): T =
    try {
      app.massaluovutusScheduler.pause(Duration.ofDays(1))
      f
    } finally {
      app.massaluovutusService.cancelAllTasks("cancelled")
      app.massaluovutusScheduler.resume()
    }
}
