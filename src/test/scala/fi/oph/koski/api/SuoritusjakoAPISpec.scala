package fi.oph.koski.api

import fi.oph.koski.documentation.ExamplesTaiteenPerusopetus.PäätasonSuoritus.Koulutusmoduuli
import fi.oph.koski.documentation.ExamplesTaiteenPerusopetus.varsinaisSuomenKansanopisto
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.{AccessLogTester, AuditLogTester}
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema._
import fi.oph.koski.suoritusjako.{Suoritusjako, SuoritusjakoRequest}
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.nio.charset.StandardCharsets
import scala.collection.mutable

class SuoritusjakoAPISpec extends AnyFreeSpec with SuoritusjakoTestMethods with Matchers with OpiskeluoikeusTestMethodsAmmatillinen with BeforeAndAfterAll {
  val secrets: mutable.Map[String, String] = mutable.Map()

  val hetu = KoskiSpecificMockOppijat.taiteenPerusopetusValmis.hetu.get
  val json =
    s"""[{
        "oppilaitosOid": "${varsinaisSuomenKansanopisto.oid}",
        "suorituksenTyyppi": "${SuorituksenTyyppi.tpoLaajanOppimääränPerusopinnot.koodiarvo}",
        "koulutusmoduulinTunniste": "${Koulutusmoduuli.musiikkiLaajaOppimääräPerusopinnot.tunniste.koodiarvo}"
      }]"""

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    createSuoritusjako(json, hetu) {
      verifyResponseStatusOk()
      secrets += ("taiteen perusopetus" -> JsonSerializer.parse[Suoritusjako](response.body).secret)
    }
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
  }

  "Suoritusjaon hakeminen" - {
    "vanhalla käyttöliittymän rajapinnalla" - {
      "onnistuu" in {
        getSuoritusjako(secrets("taiteen perusopetus")) {
          verifyResponseStatusOk()
        }
      }
    }

    "uudella käyttöliittymän rajapinnalla" - {
      "onnistuu" in {
        postSuoritusjakoV3(secrets("taiteen perusopetus")) {
          verifyResponseStatusOk()
        }
      }

      "ei sisällä hetua" in {
        postSuoritusjakoV3(secrets("taiteen perusopetus")) {
          verifyResponseStatusOk()
          val bodyString = new String(response.bodyBytes, StandardCharsets.UTF_8)
          bodyString should not include(hetu)
        }
      }

      "tuottaa auditlog-merkinnän" in {
        AuditLogTester.clearMessages
        postSuoritusjakoV3(secrets("taiteen perusopetus")) {
          verifyResponseStatusOk()
          AuditLogTester.verifyAuditLogMessage(Map("operation" -> "KANSALAINEN_SUORITUSJAKO_KATSOMINEN"))
        }
      }
    }

    "uudella JSON-rajapinnalla" - {
      "onnistuu" in {
        getSuoritusjakoPublicAPI(secrets("taiteen perusopetus")) {
          verifyResponseStatusOk()
        }
      }

      "sisältää oikean suorituksen eikä muita" in {
        getSuoritusjakoPublicAPI(secrets("taiteen perusopetus")) {
          verifyResponseStatusOk()
          val bodyString = new String(response.bodyBytes, StandardCharsets.UTF_8)

          implicit val context: ExtractionContext = strictDeserialization
          val oppija = SchemaValidatingExtractor.extract[Oppija](bodyString).right.get

          val henkilö = oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot]

          henkilö.sukunimi should be(KoskiSpecificMockOppijat.taiteenPerusopetusValmis.sukunimi)

          oppija.opiskeluoikeudet should have length 1

          val oo = oppija.opiskeluoikeudet(0).asInstanceOf[TaiteenPerusopetuksenOpiskeluoikeus]

          oo.suoritukset should have length 1

          oo.suoritukset(0).koulutusmoduuli.tunniste.koodiarvo should be(Koulutusmoduuli.musiikkiLaajaOppimääräPerusopinnot.tunniste.koodiarvo)
        }
      }

      "ei sisällä hetua" in {
        getSuoritusjakoPublicAPI(secrets("taiteen perusopetus")) {
          verifyResponseStatusOk()
          val bodyString = new String(response.bodyBytes, StandardCharsets.UTF_8)
          bodyString should not include(hetu)
        }
      }

      "tuottaa auditlog-merkinnän" in {
        AuditLogTester.clearMessages
        getSuoritusjakoPublicAPI(secrets("taiteen perusopetus")) {
          verifyResponseStatusOk()
          AuditLogTester.verifyAuditLogMessage(Map("operation" -> "KANSALAINEN_SUORITUSJAKO_KATSOMINEN"))
        }
      }

      "salaisuus ei päädy lokiin" in {
        AccessLogTester.clearMessages
        val secret = secrets("taiteen perusopetus")
        val maskedSecret = secret.take(8) + "*" * (32 - 8)
        getSuoritusjakoPublicAPI(secret) {
          verifyResponseStatusOk()
          AccessLogTester.getLatestMatchingAccessLog("/koski/api/opinnot") should include(maskedSecret)
        }
      }
    }
  }

  def postSuoritusjakoV3[A](secret: String)(f: => A): A = {
    post("/api/suoritusjakoV3/", JsonSerializer.writeWithRoot(SuoritusjakoRequest(secret)), headers = jsonContent)(f)
  }

  def getSuoritusjakoPublicAPI[A](secret: String)(f: => A): A = {
    get(s"/api/opinnot/${secret}", headers = jsonContent)(f)
  }
}
