package fi.oph.koski.schema

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.{DefaultValue, Description, Title}

trait OppiaineenSuoritus extends Suoritus {
  @Title("Oppiaine")
  def koulutusmoduuli: Koulutusmoduuli
}

trait KurssinSuoritus extends Suoritus with Vahvistukseton{
  @Title("Kurssi")
  @FlattenInUI
  def koulutusmoduuli: Koulutusmoduuli
}

trait Yksilöllistettävä {
  @DefaultValue(false)
  @Description("Tieto siitä, onko oppiaineen oppimäärä yksilöllistetty (true/false). Jos oppilas opiskelee yhdessä yksilöllistetyn oppimäärän mukaan, myös päättöarviointi voi näissä aineissa olla sanallinen.")
  @Tooltip("Onko oppilas opiskellut oppiaineessa yksilöllisen oppimäärän. Jos oppilas opiskelee yhdessä yksilöllistetyn oppimäärän mukaan, myös päättöarviointi voi näissä aineissa olla sanallinen.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  def yksilöllistettyOppimäärä: Boolean
}

trait YleissivistavaOppiaine extends KoodistostaLöytyväKoulutusmoduuli with Valinnaisuus {
  @Description("Oppiaine")
  @KoodistoUri("koskioppiaineetyleissivistava")
  @OksaUri("tmpOKSAID256", "oppiaine")
  def tunniste: Koodistokoodiviite
}

trait YleissivistävänKoulutuksenArviointi extends KoodistostaLöytyväArviointi {
  @KoodistoUri("arviointiasteikkoyleissivistava")
  def arvosana: Koodistokoodiviite
  def arvioitsijat = None
  def hyväksytty = arvosana.koodiarvo match {
    case "H" => false
    case "4" => false
    case _ => true
  }
}

trait NumeerinenYleissivistävänKoulutuksenArviointi extends YleissivistävänKoulutuksenArviointi {
  @KoodistoKoodiarvo("4")
  @KoodistoKoodiarvo("5")
  @KoodistoKoodiarvo("6")
  @KoodistoKoodiarvo("7")
  @KoodistoKoodiarvo("8")
  @KoodistoKoodiarvo("9")
  @KoodistoKoodiarvo("10")
  def arvosana: Koodistokoodiviite
}

trait SanallinenYleissivistävänKoulutuksenArviointi extends YleissivistävänKoulutuksenArviointi with SanallinenArviointi {
  @KoodistoKoodiarvo("S")
  @KoodistoKoodiarvo("H")
  @KoodistoKoodiarvo("O")
  def arvosana: Koodistokoodiviite
}

trait OmanÄidinkielenArviointi extends NumeerinenYleissivistävänKoulutuksenArviointi {
  @KoodistoKoodiarvo("O")
  override def arvosana: Koodistokoodiviite
}
