package fi.oph.tor.schema

import java.time.LocalDate

import fi.oph.tor.schema.generic.annotation.{Description, MaxItems, MinItems}

@Description("Lukion opiskeluoikeus")
case class LukionOpiskeluoikeus(
  id: Option[Int],
  versionumero: Option[Int],
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  alkamispäivä: Option[LocalDate],
  arvioituPäättymispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid],
  @MinItems(1) @MaxItems(1)
  suoritukset: List[LukionOppimääränSuoritus],
  opiskeluoikeudenTila: Option[OpiskeluoikeudenTila],
  läsnäolotiedot: Option[Läsnäolotiedot],
  @KoodistoKoodiarvo("lukiokoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukiokoulutus", Some("Lukiokoulutus"), "opiskeluoikeudentyyppi", None)
) extends Opiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))

}

case class LukionOppimääränSuoritus(
  paikallinenId: Option[String],
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: OrganisaatioWithOid,
  @KoodistoKoodiarvo("lukionoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionoppimaara", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: Ylioppilastutkinto = Ylioppilastutkinto(),
  arviointi: Option[List[YleissivistävänkoulutuksenArviointi]] = None,
  vahvistus: Option[Vahvistus] = None,
  override val osasuoritukset: Option[List[LukionOppiaineenSuoritus]]
) extends Suoritus

case class LukionOppiaineenSuoritus(
  @KoodistoKoodiarvo("lukionoppiainesuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionoppiainesuoritus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: YleissivistavaOppiaine,
  paikallinenId: Option[String],
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  arviointi: Option[List[YleissivistävänkoulutuksenArviointi]] = None,
  vahvistus: Option[Vahvistus] = None,
  override val osasuoritukset: Option[List[LukionKurssinSuoritus]]
) extends Suoritus

case class LukionKurssinSuoritus(
  @KoodistoKoodiarvo("lukionkurssisuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionkurssisuoritus", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: LukionKurssiModuuli,
  paikallinenId: Option[String],
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  arviointi: Option[List[YleissivistävänkoulutuksenArviointi]] = None,
  vahvistus: Option[Vahvistus] = None
) extends Suoritus

case class LukionKurssiModuuli(
  @Description("Lukion kurssi")
  @KoodistoUri("lukionkurssit")
  @OksaUri("tmpOKSAID873", "kurssi")
  tunniste: Koodistokoodiviite
) extends Koulutusmoduuli

case class Ylioppilastutkinto(
 @Description("Tutkinnon 6-numeroinen tutkintokoodi")
 @KoodistoUri("koulutus")
 @KoodistoKoodiarvo("301000")
 @OksaUri("tmpOKSAID560", "tutkinto")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("301000", koodistoUri = "koulutus")
) extends Koulutusmoduuli

case class LukionMatematiikka(
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "MA", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Mikä matematiikka on kyseessä")
  @KoodistoUri("oppiainematematiikka")
  matematiikka: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[Laajuus] = None
) extends YleissivistavaOppiaine
