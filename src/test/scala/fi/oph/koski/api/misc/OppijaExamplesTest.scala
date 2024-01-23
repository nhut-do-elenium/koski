package fi.oph.koski.api.misc

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.Examples.oppijaExamples
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Henkilö, HenkilöWithOid, UusiHenkilö}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OppijaExamplesTest extends AnyFreeSpec with Matchers with KoskiHttpSpec with Logging with BeforeAndAfterAll with OpiskeluoikeusTestMethods with SearchTestMethods {
  override def beforeAll(): Unit = {
    super.beforeAll()
    resetFixtures()
  }

  "API examples" - {
    oppijaExamples.foreach { example =>
      "PUT " + example.name in {
        val body = JsonSerializer.writeWithRoot(example.data).getBytes("utf-8")
        mitätöiOppijanKaikkiOpiskeluoikeudet(example.data.henkilö)
        put("api/oppija", body = body, headers = authHeaders() ++ jsonContent) {
          verifyResponseStatusOk(example.statusCode)
          logger.info(example.name + ": OK")
        }
      }
    }
  }
}
