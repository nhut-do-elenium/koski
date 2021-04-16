package fi.oph.koski.valpas.repository

import fi.oph.koski.henkilo.Yhteystiedot
import fi.oph.koski.schema.{BlankableLocalizedString, Koodistokoodiviite, LocalizedString}
import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.koski.valpas.hakukooste.Hakukooste

import java.time.LocalDateTime

case class ValpasYhteystiedot(
  alkuperä: ValpasYhteystietojenAlkuperä,
  yhteystietoryhmänNimi: LocalizedString,
  henkilönimi: Option[String] = None,
  sähköposti: Option[String] = None,
  puhelinnumero: Option[String] = None,
  matkapuhelinnumero: Option[String] = None,
  lähiosoite: Option[String] = None,
  postitoimipaikka: Option[String] = None,
  postinumero: Option[String] = None,
  maa: Option[String] = None,
)

trait ValpasYhteystietojenAlkuperä

case class ValpasYhteystietoHakemukselta (
  hakuNimi: BlankableLocalizedString,
  haunAlkamispaivämäärä: LocalDateTime,
  hakemuksenMuokkauksenAikaleima: Option[LocalDateTime],
  hakuOid: String,
  hakemusOid: String
) extends ValpasYhteystietojenAlkuperä

object ValpasYhteystietoHakemukselta {
  def apply(hakukooste: Hakukooste): ValpasYhteystietoHakemukselta = ValpasYhteystietoHakemukselta(
    hakuNimi = hakukooste.hakuNimi,
    haunAlkamispaivämäärä = hakukooste.haunAlkamispaivamaara,
    hakemuksenMuokkauksenAikaleima = hakukooste.hakemuksenMuokkauksenAikaleima,
    hakuOid = hakukooste.hakuOid,
    hakemusOid = hakukooste.hakemusOid
  )
}

case class ValpasYhteystietoOppijanumerorekisteristä (
  @KoodistoUri("yhteystietojenalkupera")
  alkuperä: Koodistokoodiviite,
  @KoodistoUri("yhteystietotyypit")
  tyyppi: Koodistokoodiviite,
) extends ValpasYhteystietojenAlkuperä

object ValpasYhteystiedot {
  def oppijanIlmoittamatYhteystiedot(hakukooste: Hakukooste, nimi: LocalizedString): ValpasYhteystiedot = ValpasYhteystiedot(
    alkuperä = ValpasYhteystietoHakemukselta(hakukooste),
    yhteystietoryhmänNimi = nimi,
    matkapuhelinnumero = Some(hakukooste.matkapuhelin),
    lähiosoite = Some(hakukooste.lahiosoite),
    postinumero = Some(hakukooste.postinumero),
    postitoimipaikka = hakukooste.postitoimipaikka,
    sähköposti = Some(hakukooste.email),
  )

  def oppijanIlmoittamatHuoltajanYhteystiedot(hakukooste: Hakukooste, nimi: LocalizedString): Option[ValpasYhteystiedot] =
    if (hakukooste.huoltajanNimi.nonEmpty || hakukooste.huoltajanPuhelinnumero.nonEmpty || hakukooste.huoltajanSähkoposti.nonEmpty) {
      Some(ValpasYhteystiedot(
        alkuperä = ValpasYhteystietoHakemukselta(hakukooste),
        yhteystietoryhmänNimi = nimi,
        henkilönimi = hakukooste.huoltajanNimi,
        matkapuhelinnumero = hakukooste.huoltajanPuhelinnumero,
        sähköposti = hakukooste.huoltajanSähkoposti,
      ))
    } else {
      None
    }

  def virallinenYhteystieto(yhteystiedot: Yhteystiedot, nimi: LocalizedString): ValpasYhteystiedot = ValpasYhteystiedot(
    alkuperä = ValpasYhteystietoOppijanumerorekisteristä(yhteystiedot.alkuperä, yhteystiedot.tyyppi),
    yhteystietoryhmänNimi = nimi,
    sähköposti = yhteystiedot.sähköposti,
    puhelinnumero = yhteystiedot.puhelinnumero,
    matkapuhelinnumero = yhteystiedot.matkapuhelinnumero,
    lähiosoite = yhteystiedot.katuosoite,
    postitoimipaikka = yhteystiedot.kunta.orElse(yhteystiedot.kaupunki),
    postinumero = yhteystiedot.postinumero,
    maa = yhteystiedot.maa,
  )
}
