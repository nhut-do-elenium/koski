package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.{LocalizationRepository, LocalizedString}
import fi.oph.koski.localization.LocalizedString._
import fi.oph.scalaschema.annotation._
import fi.oph.koski.localization.LocalizedStringImplicits._

@Description("Ammatillisen koulutuksen opiskeluoikeus")
case class AmmatillinenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  override val sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None,
  alkamispäivä: Option[LocalDate] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
  päättymispäivä: Option[LocalDate] = None,
  tila: AmmatillinenOpiskeluoikeudenTila,
  suoritukset: List[AmmatillinenPäätasonSuoritus],
  lisätiedot: Option[AmmatillisenOpiskeluoikeudenLisätiedot] = None,
  @KoodistoKoodiarvo("ammatillinenkoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillinenkoulutus", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def withSuoritukset(suoritukset: List[PäätasonSuoritus]) = copy(suoritukset = suoritukset.asInstanceOf[List[AmmatillinenPäätasonSuoritus]])
}

sealed trait AmmatillinenPäätasonSuoritus extends PäätasonSuoritus with Työssäoppimisjaksollinen

trait Työssäoppimisjaksollinen {
  @Description("Tutkinnon suoritukseen kuuluvat työssäoppimisjaksot")
  def työssäoppimisjaksot: Option[List[Työssäoppimisjakso]]
}

case class AmmatillisenOpiskeluoikeudenLisätiedot(
  @Description("Jos kyseessä erityisopiskelija, jolle on tehty henkilökohtainen opetuksen järjestämistä koskeva suunnitelma (hojks), täytetään tämä tieto. Kentän puuttuminen tai null-arvo tulkitaan siten, että suunnitelmaa ei ole tehty.")
  hojks: Option[Hojks],
  oikeusMaksuttomaanAsuntolapaikkaan: Boolean = false,
  @Description("Opintoihin liittyvien ulkomaanjaksojen tiedot")
  ulkomaanjaksot: Option[List[Ulkomaanjakso]] = None,
  @DefaultValue(false)
  vaikeastiVammainen: Boolean = false,
  @DefaultValue(false)
  vammainenJaAvustaja: Boolean = false,
  majoitus: Option[List[Majoitusjakso]] = None,
  sisäoppilaitosmainenMajoitus: Option[List[Majoitusjakso]] = None,
  vaativanErityisenTuenYhteydessäJärjestettäväMajoitus: Option[List[Majoitusjakso]] = None,
  @DefaultValue(false)
  henkilöstökoulutus: Boolean = false,
  @DefaultValue(false)
  vankilaopetuksessa: Boolean = false,
  @MinValueExclusive(0)
  @MaxValueExclusive(100)
  @UnitOfMeasure("%")
  @Title("Osa-aikaisuus")
  osaAikaisuus: Option[Int] = None,
  @Description("Yli 4 viikon poissaolot")
  poissaolojaksot: Option[List[Poissaolojakso]] = None
) extends OpiskeluoikeudenLisätiedot

case class Majoitusjakso (
  alku: LocalDate,
  loppu: Option[LocalDate]
) extends Jakso


case class Poissaolojakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @KoodistoUri("ammatillistenopintojenpoissaolonsyy")
  syy: Koodistokoodiviite
) extends Jakso

@Description("Ammatillisen opiskeluoikeuden tilat:\n- läsnä: opiskelijan opiskelu-oikeus on voimassa (Ammatillisesta peruskoulutuksesta annetun lain 34 §:n mukaan opiskelijan tulee osallistua opetukseen, jollei hänelle ole myönnetty siitä vapautusta. lukuvuosi alkaa 1.8. Opintotukilainsäädännössä määrätty. Ammatillisesta aikuiskoulutuksesta annetun lain 11 §:n (952/2011) 1 momentin 13 kohdan mukaan ammatilliseen aikuiskoulutukseen sovelletaan ammatillisesta peruskoulutuksesta annetun lain 34 §:n säännöksiä opiskelijan velvollisuuksista. Ammatillisesta peruskoulutuksesta annetun lain 34 §:n 1 momentin mukaan opiskelijan tulee osallistua opetukseen, jollei hänelle ole myönnetty siitä vapautusta. Mikäli opiskelija on hakenut opiskeluoikeuden palauttamista (Laki ammatillisesta peruskoulutuksesta (630/1998), 32 b §.) on opiskelijan tilana tuolloin läsnä.) \n - väliaikainen keskeytyminen: Ammatillisesta peruskoulutuksesta annetun lain 34 §:n mukaan opiskelijan tulee osallistua opetukseen, jollei hänelle ole myönnetty siitä vapautusta. Opiskelija voi myös väliaikaisesti keskeyttää koulutuksen, jos hänelle on myönnetty vapautus opetukseen osallistumisesta. Väliaikaisella keskeyttämisellä tarkoitettaisiin käytännössä tilanteita, joissa opiskelija on luvallisesti poissa koulutuksesta tai opiskelija on luvatta koulutuksesta tai opiskelija on erotettu määräajaksi (pidätetty määräajaksi poissa koulutuksesta). Määräaikainen erottaminen voi olla maksimissaan 12 kk. (35 § 5 mom.) Opetushallituksella voi määrätä tietojen sisällöstä 12 § mukaisest (laki ammatillisesta peruskoulutuksesta 34 §) opiskelija on erotettu koulutuksen järjestäjän toimesta (kurinpidolliset menettelyt) Keskeyttäminen voi tulla kyseeseen opiskelijan omasta ilmoituksesta. Opiskelija on keskeyttäynyt opiskelunsa väliaikaisesti omasta ilmoituksesta (Opintotukilaki 25 a) \n - valmistunut: opiskelijan opiskelu-oikeus on päättynyt (tutkinto on valmis) (Ammatillisesta peruiskoulutuksesta annetun lain (630/1998) 31 §:ssä (246/2015) säädetään opiskeluajasta. Ammatillinen perustutkinto tulee suorittaa enintään yhtä vuotta tutkinnon laajuudeksi määriteltyä aikaa pidemmässä ajassa, jollei opiskelijalle perustellusta syystä myönnetä suoritusaikaan pidennystä.) \n- eronnut: opiskelija on eronnut koulutuksesta omasta ilmoituksestaan \n - katsotaan eronneeksi: Ammatillisesta peruskoulutuksesta annetun lain 31 §:n 4 momentin mukaan opiskelija, joka ei ole suorittanut tutkintoa tai koulutusta 1—3 momentissa säädetyssä ajassa, katsotaan eronneeksi. Eronneeksi katsotaan myös sellainen opiskelija, joka pätevää syytä ilmoittamatta on poissa opetuksesta, jos on ilmeistä, ettei hänen tarkoituksenaan ole jatkaa opintoja. Laki ammatillisesta peruskoulutuksesta (630/1998), 31 §. Ammatillisesta aikuiskoulutuksesta annetun lain (631/1998) 11 §:n 1 momentin 8 kohdan (247/2015) mukaan ammatilliseen aikuiskoulutukseen sovelletaan ammatillisesta peruskoulutuksesta annetun lain 31 §:n 4 momentin toisen virkkeen säännöksiä siitä, milloin opiskelija voidaan katsoa eronneeksi. Eronneeksi katsotaan sellainen opiskelija, joka pätevää syytä ilmoittamatta on poissa opetuksesta, jos on ilmeistä, ettei hänen tarkoituksenaan ole jatkaa opintoja. ")
case class AmmatillinenOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[AmmatillinenOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

@Description("Sisältää myös tiedon opintojen rahoituksesta jaksoittain.")
case class AmmatillinenOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: Koodistokoodiviite,
  @Description("Opintojen rahoitus")
  @KoodistoUri("opintojenrahoitus")
  opintojenRahoitus: Option[Koodistokoodiviite] = None
) extends KoskiOpiskeluoikeusjakso

case class NäyttötutkintoonValmistavanKoulutuksenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: NäyttötutkintoonValmistavaKoulutus = NäyttötutkintoonValmistavaKoulutus(),
  @Description("Tässä kentässä kuvataan sen tutkinnon tiedot, joihin valmistava koulutus tähtää")
  tutkinto: AmmatillinenTutkintoKoulutus,
  override val tutkintonimike: Option[List[Koodistokoodiviite]] = None,
  override val osaamisala: Option[List[Koodistokoodiviite]] = None,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  override val alkamispäivä: Option[LocalDate],
  @Description("Suorituksen päättymispäivä. Muoto YYYY-MM-DD")
  val päättymispäivä: Option[LocalDate],
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Koulutuksen järjestämismuoto")
  @OksaUri("tmpOKSAID140", "koulutuksen järjestämismuoto")
  järjestämismuoto: Option[Järjestämismuoto] = None,
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  @Description("Valmistavan koulutuksen osat")
  @Title("Koulutuksen osat")
  override val osasuoritukset: Option[List[NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("nayttotutkintoonvalmistavakoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("nayttotutkintoonvalmistavakoulutus", "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends AmmatillinenPäätasonSuoritus with Toimipisteellinen with Todistus with Arvioinniton with Ryhmällinen with Tutkintonimikkeellinen with Osaamisalallinen

@Description("Näyttötutkintoon valmistavan koulutuksen tunnistetiedot")
case class NäyttötutkintoonValmistavaKoulutus(
  @KoodistoKoodiarvo("999904")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999904", "koulutus")
) extends Koulutus {
  def laajuus = None
}

case class AmmatillisenTutkinnonSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: AmmatillinenTutkintoKoulutus,
  override val tutkintonimike: Option[List[Koodistokoodiviite]] = None,
  override val osaamisala: Option[List[Koodistokoodiviite]] = None,
  toimipiste: OrganisaatioWithOid,
  @Description("Tutkinnon suorituksen tila-tieto kertoo, onko tutkinto valmis, kesken tai keskeytetty. Valmis -tilassa oppija on suorittanut tutkintosuorituksen, ja se vahvistetaan erikseen välittämällä tutkinnon vahvistus-tieto. Kesken -tilassa tutkinnon osan suorituksia puuttuu vielä.")
  tila: Koodistokoodiviite,
  override val alkamispäivä: Option[LocalDate] = None,
  vahvistus: Option[Henkilövahvistus] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Tutkinnon suoritustapa (näyttö / ops). Ammatillisen perustutkinnon voi suorittaa joko opetussuunnitelmaperusteisesti tai näyttönä. Ammattitutkinnot ja erikoisammattitutkinnot suoritetaan aina näyttönä.")
  @OksaUri("tmpOKSAID141", "ammatillisen koulutuksen järjestämistapa")
  @KoodistoUri("ammatillisentutkinnonsuoritustapa")
  suoritustapa: Option[Koodistokoodiviite] = None,
  @Description("Koulutuksen järjestämismuoto. Oppilaitosmuotoinen tai - oppisopimuskoulutus.")
  @OksaUri("tmpOKSAID140", "koulutuksen järjestämismuoto")
  järjestämismuoto: Option[Järjestämismuoto] = None,
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  @Description("Ammatilliseen tutkintoon liittyvät tutkinnonosan suoritukset")
  @Title("Tutkinnon osat")
  override val osasuoritukset: Option[List[AmmatillisenTutkinnonOsanSuoritus]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("ammatillinentutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillinentutkinto", "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends AmmatillinenPäätasonSuoritus with Toimipisteellinen with Todistus with Arvioinniton with Ryhmällinen with Tutkintonimikkeellinen with Osaamisalallinen

@Description("Oppija suorittaa yhtä tai useampaa tutkinnon osaa, eikä koko tutkintoa.")
case class AmmatillisenTutkinnonOsittainenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: AmmatillinenTutkintoKoulutus,
  override val tutkintonimike: Option[List[Koodistokoodiviite]] = None,
  @Description("Onko kyse uuden tutkintonimikkeen suorituksesta, liittyen aiemmin suoritettuun tutkintoon")
  @DefaultValue(false)
  toinenTutkintonimike: Boolean = false,
  override val osaamisala: Option[List[Koodistokoodiviite]] = None,
  @Description("Onko kyse uuden osaamisalan suorituksesta, liittyen aiemmin suoritettuun tutkintoon")
  @DefaultValue(false)
  toinenOsaamisala: Boolean = false,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  override val alkamispäivä: Option[LocalDate] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Koulutuksen järjestämismuoto")
  @OksaUri("tmpOKSAID140", "koulutuksen järjestämismuoto")
  järjestämismuoto: Option[Järjestämismuoto] = None,
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  @Description("Ammatilliseen tutkintoon liittyvät tutkinnonosan suoritukset")
  @Title("Tutkinnon osat")
  override val osasuoritukset: Option[List[AmmatillisenTutkinnonOsanSuoritus]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("ammatillinentutkintoosittainen")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillinentutkintoosittainen", "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends AmmatillinenPäätasonSuoritus with Toimipisteellinen with VahvistuksetonSuoritus with Arvioinniton with Ryhmällinen with Tutkintonimikkeellinen with Osaamisalallinen

trait AmmatillisenTutkinnonOsanSuoritus extends Suoritus with Työssäoppimisjaksollinen {
  @Description("Suoritettavan tutkinnon osan tunnistetiedot")
  @Title("Tutkinnon osa")
  @Discriminator
  def koulutusmoduuli: AmmatillisenTutkinnonOsa
  @Description("Tutkinto, jonka rakenteeseen tutkinnon osa liittyy. Käytetään vain tapauksissa, joissa tutkinnon osa on poimittu toisesta tutkinnosta.")
  def tutkinto: Option[AmmatillinenTutkintoKoulutus]
  @KoodistoUri("ammatillisentutkinnonosanryhma")
  def tutkinnonOsanRyhmä: Option[Koodistokoodiviite]
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  @Title("Oppilaitos / toimipiste")
  def toimipiste: Option[OrganisaatioWithOid]
  def tila: Koodistokoodiviite
  def arviointi: Option[List[AmmatillinenArviointi]]
  def vahvistus: Option[HenkilövahvistusValinnaisellaTittelillä]
  def alkamispäivä: Option[LocalDate]
  @Description("Jos tutkinnon osa on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot")
  @ComplexObject
  def tunnustettu: Option[OsaamisenTunnustaminen]
  def lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]
  def suorituskieli: Option[Koodistokoodiviite]
  @KoodistoKoodiarvo("ammatillisentutkinnonosa")
  def tyyppi: Koodistokoodiviite
  def toimipisteellä(toimipiste: OrganisaatioWithOid): AmmatillisenTutkinnonOsanSuoritus
}

@Title("Yhteisen tutkinnon osan suoritus")
case class YhteisenAmmatillisenTutkinnonOsanSuoritus(
  koulutusmoduuli: YhteinenTutkinnonOsa,
  tutkinto: Option[AmmatillinenTutkintoKoulutus] = None,
  tutkinnonOsanRyhmä: Option[Koodistokoodiviite] = None,
  toimipiste: Option[OrganisaatioWithOid],
  tila: Koodistokoodiviite,
  arviointi: Option[List[AmmatillinenArviointi]] = None,
  vahvistus: Option[HenkilövahvistusValinnaisellaTittelillä] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  näyttö: Option[Näyttö] = None,
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  @Title("Osa-alueet")
  override val osasuoritukset: Option[List[AmmatillisenTutkinnonOsanOsaAlueenSuoritus]] = None,
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillisentutkinnonosa", koodistoUri = "suorituksentyyppi")
) extends AmmatillisenTutkinnonOsanSuoritus {
  def toimipisteellä(toimipiste: OrganisaatioWithOid) = copy(toimipiste = Some(toimipiste))
}


@Title("Muun tutkinnon osan suoritus")
case class MuunAmmatillisenTutkinnonOsanSuoritus(
  koulutusmoduuli: AmmatillisenTutkinnonOsa,
  tutkinto: Option[AmmatillinenTutkintoKoulutus] = None,
  tutkinnonOsanRyhmä: Option[Koodistokoodiviite] = None,
  toimipiste: Option[OrganisaatioWithOid],
  tila: Koodistokoodiviite,
  arviointi: Option[List[AmmatillinenArviointi]] = None,
  vahvistus: Option[HenkilövahvistusValinnaisellaTittelillä] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Suoritukseen liittyvän näytön tiedot")
  @ComplexObject
  näyttö: Option[Näyttö] = None,
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  override val osasuoritukset: Option[List[AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus]] = None,
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillisentutkinnonosa", koodistoUri = "suorituksentyyppi")
) extends AmmatillisenTutkinnonOsanSuoritus {
  def toimipisteellä(toimipiste: OrganisaatioWithOid) = copy(toimipiste = Some(toimipiste))
}

@Description("Työssäoppimisjakson tiedot (aika, paikka, työtehtävät, laajuus)")
case class Työssäoppimisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @Description("Työssäoppimispaikan nimi")
  työssäoppimispaikka: Option[LocalizedString],
  @KoodistoUri("kunta")
  @Description("Kunta, jossa työssäoppiminen on tapahtunut")
  paikkakunta: Koodistokoodiviite,
  @Description("Maa, jossa työssäoppiminen on tapahtunut")
  @KoodistoUri("maatjavaltiot2")
  maa: Koodistokoodiviite,
  @Description("Työtehtävien kuvaus")
  työtehtävät: Option[LocalizedString],
  laajuus: LaajuusOsaamispisteissä
) extends Jakso

@Title("Ammatillinen tutkintokoulutus")
@Description("Ammatillisen tutkinnon tunnistetiedot. Ammatillisille koulutuksille on ePerusteet.")
case class AmmatillinenTutkintoKoulutus(
 tunniste: Koodistokoodiviite,
 perusteenDiaarinumero: Option[String]
) extends DiaarinumerollinenKoulutus {
  override def laajuus = None
  override def isTutkinto = true
}

sealed trait AmmatillisenTutkinnonOsa extends Koulutusmoduuli {
  def laajuus: Option[LaajuusOsaamispisteissä]
  def pakollinen: Boolean
}

object AmmatillisenTutkinnonOsa {
  val yhteisetTutkinnonOsat = List("101053", "101054", "101055", "101056").map(Koodistokoodiviite(_, "tutkinnonosat"))
}

trait ValtakunnallinenTutkinnonOsa extends AmmatillisenTutkinnonOsa with KoodistostaLöytyväKoulutusmoduuli with Valinnaisuus with ValmaKoulutuksenOsa with TelmaKoulutuksenOsa with NäyttötutkintoonValmistavanKoulutuksenOsa {
  @Description("Tutkinnon osan kansallinen koodi")
  @KoodistoUri("tutkinnonosat")
  def tunniste: Koodistokoodiviite
}

@Description("Yhteisen tutkinnon osan tunnistetiedot")
case class YhteinenTutkinnonOsa(
  @KoodistoKoodiarvo("101053")
  @KoodistoKoodiarvo("101054")
  @KoodistoKoodiarvo("101055")
  @KoodistoKoodiarvo("101056")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean,
  override val laajuus: Option[LaajuusOsaamispisteissä]
) extends ValtakunnallinenTutkinnonOsa

@Description("Opetussuunnitelmaan kuuluvan tutkinnon osan tunnistetiedot")
case class MuuValtakunnallinenTutkinnonOsa(
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean,
  override val laajuus: Option[LaajuusOsaamispisteissä]
) extends ValtakunnallinenTutkinnonOsa

@Description("Paikallisen tutkinnon osan tunnistetiedot")
case class PaikallinenTutkinnonOsa(
  tunniste: PaikallinenKoodi,
  @Description("Tutkinnonosan kuvaus sisältäen ammattitaitovaatimukset")
  kuvaus: LocalizedString,
  pakollinen: Boolean,
  override val laajuus: Option[LaajuusOsaamispisteissä]
) extends AmmatillisenTutkinnonOsa with PaikallinenKoulutusmoduuli with Valinnaisuus

@Title("Ammatillisen tutkinnon osaa pienempi kokonaisuus")
@Description("Muiden kuin yhteisten tutkinnon osien osasuoritukset")
case class AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
  @Title("Kokonaisuus")
  koulutusmoduuli: AmmatillisenTutkinnonOsaaPienempiKokonaisuus,
  tila: Koodistokoodiviite,
  arviointi: Option[List[AmmatillinenArviointi]] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  @Description("Jos kokonaisuus on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot")
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("ammatillisentutkinnonosaapienempikokonaisuus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillisentutkinnonosaapienempikokonaisuus", "suorituksentyyppi")
) extends Suoritus with VahvistuksetonSuoritus

@Title("Ammatillisen tutkinnon osan osa-alueen suoritus")
case class AmmatillisenTutkinnonOsanOsaAlueenSuoritus(
  @Title("Osa-alue")
  koulutusmoduuli: AmmatillisenTutkinnonOsanOsaAlue,
  tila: Koodistokoodiviite,
  arviointi: Option[List[AmmatillinenArviointi]] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  @Description("Jos osa-alue on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot")
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("ammatillisentutkinnonosanosaalue")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillisentutkinnonosanosaalue", "suorituksentyyppi")
) extends Suoritus with VahvistuksetonSuoritus

case class AmmatillisenTutkinnonOsaaPienempiKokonaisuus(
  tunniste: PaikallinenKoodi,
  @Description("Opintokokonaisuuden kuvaus")
  kuvaus: LocalizedString,
  laajuus: Option[LaajuusOsaamispisteissä] = None
) extends PaikallinenKoulutusmoduuli

trait AmmatillisenTutkinnonOsanOsaAlue extends Koulutusmoduuli {
  def pakollinen: Boolean
}

@Description("Paikallisen tutkinnon osan osa-alueen tunnistetiedot")
@Title("Paikallinen tutkinnon osan osa-alue")
case class PaikallinenAmmatillisenTutkinnonOsanOsaAlue(
  tunniste: PaikallinenKoodi,
  @Description("Tutkinnonosan osa-alueen kuvaus")
  kuvaus: LocalizedString,
  pakollinen: Boolean,
  laajuus: Option[LaajuusOsaamispisteissä] = None
) extends AmmatillisenTutkinnonOsanOsaAlue with PaikallinenKoulutusmoduuli

@Description("Valtakunnallisen tutkinnon osan osa-alueen tunnistetiedot")
@Title("Valtakunnallinen tutkinnon osan osa-alue")
case class ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(
  @KoodistoUri("ammatillisenoppiaineet")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean,
  laajuus: Option[LaajuusOsaamispisteissä]
) extends AmmatillisenTutkinnonOsanOsaAlue with KoodistostaLöytyväKoulutusmoduuli

@Title("Vieras tai toinen kotimainen kieli")
case class AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli(
  @KoodistoKoodiarvo("VK")
  @KoodistoKoodiarvo("TK1")
  @KoodistoUri("ammatillisenoppiaineet")
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  @Discriminator
  kieli: Koodistokoodiviite,
  pakollinen: Boolean,
  laajuus: Option[LaajuusOsaamispisteissä]
) extends AmmatillisenTutkinnonOsanOsaAlue with KoodistostaLöytyväKoulutusmoduuli{
  override def description(text: LocalizationRepository) = concat(nimi, ", ", kieli)
}

@Title("Äidinkieli")
case class AmmatillisenTutkinnonÄidinkieli(
  @KoodistoKoodiarvo("AI")
  @KoodistoUri("ammatillisenoppiaineet")
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("oppiaineaidinkielijakirjallisuus")
  @Discriminator
  kieli: Koodistokoodiviite,
  pakollinen: Boolean,
  laajuus: Option[LaajuusOsaamispisteissä]
) extends AmmatillisenTutkinnonOsanOsaAlue with KoodistostaLöytyväKoulutusmoduuli{
  override def description(text: LocalizationRepository) = concat(nimi, ", ", kieli)
}

@Description("Suoritukseen liittyvät lisätiedot, kuten mukautettu arviointi tai poikkeus arvioinnissa")
case class AmmatillisenTutkinnonOsanLisätieto(
  @Description("Lisätiedon tyyppi kooditettuna")
  @KoodistoUri("ammatillisentutkinnonosanlisatieto")
  tunniste: Koodistokoodiviite,
  @Description("Lisätiedon kuvaus siinä muodossa, kuin se näytetään todistuksella")
  kuvaus: LocalizedString
)

@Description("Näytön kuvaus")
case class Näyttö(
  @Description("Vapaamuotoinen kuvaus suoritetusta näytöstä")
  kuvaus: Option[LocalizedString],
  suorituspaikka: Option[NäytönSuorituspaikka],
  @Description("Näyttötilaisuuden ajankohta")
  suoritusaika: Option[NäytönSuoritusaika],
  @Description("Näytön arvioinnin lisätiedot")
  @Flatten
  arviointi: Option[NäytönArviointi],
  @Description("Onko näyttö suoritettu työssäoppimisen yhteydessä (true/false)")
  työssäoppimisenYhteydessä: Boolean = false,
  @Description("Halutaanko näytöstä erillinen todistus. Puuttuva arvo tulkitaan siten, että halukkuutta ei tiedetä.")
  haluaaTodistuksen: Option[Boolean] = None
)

@Description("Ammatillisen näytön suorituspaikka")
case class NäytönSuorituspaikka(
  @Description("Suorituspaikan tyyppi 1-numeroisella koodilla")
  @KoodistoUri("ammatillisennaytonsuorituspaikka")
  tunniste: Koodistokoodiviite,
  @Description("Vapaamuotoinen suorituspaikan kuvaus")
  kuvaus: LocalizedString
)

case class NäytönSuoritusaika(
  @Description("Näyttötilaisuuden alkamispäivämäärä. Muoto YYYY-MM-DD")
  alku: LocalDate,
  @Description("Näyttötilaisuuden päättymispäivämäärä. Muoto YYYY-MM-DD")
  loppu: LocalDate
)

case class NäytönArviointi (
  arvosana: Koodistokoodiviite,
  päivä: LocalDate,
  @Description("Näytön arvioineet henkilöt")
  arvioitsijat: Option[List[NäytönArvioitsija]] = None,
  @Description("Näytön eri arviointikohteiden (Työprosessin hallinta jne) arvosanat.")
  @Tabular
  arviointikohteet: Option[List[NäytönArviointikohde]],
  @KoodistoUri("ammatillisennaytonarvioinnistapaattaneet")
  @Description("Arvioinnista päättäneet tahot, ilmaistuna 1-numeroisella koodilla")
  @MinItems(1)
  arvioinnistaPäättäneet: List[Koodistokoodiviite],
  @KoodistoUri("ammatillisennaytonarviointikeskusteluunosallistuneet")
  @Description("Arviointikeskusteluun osallistuneet tahot, ilmaistuna 1-numeroisella koodilla")
  @MinItems(1)
  arviointikeskusteluunOsallistuneet: List[Koodistokoodiviite],
  @Description("Jos näyttö on hylätty, kuvataan hylkäyksen perusteet tänne.")
  hylkäyksenPeruste: Option[LocalizedString] = None
) extends AmmatillinenKoodistostaLöytyväArviointi

case class NäytönArviointikohde(
  @Description("Arviointikohteen tunniste")
  @KoodistoUri("ammatillisennaytonarviointikohde")
  @Title("Arviointikohde")
  tunniste: Koodistokoodiviite,
  @Description("Arvosana. Kullekin arviointiasteikolle löytyy oma koodistonsa")
  @KoodistoUri("arviointiasteikkoammatillinenhyvaksyttyhylatty")
  @KoodistoUri("arviointiasteikkoammatillinent1k3")
  arvosana: Koodistokoodiviite
)

case class NäytönArvioitsija(
  @Representative
  nimi: String,
  @Description("Onko suorittanut näyttötutkintomestarikoulutuksen (true/false). Puuttuva arvo tulkitaan siten, että koulutuksen suorittamisesta ei ole tietoa.")
  @Title("Näyttötutkintomestari")
  ntm: Option[Boolean]
) extends SuorituksenArvioitsija

@Description("Oppisopimuksen tiedot")
case class Oppisopimus(
  @Flatten
  työnantaja: Yritys
)

trait Järjestämismuoto {
  @Discriminator
  def tunniste: Koodistokoodiviite
}

@Description("Järjestämismuoto ilman lisätietoja")
case class JärjestämismuotoIlmanLisätietoja(
  @KoodistoUri("jarjestamismuoto")
  @Representative
  tunniste: Koodistokoodiviite
) extends Järjestämismuoto

@Description("Koulutuksen järjestäminen oppisopimuskoulutuksena. Sisältää oppisopimuksen lisätiedot")
case class OppisopimuksellinenJärjestämismuoto(
  @KoodistoUri("jarjestamismuoto")
  @KoodistoKoodiarvo("20")
  tunniste: Koodistokoodiviite,
  @Flatten
  @Discriminator
  oppisopimus: Oppisopimus
) extends Järjestämismuoto

@Description("Henkilökohtainen opetuksen järjestämistä koskeva suunnitelma, https://fi.wikipedia.org/wiki/HOJKS")
@OksaUri("tmpOKSAID228", "erityisopiskelija")
case class Hojks(
  @KoodistoUri("opetusryhma")
  opetusryhmä: Koodistokoodiviite,
  @Description("Alkamispäivämäärä. Muoto YYYY-MM-DD")
  alku: Option[LocalDate] = None,
  @Description("Loppupäivämäärä. Muoto YYYY-MM-DD")
  loppu: Option[LocalDate] = None
)

case class LaajuusOsaamispisteissä(
  arvo: Float,
  @KoodistoKoodiarvo("6")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite("6", Some(finnish("Osaamispistettä")), "opintojenlaajuusyksikko")
) extends Laajuus

case class NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus(
  @Title("Koulutuksen osa")
  koulutusmoduuli: NäyttötutkintoonValmistavanKoulutuksenOsa,
  tila: Koodistokoodiviite,
  override val alkamispäivä: Option[LocalDate] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Tutkinnon suoritukseen kuuluvat työssäoppimisjaksot")
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  @KoodistoKoodiarvo("nayttotutkintoonvalmistavankoulutuksenosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("nayttotutkintoonvalmistavankoulutuksenosa", koodistoUri = "suorituksentyyppi")
) extends VahvistuksetonSuoritus with Arvioinniton

trait NäyttötutkintoonValmistavanKoulutuksenOsa extends Koulutusmoduuli

@Description("Ammatilliseen peruskoulutukseen valmentavan koulutuksen osan tunnistetiedot")
case class PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa(
  tunniste: PaikallinenKoodi,
  @Description("Tutkinnonosan kuvaus sisältäen ammattitaitovaatimukset")
  kuvaus: LocalizedString
) extends PaikallinenKoulutusmoduuli with NäyttötutkintoonValmistavanKoulutuksenOsa{
  def laajuus = None
}

trait ValmentavaSuoritus extends PäätasonSuoritus with Toimipisteellinen with Todistus with Arvioinniton {
  override def osasuoritukset: Option[List[ValmentavanKoulutuksenOsanSuoritus]] = None
}

@Description("Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)")
@Title("Valma-koulutuksen suoritus")
case class ValmaKoulutuksenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: ValmaKoulutus,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  @Description("Ammatilliseen peruskoulutukseen valmentavan koulutuksen osasuoritukset")
  @Title("Koulutuksen osat")
  override val osasuoritukset: Option[List[ValmaKoulutuksenOsanSuoritus]],
  @KoodistoKoodiarvo("valma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valma", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends ValmentavaSuoritus with AmmatillinenPäätasonSuoritus with Ryhmällinen

@Title("Valma-koulutuksen osan suoritus")
case class ValmaKoulutuksenOsanSuoritus(
  @Title("Koulutuksen osa")
  koulutusmoduuli: ValmaKoulutuksenOsa,
  tila: Koodistokoodiviite,
  arviointi: Option[List[AmmatillinenArviointi]],
  vahvistus: Option[HenkilövahvistusValinnaisellaTittelillä] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Jos tutkinnon osa on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot")
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  @Description("Suoritukseen liittyvän näytön tiedot")
  @ComplexObject
  näyttö: Option[Näyttö] = None,
  @Description("Tutkinnon suoritukseen kuuluvat työssäoppimisjaksot")
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  @KoodistoKoodiarvo("valmakoulutuksenosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valmakoulutuksenosa", koodistoUri = "suorituksentyyppi")
) extends ValmentavanKoulutuksenOsanSuoritus

@Description("Ammatilliseen peruskoulutukseen valmentavan koulutuksen (VALMA) tunnistetiedot")
@Title("Valma-koulutus")
case class ValmaKoulutus(
  @KoodistoKoodiarvo("999901")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999901", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String] = None,
  laajuus: Option[LaajuusOsaamispisteissä] = None
) extends DiaarinumerollinenKoulutus

trait ValmaKoulutuksenOsa extends Koulutusmoduuli

@Description("Ammatilliseen peruskoulutukseen valmentavan koulutuksen osan tunnistetiedot")
@Title("Paikallinen Valma-koulutuksen osa")
case class PaikallinenValmaKoulutuksenOsa(
  tunniste: PaikallinenKoodi,
  @Description("Tutkinnonosan kuvaus sisältäen ammattitaitovaatimukset")
  kuvaus: LocalizedString,
  laajuus: Option[LaajuusOsaamispisteissä],
  pakollinen: Boolean
) extends PaikallinenKoulutusmoduuli with Valinnaisuus with ValmaKoulutuksenOsa

@Description("Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)")
@Title("Telma-koulutuksen suoritus")
case class TelmaKoulutuksenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: TelmaKoulutus,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  @Description("Työhön ja itsenäiseen elämään valmentavan koulutuksen osasuoritukset")
  @Title("Koulutuksen osat")
  override val osasuoritukset: Option[List[TelmaKoulutuksenOsanSuoritus]],
  @KoodistoKoodiarvo("telma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("telma", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends ValmentavaSuoritus with AmmatillinenPäätasonSuoritus with Ryhmällinen

@Title("Telma-koulutuksen osan suoritus")
case class TelmaKoulutuksenOsanSuoritus(
  @Title("Koulutuksen osa")
  koulutusmoduuli: TelmaKoulutuksenOsa,
  tila: Koodistokoodiviite,
  arviointi: Option[List[AmmatillinenArviointi]],
  vahvistus: Option[HenkilövahvistusValinnaisellaTittelillä] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Jos koulutuksen osa on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot")
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  @Description("Suoritukseen liittyvän näytön tiedot")
  @ComplexObject
  näyttö: Option[Näyttö] = None,
  @Description("Tutkinnon suoritukseen kuuluvat työssäoppimisjaksot")
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  @KoodistoKoodiarvo("telmakoulutuksenosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("telmakoulutuksenosa", koodistoUri = "suorituksentyyppi")
) extends ValmentavanKoulutuksenOsanSuoritus

@Description("Työhön ja itsenäiseen elämään valmentavan koulutuksen (TELMA) tunnistetiedot")
@Title("Telma-koulutus")
case class TelmaKoulutus(
  @KoodistoKoodiarvo("999903")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999903", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String] = None,
  laajuus: Option[LaajuusOsaamispisteissä] = None
) extends DiaarinumerollinenKoulutus

trait TelmaKoulutuksenOsa extends Koulutusmoduuli

@Description("Työhön ja itsenäiseen elämään valmentavan koulutuksen osan tunnistiedot")
@Title("Paikallinen Telma-koulutuksen osa")
case class PaikallinenTelmaKoulutuksenOsa(
  tunniste: PaikallinenKoodi,
  @Description("Tutkinnonosan kuvaus sisältäen ammattitaitovaatimukset")
  kuvaus: LocalizedString,
  laajuus: Option[LaajuusOsaamispisteissä],
  pakollinen: Boolean
) extends PaikallinenKoulutusmoduuli with Valinnaisuus with TelmaKoulutuksenOsa

trait AmmatillinenKoodistostaLöytyväArviointi extends KoodistostaLöytyväArviointi with ArviointiPäivämäärällä {
  @KoodistoUri("arviointiasteikkoammatillinenhyvaksyttyhylatty")
  @KoodistoUri("arviointiasteikkoammatillinent1k3")
  override def arvosana: Koodistokoodiviite
  override def arvioitsijat: Option[List[SuorituksenArvioitsija]]
  override def hyväksytty = arvosana.koodiarvo match {
    case "0" => false
    case "Hylätty" => false
    case _ => true
  }
}

case class AmmatillinenArviointi(
  arvosana: Koodistokoodiviite,
  päivä: LocalDate,
  @Description("Tutkinnon osan suorituksen arvioinnista päättäneen henkilön nimi")
  arvioitsijat: Option[List[Arvioitsija]] = None,
  kuvaus: Option[LocalizedString] = None
) extends AmmatillinenKoodistostaLöytyväArviointi with SanallinenArviointi

trait Tutkintonimikkeellinen {
  @Description("Tieto siitä mihin tutkintonimikkeeseen oppijan tutkinto liittyy")
  @KoodistoUri("tutkintonimikkeet")
  @OksaUri("tmpOKSAID588", "tutkintonimike")
  def tutkintonimike: Option[List[Koodistokoodiviite]] = None
}

trait Osaamisalallinen {
  @Description("Tieto siitä mihin osaamisalaan/osaamisaloihin oppijan tutkinto liittyy")
  @KoodistoUri("osaamisala")
  @OksaUri(tunnus = "tmpOKSAID299", käsite = "osaamisala")
  def osaamisala: Option[List[Koodistokoodiviite]] = None
}
