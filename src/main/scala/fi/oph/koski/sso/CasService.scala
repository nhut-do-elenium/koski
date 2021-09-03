package fi.oph.koski.sso

import com.typesafe.config.Config
import fi.oph.koski.cas.{CasAuthenticationException, CasClient, CasUser}
import fi.oph.koski.cas.CasClient.Username
import fi.oph.koski.http.{Http, OpintopolkuCallerId}
import fi.oph.koski.log.Logging
import fi.oph.koski.userdirectory.Password

import scala.concurrent.duration.DurationInt

class CasService(config: Config) extends Logging {
  private val casVirkailijaClient = new CasClient(
    config.getString("opintopolku.virkailija.url") + "/cas",
    Http.newClient("cas.serviceticketvalidation.virkailija"),
    OpintopolkuCallerId.koski
  )

  private val casOppijaClient = new CasClient(
    config.getString("opintopolku.oppija.url") + "/cas-oppija",
    Http.newClient("cas.serviceticketvalidation.oppija"),
    OpintopolkuCallerId.koski
  )

  private val mockUsernameForAllVirkailijaTickets = {
    if (config.getString("opintopolku.virkailija.url") == "mock" && config.hasPath("mock.casClient.usernameForAllVirkailijaTickets")) {
      Some(config.getString("mock.casClient.usernameForAllVirkailijaTickets"))
    } else {
      None
    }
  }

  def validateKansalainenServiceTicket(url: String, ticket: String): String = {
    val oppijaAttributes = Http.runIO(
      casOppijaClient.validateServiceTicketWithOppijaAttributes(url)(ticket),
      timeout = 10.seconds
    )
    oppijaAttributes("nationalIdentificationNumber")
  }

  def validateVirkailijaServiceTicket(url: String, ticket: String): Username = {
    mockUsernameForAllVirkailijaTickets.getOrElse({
      Http.runIO(
        casVirkailijaClient.validateServiceTicketWithVirkailijaUsername(url)(ticket),
        timeout = 10.seconds
      )
    })
  }
}
