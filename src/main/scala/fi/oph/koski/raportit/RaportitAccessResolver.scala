package fi.oph.koski.raportit

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.raportointikanta.RaportointiDatabase
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema._

import scala.collection.JavaConverters._

object RaportitAccessResolver {
  def apply(application: KoskiApplication): RaportitAccessResolver = {
    RaportitAccessResolver(application.organisaatioRepository, application.raportointiDatabase, application.config)
  }
}

case class RaportitAccessResolver(organisaatioRepository: OrganisaatioRepository, raportointiDatabase: RaportointiDatabase, config: Config) {

  def kyselyOiditOrganisaatiolle(organisaatioOid: Organisaatio.Oid): Set[Organisaatio.Oid] = {
    organisaatioRepository.getOrganisaatio(organisaatioOid)
      .flatMap(organisaatioWithOid => organisaatioRepository.getChildOids(organisaatioWithOid.oid))
      .getOrElse(Set.empty[Oid])
  }

  def mahdollisetRaporttienTyypitOrganisaatiolle(organisaatioOid: Organisaatio.Oid)(implicit session: KoskiSession): Set[RaportinTyyppi] = {
    val organisaatio = organisaatioRepository.getOrganisaatio(organisaatioOid)
    val isKoulutustoimija = organisaatio.map(_.isInstanceOf[Koulutustoimija]).getOrElse(false)

    organisaatio
      .flatMap(organisaatioWithOid => organisaatioRepository.getChildOids(organisaatioWithOid.oid))
      .map(raportointiDatabase.oppilaitostenKoulutusmuodot)
      .map(_.flatMap(raportinTyypitKoulutusmuodolle(_, isKoulutustoimija)))
      .map(_.filter(checkRaporttiAccessIfAccessIsLimited(_)))
      .map(_.filter(raportti => session.allowedOpiskeluoikeusTyypit.contains(raportti.opiskeluoikeudenTyyppi)))
      .getOrElse(Set.empty[RaportinTyyppi])
  }

  private def raportinTyypitKoulutusmuodolle(koulutusmuoto: String, isKoulutustoimija: Boolean) = koulutusmuoto match {
    case "ammatillinenkoulutus" if !isKoulutustoimija => Seq(
      AmmatillinenOpiskelijavuositiedot,
      AmmatillinenTutkintoSuoritustietojenTarkistus,
      AmmatillinenOsittainenSuoritustietojenTarkistus,
      MuuAmmatillinenKoulutus,
      TOPKSAmmatillinen
    )
    case "perusopetus" => Seq(PerusopetuksenVuosiluokka, PerusopetuksenOppijaMääräRaportti)
    case "perusopetuksenlisaopetus" => Seq(PerusopetuksenLisäopetuksenOppijaMääräRaportti)
    case "lukiokoulutus" if !isKoulutustoimija => Seq(LukionSuoritustietojenTarkistus, LukioDiaIbInternationalOpiskelijamaarat, LukioKurssikertyma)
    case "lukiokoulutus" => Seq(LukioDiaIbInternationalOpiskelijamaarat, LukioKurssikertyma)
    case "ibtutkinto" => Seq(LukioDiaIbInternationalOpiskelijamaarat)
    case "diatutkinto" => Seq(LukioDiaIbInternationalOpiskelijamaarat)
    case "internationalschool" => Seq(LukioDiaIbInternationalOpiskelijamaarat)
    case "esiopetus" => Seq(EsiopetuksenRaportti, EsiopetuksenOppijaMäärienRaportti)
    case "aikuistenperusopetus" if !isKoulutustoimija => Seq(AikuistenPerusopetusSuoritustietojenTarkistus, AikuistenPerusopetusOppijaMäärienRaportti, AikuistenPerusopetusKurssikertymänRaportti)
    case "aikuistenperusopetus" => Seq(AikuistenPerusopetusOppijaMäärienRaportti, AikuistenPerusopetusKurssikertymänRaportti)
    case "luva" => Seq(LuvaOpiskelijamaarat)
    case _ => Seq.empty[RaportinTyyppi]
  }

  private def checkRaporttiAccessIfAccessIsLimited(raportti: RaportinTyyppi)(implicit session: KoskiSession) = {
    val rajatutRaportit = config.getConfigList("raportit.rajatut")
    val conf = rajatutRaportit.asScala.find(_.getString("name") == raportti.toString)
    conf match {
      case Some(c) => c.getStringList("whitelist").contains(session.oid)
      case _ => true
    }
  }
}

