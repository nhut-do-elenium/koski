package fi.oph.koski.fixture

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{KoskiDatabase, KoskiDatabaseConfig}
import fi.oph.koski.fixture.Fixtures._
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusRepository
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.henkilo.authenticationservice.MockAuthenticationServiceClient
import fi.oph.koski.localization.MockLocalizationRepository
import fi.oph.koski.util.Timing
import fi.oph.koski.validation.KoskiValidator

object Fixtures {
  def shouldUseFixtures(config: Config) = {
    if (config.hasPath("fixtures.use")) {
      config.getBoolean("fixtures.use")
    } else {
      KoskiDatabaseConfig(config).isLocal && config.getString("opintopolku.virkailija.url") == "mock"
    }
  }
}

class FixtureCreator(application: KoskiApplication) extends Logging with Timing {
  private val databaseFixtures = new KoskiDatabaseFixtureCreator(application)

  def resetFixtures = if(shouldUseFixtures(application.config)) {
    application.cacheManager.invalidateAllCaches
    databaseFixtures.resetFixtures
    application.henkilöRepository.opintopolku.henkilöPalveluClient.asInstanceOf[MockAuthenticationServiceClient].resetFixtures
    application.localizationRepository.asInstanceOf[MockLocalizationRepository].reset
    application.tiedonsiirtoService.deleteAll
    logger.info("Reset application fixtures")
  }
}