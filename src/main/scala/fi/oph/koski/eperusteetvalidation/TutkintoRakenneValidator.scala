package fi.oph.koski.eperusteetvalidation

import com.typesafe.config.Config
import fi.oph.koski.documentation.KoskiApiOperations.opiskeluoikeus
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._
import fi.oph.koski.schema
import fi.oph.koski.tutkinto.Koulutustyyppi._
import fi.oph.koski.tutkinto.{Koulutustyyppi, _}
import fi.oph.koski.util.ChainingSyntax._

import java.time.LocalDate
import scala.collection.mutable.ArrayBuffer

case class TutkintoRakenneValidator(tutkintoRepository: TutkintoRepository, koodistoViitePalvelu: KoodistoViitePalvelu, config: Config)
  extends EPerusteetValidationUtils(tutkintoRepository, koodistoViitePalvelu) with Logging {

  def validate(suoritus: PäätasonSuoritus, alkamispäiväLäsnä: Option[LocalDate], vaadittuPerusteenVoimassaolopäivä: LocalDate, oo: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    validateTutkintoRakenne(suoritus, alkamispäiväLäsnä, vaadittuPerusteenVoimassaolopäivä, oo)
      .onSuccess(validateDiaarinumerollinenAmmatillinen(suoritus, vaadittuPerusteenVoimassaolopäivä))
      .onSuccess(validatePaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus(suoritus, vaadittuPerusteenVoimassaolopäivä))
  }

  private def validateTutkintoRakenne(
    suoritus: PäätasonSuoritus,
    alkamispäiväLäsnä: Option[LocalDate],
    vaadittuPerusteenVoimassaolopäivä: LocalDate,
    oo: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    suoritus match {
      case tutkintoSuoritus: AmmatillisenTutkinnonSuoritus =>
        validateKoulutustyypitJaHaeRakenteet(tutkintoSuoritus.koulutusmoduuli, Some(ammatillisetKoulutustyypit), Some(vaadittuPerusteenVoimassaolopäivä), Some(tutkintoSuoritus)) match {
          case Left(status) => status
          case Right(rakenteet) =>
            HttpStatus.fold {
              val tuloksetOsaamisala = rakenteet.map(rakenne =>
                validateOsaamisalat(tutkintoSuoritus.osaamisala.toList.flatten.map(_.osaamisala), rakenne)
              )
              val tuloksetTutkintonimike = rakenteet.map(rakenne =>
                validateTutkintonimikkeet(tutkintoSuoritus.tutkintonimike.toList.flatten, rakenne)
              )
              if (tuloksetOsaamisala.exists(_.isOk) && tuloksetTutkintonimike.exists(_.isOk)) {
                List(HttpStatus.ok)
              } else {
                tuloksetOsaamisala ++ tuloksetTutkintonimike
              }
            }.onSuccess(HttpStatus.fold(suoritus.osasuoritusLista.map {
              case osaSuoritus: AmmatillisenTutkinnonOsanSuoritus =>
                HttpStatus.fold(osaSuoritus.koulutusmoduuli match {
                  case osa: ValtakunnallinenTutkinnonOsa =>
                    HttpStatus.fold {
                      val tulokset = rakenteet.map(rakenne =>
                        validateTutkinnonOsa(
                          osaSuoritus,
                          osa,
                          rakenne,
                          tutkintoSuoritus.suoritustapa,
                          alkamispäiväLäsnä,
                          vaadittuPerusteenVoimassaolopäivä,
                          oo,
                          suoritus
                        )
                      )
                      if (tulokset.exists(_.isOk)) {
                        List(HttpStatus.ok)
                      } else {
                        tulokset
                      }
                    }
                  case osa: PaikallinenTutkinnonOsa =>
                    HttpStatus.ok // vain OpsTutkinnonosatoteutukset validoidaan, muut sellaisenaan läpi, koska niiden rakennetta ei tunneta
                  case osa: KorkeakouluopinnotTutkinnonOsa =>
                    HttpStatus.ok
                  case osa: JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa =>
                    HttpStatus.ok
                }, validateTutkintoField(tutkintoSuoritus, osaSuoritus))
            }))
        }
      case n: NäyttötutkintoonValmistavanKoulutuksenSuoritus =>
        HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(n.tutkinto, Some(ammatillisetKoulutustyypit), Some(vaadittuPerusteenVoimassaolopäivä)))
      case suoritus: AikuistenPerusopetuksenOppimääränSuoritus =>
        HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(suoritus.koulutusmoduuli, Some(List(aikuistenPerusopetus)), Some(vaadittuPerusteenVoimassaolopäivä), Some(suoritus)))
      case suoritus: AmmatillisenTutkinnonOsittainenSuoritus => HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(
        suoritus.koulutusmoduuli,
        Some(ammatillisetKoulutustyypit),
        Some(vaadittuPerusteenVoimassaolopäivä),
        Some(suoritus)
      )).onSuccess(
        HttpStatus.fold(
          suoritus.osasuoritukset.toList.flatten.map {
            case suoritus if suoritus.tunnustettu.isDefined => validateTutkinnonOsanTutkinto(suoritus, None)
            case suoritus => validateTutkinnonOsanTutkinto(suoritus, Some(vaadittuPerusteenVoimassaolopäivä))
          }
        )
      )
      case s: LukionPäätasonSuoritus2019 =>
        HttpStatus.justStatus(
          validateKoulutustyypitJaHaeRakenteet(s.koulutusmoduuli, Some(lukionKoulutustyypit), Some(vaadittuPerusteenVoimassaolopäivä))
        ).onSuccess(validateLukio2019Diaarinumero(s))
      case s: TaiteenPerusopetuksenPäätasonSuoritus =>
        HttpStatus.justStatus(
          validateKoulutustyypitJaHaeRakenteet(s.koulutusmoduuli, Some(List(taiteenperusopetus)), Some(vaadittuPerusteenVoimassaolopäivä))
        ).onSuccess(validateTaiteenPerusopetuksenPerusteenDiaarinumero2017(s))
      case _ =>
        suoritus.koulutusmoduuli match {
          case d: Esiopetus =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(esiopetus)), Some(vaadittuPerusteenVoimassaolopäivä)))
          case d: AikuistenPerusopetus =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(aikuistenPerusopetus)), Some(vaadittuPerusteenVoimassaolopäivä)))
          case d: AikuistenPerusopetuksenAlkuvaihe =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(aikuistenPerusopetus)), Some(vaadittuPerusteenVoimassaolopäivä)))
          case d: PerusopetuksenDiaarinumerollinenKoulutus =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(perusopetus)), Some(vaadittuPerusteenVoimassaolopäivä)))
          case d: PerusopetukseenValmistavaOpetus =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(perusopetukseenValmistava)), Some(vaadittuPerusteenVoimassaolopäivä)))
          case d: PerusopetuksenLisäopetus =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(perusopetuksenLisäopetus)), Some(vaadittuPerusteenVoimassaolopäivä)))
          case d: AikuistenPerusopetuksenOppiaine =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(aikuistenPerusopetus)), Some(vaadittuPerusteenVoimassaolopäivä)))
          case d: NuortenPerusopetuksenOppiaine =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(perusopetus)), Some(vaadittuPerusteenVoimassaolopäivä)))
          case d: LukionOppimäärä =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(lukionKoulutustyypit), Some(vaadittuPerusteenVoimassaolopäivä))).onSuccess(validateLukio2015Diaarinumero(d))
          case d: LukioonValmistavaKoulutus =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(luvaKoulutustyypit), Some(vaadittuPerusteenVoimassaolopäivä)))
          // Valmassa erikoistapauksena hyväksytään valmistuminen pidempään TUVA-siirtymän vuoksi
          // Katso myös EPerusteisiinPerustuvaValidation.validatePerusteVoimassa
          case d: ValmaKoulutus if vaadittuPerusteenVoimassaolopäivä.isBefore(LocalDate.of(2022, 7, 31)) =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(valmaKoulutustyypit), Some(vaadittuPerusteenVoimassaolopäivä)))
          case d: ValmaKoulutus if vaadittuPerusteenVoimassaolopäivä.isBefore(LocalDate.of(2023, 6, 1)) =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(valmaKoulutustyypit), Some(LocalDate.of(2022, 7, 31))))
          case d: ValmaKoulutus =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(valmaKoulutustyypit), Some(vaadittuPerusteenVoimassaolopäivä)))
          case d: TelmaKoulutus =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(telma)), Some(vaadittuPerusteenVoimassaolopäivä)))
          case d: LukionOppiaine =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(lukionKoulutustyypit), Some(vaadittuPerusteenVoimassaolopäivä))).onSuccess(validateLukio2015Diaarinumero(d))
          case d: TutkintokoulutukseenValmentavanKoulutus =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(tuva)), Some(vaadittuPerusteenVoimassaolopäivä)))
          case d: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(vapaanSivistystyönVapaatavoitteinenKoulutus)), Some(vaadittuPerusteenVoimassaolopäivä)))
          case d: VapaanSivistystyönLukutaitokoulutus =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(vstlukutaitokoulutus)), Some(vaadittuPerusteenVoimassaolopäivä)))
          case d: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(vapaanSivistystyönMaahanmuuttajienKotoutumisKoulutus)), Some(vaadittuPerusteenVoimassaolopäivä)))
          case d: VSTKotoutumiskoulutus2022 =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, Some(List(vapaanSivistystyönMaahanmuuttajienKotoutumisKoulutus)), Some(vaadittuPerusteenVoimassaolopäivä)))
          case d: Diaarinumerollinen =>
            HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(d, None, Some(vaadittuPerusteenVoimassaolopäivä)))
          case _ => HttpStatus.ok
        }
    }
  }

  private def validateTutkintoField(tutkintoSuoritus: AmmatillisenTutkinnonSuoritus, osaSuoritus: AmmatillisenTutkinnonOsanSuoritus) = (tutkintoSuoritus.koulutusmoduuli.perusteenDiaarinumero, osaSuoritus.tutkinto.flatMap(_.perusteenDiaarinumero)) match {
    case (Some(tutkinnonDiaari), Some(osanDiaari)) if tutkinnonDiaari == osanDiaari =>
      KoskiErrorCategory.badRequest.validation.rakenne.samaTutkintokoodi(s"Tutkinnon osalle ${osaSuoritus.koulutusmoduuli.tunniste} on merkitty tutkinto, jossa on sama diaarinumero $tutkinnonDiaari kuin tutkinnon suorituksessa")
    case _ =>
      HttpStatus.ok
  }

  private def validateKoulutustyypitJaHaeRakenteet(
    tutkinto: Diaarinumerollinen,
    koulutustyypit: Option[List[Koulutustyyppi.Koulutustyyppi]],
    vaadittuPerusteenVoimassaolopäivä: Option[LocalDate],
    suoritusVirheilmoitukseen: Option[PäätasonSuoritus] = None
  ): Either[HttpStatus, List[TutkintoRakenne]] = {
    validateDiaarinumero(tutkinto.perusteenDiaarinumero)
      .flatMap { diaarinumero =>
        if (onKoodistossa(diaarinumero)) {
          Left(KoskiErrorCategory.ok())
        } else {
          tutkintoRepository.findPerusteRakenteet(diaarinumero, vaadittuPerusteenVoimassaolopäivä) match {
            case Nil => {
              logger.warn(s"Opiskeluoikeuden voimassaoloaikana voimassaolevaa tutkinnon perustetta ei löydy diaarinumerolla " + diaarinumero + " eperusteista eikä koskikoulutustendiaarinumerot-koodistosta")
              Left(KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari(s"Opiskeluoikeuden voimassaoloaikana voimassaolevaa tutkinnon perustetta ei löydy diaarinumerolla " + diaarinumero))
            }
            case rakenteet =>
              koulutustyypit match {
                case Some(koulutustyypit) if !rakenteet.exists(rakenne => koulutustyypit.contains(rakenne.koulutustyyppi)) =>
                  val tyyppiStr = suoritusVirheilmoitukseen.getOrElse(tutkinto) match {
                    case p: Product => p.productPrefix
                    case x: AnyRef => x.getClass.getSimpleName
                  }
                  Left(KoskiErrorCategory.badRequest.validation.rakenne.vääräKoulutustyyppi(
                    rakenteet.map(rakenne =>
                      s"Suoritukselle $tyyppiStr ei voi käyttää opiskeluoikeuden voimassaoloaikana voimassaollutta perustetta ${rakenne.diaarinumero} (${rakenne.id}), jonka koulutustyyppi on ${Koulutustyyppi.describe(rakenne.koulutustyyppi)}. "
                    ).mkString +
                      s"Tälle suoritukselle hyväksytyt perusteen koulutustyypit ovat ${koulutustyypit.map(Koulutustyyppi.describe).mkString(", ")}."
                  ))
                case _ =>
                  Right(rakenteet)
              }
          }
        }
      }
  }

  private def validateDiaarinumerollinenAmmatillinen(suoritus: PäätasonSuoritus, vaadittuPerusteenVoimassaolopäivä: LocalDate) = suoritus.koulutusmoduuli match {
    case koulutusmoduuli: Diaarinumerollinen if suoritus.isInstanceOf[AmmatillinenPäätasonSuoritus] =>
      validateKoulutusmoduulinTunniste(koulutusmoduuli.tunniste, koulutusmoduuli.perusteenDiaarinumero, vaadittuPerusteenVoimassaolopäivä)
    case _ => HttpStatus.ok
  }

  private def validatePaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus(suoritus: PäätasonSuoritus, vaadittuPerusteenVoimassaolopäivä: LocalDate) = HttpStatus.fold(
    suoritus.rekursiivisetOsasuoritukset.map {
      case os: PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus if os.liittyyTutkintoon.perusteenDiaarinumero.isDefined =>
        validateKoulutusmoduulinTunniste(os.liittyyTutkintoon.tunniste, os.liittyyTutkintoon.perusteenDiaarinumero, vaadittuPerusteenVoimassaolopäivä)
      case _ => HttpStatus.ok
    })

  private def validateKoulutusmoduulinTunniste(tunniste: KoodiViite, diaariNumero: Option[String], vaadittuPerusteenVoimassaolopäivä: LocalDate) = diaariNumero match {
    case None => KoskiErrorCategory.badRequest.validation.rakenne.diaariPuuttuu()
    case Some(diaari) if onKoodistossa(diaari) =>
      HttpStatus.ok
    case Some(diaari) =>
      val koulutukset = tutkintoRepository.findPerusteRakenteet(diaari, Some(vaadittuPerusteenVoimassaolopäivä)).flatMap(_.koulutukset.map(_.koodiarvo))
      HttpStatus.validate(koulutukset.isEmpty || koulutukset.contains(tunniste.koodiarvo))(
        KoskiErrorCategory.badRequest.validation.rakenne.tunnisteenKoodiarvoaEiLöydyRakenteesta(
          s"Tunnisteen koodiarvoa ${tunniste.koodiarvo} ei löytynyt opiskeluoikeuden voimassaoloaikana voimassaolleen rakenteen ${diaariNumero.get} mahdollisista koulutuksista. Tarkista tutkintokoodit ePerusteista."
        ))
  }

  private def validateDiaarinumero(diaarinumero: Option[String]): Either[HttpStatus, String] = {
    // Avoid sending totally bogus diaarinumeros to ePerusteet (e.g. 3000 characters long), as that leads
    // to "414 Request-URI Too Large" and eventually "Internal server error". Other than that, don't validate
    // the format (at least not yet), since in theory diaarinumero could contain spaces etc.
    diaarinumero match {
      case None => Left(KoskiErrorCategory.badRequest.validation.rakenne.diaariPuuttuu())
      case Some(d) if (d.length < 1) || (d.length > 30) => Left(KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Diaarinumeron muoto on virheellinen: " + diaarinumero.get.take(30)))
      case Some(d) => Right(d)
    }
  }

  private def validateOsaamisalat(osaamisalat: List[Koodistokoodiviite], rakenne: TutkintoRakenne): HttpStatus = {
    val tuntemattomatOsaamisalat: List[Koodistokoodiviite] = osaamisalat.filter(osaamisala => findOsaamisala(rakenne, osaamisala.koodiarvo).isEmpty)

    HttpStatus.fold(tuntemattomatOsaamisalat.map {
      osaamisala: Koodistokoodiviite => KoskiErrorCategory.badRequest.validation.rakenne.tuntematonOsaamisala(s"Osaamisala ${osaamisala.koodiarvo} ei löydy tutkintorakenteesta opiskeluoikeuden voimassaoloaikana voimassaolleelle perusteelle ${rakenne.diaarinumero} (${rakenne.id})")
    })
  }

  private def validateTutkintonimikkeet(tutkintonimikkeet: List[Koodistokoodiviite], rakenne: TutkintoRakenne): HttpStatus = {
    val tuntemattomatTutkintonimikkeet: List[Koodistokoodiviite] = tutkintonimikkeet.filter(tutkintonimike => findTutkintonimike(rakenne, tutkintonimike.koodiarvo).isEmpty)

    if (tuntemattomatTutkintonimikkeet.isEmpty) {
      HttpStatus.ok
    } else {
      val nimetJaKoodiarvot = tuntemattomatTutkintonimikkeet.map(tutkintonimike => s"${tutkintonimike.nimi.map(_.get("fi")).getOrElse("Tuntematon nimi")}(${tutkintonimike.koodiarvo})").mkString(",")

      HttpStatus.fold(tuntemattomatTutkintonimikkeet.map {
        tutkintonimike: Koodistokoodiviite =>
          KoskiErrorCategory.badRequest.validation.rakenne.tuntematonTutkintonimike(
            s"Tutkintonimikkeitä ${nimetJaKoodiarvot} ei löydy tutkintorakenteesta opiskeluoikeuden voimassaoloaikana voimassaolleelle perusteelle ${rakenne.diaarinumero} (${rakenne.id})"
          )
      })
    }
  }

  private def validateTutkinnonOsanTutkinto(suoritus: TutkinnonOsanSuoritus, vaadittuPerusteenVoimassaolopäivä: Option[LocalDate]) = {
    suoritus.tutkinto match {
      case Some(tutkinto) => HttpStatus.justStatus(validateKoulutustyypitJaHaeRakenteet(tutkinto, Some(ammatillisetKoulutustyypit), vaadittuPerusteenVoimassaolopäivä))
      case None => HttpStatus.ok
    }
  }

  private def validateTutkinnonOsa(
    suoritus: AmmatillisenTutkinnonOsanSuoritus,
    osa: ValtakunnallinenTutkinnonOsa,
    rakenne: TutkintoRakenne,
    suoritustapa: Koodistokoodiviite,
    alkamispäiväLäsnä: Option[LocalDate],
    vaadittuPerusteenVoimassaolopäivä: LocalDate,
    oo: KoskeenTallennettavaOpiskeluoikeus,
    ps: PäätasonSuoritus):
  HttpStatus = {
    val suoritustapaJaRakenne = rakenne.findSuoritustapaJaRakenne(suoritustapa)
      .orElse {
        // TOR-384 Siirtymäaikana (vuonna 2018 aloittaneet) käytetään suoritustapaa "reformi", vaikka
        // opiskelisi vanhojen perusteiden mukaisesti. Mikäli ePerusteista ei löydy tutkinnon rakennetta
        // suoritustavalla "reformi", tarkistetaan löytyykö rakenne suoritustavalla "naytto" tai "ops" ja
        // validoidaan sen mukaan.
        if (suoritustapa == Suoritustapa.reformi && alkamispäiväLäsnä.exists(_.getYear == 2018)) rakenne.koulutustyyppi match {
          case Koulutustyyppi.ammattitutkinto | Koulutustyyppi.erikoisammattitutkinto => rakenne.findSuoritustapaJaRakenne(Suoritustapa.naytto)
          case k if Koulutustyyppi.ammatillisenPerustutkinnonTyypit.contains(k) => rakenne.findSuoritustapaJaRakenne(Suoritustapa.ops)
          case _ => None
        } else None
      }
    suoritustapaJaRakenne match {
      case Some(suoritustapaJaRakenne) =>
        (suoritus.tutkinto, suoritus.tutkinnonOsanRyhmä) match {
          case (Some(tutkinto), _) if suoritus.tunnustettu.isDefined =>
            // Tunnustettu tutkinnon osa toisesta tutkinnosta.
            // Perusteen voimassaolon ajankohtaa ei ole rajoitettu, mutta validoidaan että rakenne löytyy diaarinumerolla
            validateTutkinnonOsanTutkinto(suoritus, None)
          case (Some(tutkinto), _) =>
            // Tutkinnon osa toisesta tutkinnosta.
            // Ei validoida rakenteeseen kuuluvuutta, vain se, että rakenne löytyy diaarinumerolla
            validateTutkinnonOsanTutkinto(suoritus, Some(vaadittuPerusteenVoimassaolopäivä))
          case (_, Some(Koodistokoodiviite(koodiarvo, _, _, _, _))) if List("3", "4").contains(koodiarvo) =>
            // Vapaavalintainen tai yksilöllisesti tutkintoa laajentava osa
            // Ei validoida rakenteeseen kuuluvuutta
            HttpStatus.ok
          case (_, _) =>
            // Validoidaan tutkintorakenteen mukaisesti
            findTutkinnonOsa(suoritustapaJaRakenne, osa.tunniste) match {
              case None =>
                KoskiErrorCategory.badRequest.validation.rakenne.tuntematonTutkinnonOsa(
                  s"Tutkinnon osa ${osa.tunniste} ei löydy tutkintorakenteesta opiskeluoikeuden voimassaoloaikana voimassaolleelle perusteelle ${rakenne.diaarinumero} (${rakenne.id}) - suoritustapa ${suoritustapaJaRakenne.suoritustapa.koodiarvo}")
              case Some(tutkinnonOsa) =>
                validateLaajuusJaOsaAlueet(suoritus, tutkinnonOsa, oo, ps, rakenne)
            }
        }
      case None =>
        KoskiErrorCategory.badRequest.validation.rakenne.suoritustapaaEiLöydyRakenteesta(s"Suoritustapaa ei löydy tutkinnon rakenteesta opiskeluoikeuden voimassaoloaikana voimassaolleelle perusteelle ${rakenne.diaarinumero} (${rakenne.id})")
    }
  }

  private def findTutkinnonOsa(rakenne: SuoritustapaJaRakenne, koulutusModuuliTunniste: Koodistokoodiviite): Option[TutkinnonOsa] = {
    rakenne.rakenne.flatMap(findTutkinnonOsa(_, koulutusModuuliTunniste))
  }

  private def findTutkinnonOsa(rakenne: RakenneOsa, koulutusModuuliTunniste: Koodistokoodiviite): Option[TutkinnonOsa] = rakenne match {
    case t: TutkinnonOsa if t.tunniste == koulutusModuuliTunniste => Some(t)
    case t: RakenneModuuli => t.osat.flatMap(findTutkinnonOsa(_, koulutusModuuliTunniste)).headOption
    case _ => None
  }

  private def findOsaamisala(rakenne: TutkintoRakenne, osaamisAlaKoodi: String) = rakenne.osaamisalat.find(_.koodiarvo == osaamisAlaKoodi)

  private def findTutkintonimike(rakenne: TutkintoRakenne, tutkintonimikeKoodi: String) = rakenne.tutkintonimikkeet.find(_.koodiarvo == tutkintonimikeKoodi)

  private def validateLaajuusJaOsaAlueet(suoritus: AmmatillisenTutkinnonOsanSuoritus, perusteenTutkinnonOsa: TutkinnonOsa, oo: KoskeenTallennettavaOpiskeluoikeus, ps: PäätasonSuoritus, rakenne: TutkintoRakenne):
  HttpStatus = {
    val laajuusValidaatioPäällä = config.getBoolean("validaatiot.ammatillinenEPerusteOsaAlueLaajuusValidaatio")
    val koodiValidaatioAlkaa = LocalDate.parse(config.getString("validaatiot.ammatillinenEPerusteOsaAlueKoodiValidaatioAlkaa"))
    val koodiValidaatioPällä = LocalDate.now().isAfter(koodiValidaatioAlkaa.minusDays(1))

    val logOnlyErrors = new ArrayBuffer[HttpStatus]()

    val osaStatus = perusteenTutkinnonOsa.laajuus match {
      case Some(perusteenLaajuus) if suoritus.arvioitu && suoritus.koulutusmoduuli.laajuus.exists(_.arvo < perusteenLaajuus) =>
        val laajuusError = KoskiErrorCategory.badRequest.validation.laajuudet.suorituksenLaajuusEiVastaaRakennetta(
          s"Arvioidun suorituksen '${suoritus.koulutusmoduuli.nimi.get("fi")}' laajuus oltava perusteen mukaan vähintään ${perusteenLaajuus}${suoritus.koulutusmoduuli.getLaajuus.map(l => s" (oli ${l.arvo})").getOrElse("")}"
        )
        if (laajuusValidaatioPäällä) {
          laajuusError
        } else {
          logOnlyErrors += laajuusError
          HttpStatus.ok
        }
      case _ => HttpStatus.ok
    }

    val osaAlueStatuses = suoritus.osasuoritusLista.map(osaAlueSuoritus => perusteenTutkinnonOsa.osaAlueet.find(perusteOsaAlue =>
        osaAlueSuoritus.koulutusmoduuli.tunniste.koodiarvo == perusteOsaAlue.koodiarvo)
      .map(perusteOsaAlue => {
        val kieliStr = osaAlueSuoritus.koulutusmoduuli match {
          case k: Kieliaine => k.kieli.nimi.map(_.get("fi")).map(str => s", $str").getOrElse("")
          case _ => ""
        }
        val laajuusError = osaAlueSuoritus.koulutusmoduuli match {
          case mod: Valinnaisuus => mod.pakollinen match {
            case true if perusteOsaAlue.pakollisenOsanLaajuus.isDefined && mod.getLaajuus.map(_.arvo) != perusteOsaAlue.pakollisenOsanLaajuus =>
              KoskiErrorCategory.badRequest.validation.laajuudet.suorituksenLaajuusEiVastaaRakennetta(
                s"Osa-alueen '${mod.nimi.get("fi")}${kieliStr}' (${osaAlueSuoritus.koulutusmoduuli.tunniste.koodiarvo}) pakollisen osan laajuus oltava perusteen mukaan ${perusteOsaAlue.pakollisenOsanLaajuus.get}${mod.getLaajuus.map(l => s" (oli ${l.arvo})").getOrElse("")}"
              )
            case false if perusteOsaAlue.valinnaisenOsanLaajuus.isDefined && mod.getLaajuus.map(_.arvo) != perusteOsaAlue.valinnaisenOsanLaajuus =>
              KoskiErrorCategory.badRequest.validation.laajuudet.suorituksenLaajuusEiVastaaRakennetta(
                s"Osa-alueen '${mod.nimi.get("fi")}${kieliStr}' (${osaAlueSuoritus.koulutusmoduuli.tunniste.koodiarvo}) valinnaisen osan laajuus oltava perusteen mukaan ${perusteOsaAlue.valinnaisenOsanLaajuus.get}${mod.getLaajuus.map(l => s" (oli ${l.arvo})").getOrElse("")}"
              )
            case _ => HttpStatus.ok
          }
          case _ => HttpStatus.ok
        }
        if (laajuusValidaatioPäällä) {
          laajuusError
        } else {
          logOnlyErrors += laajuusError
          HttpStatus.ok
        }
      }).getOrElse(
        {
          val koodiError = osaAlueSuoritus.koulutusmoduuli.tunniste match {
            case _ if perusteenTutkinnonOsa.osaAlueet.isEmpty => HttpStatus.ok // Jos osa-alueita ei ole parsittu vanhan mallisesta perusteesta niin skipataan tämä validaatio.
            case t: Koodistokoodiviite if t.koodistoUri == "ammatillisenoppiaineet" && rakenne.voimassaoloAlkaa.exists(_.isEqualOrAfter(LocalDate.of(2022, 8, 1))) => KoskiErrorCategory.badRequest.validation.rakenne(s"Osa-alue '${osaAlueSuoritus.koulutusmoduuli.nimi.get("fi")}' (${osaAlueSuoritus.koulutusmoduuli.tunniste.koodiarvo}) ei kuulu perusteen mukaan tutkinnon osaan '${perusteenTutkinnonOsa.nimi.get("fi")}'")
            case _ => HttpStatus.ok // mm. paikalliset tutkinnon osat
          }
          if (koodiValidaatioPällä) {
            koodiError
          } else {
            logOnlyErrors += koodiError
            HttpStatus.ok
          }
        }
      ))

    logOnlyErrors.foreach(error => {
      if (error.isError) {
        val diaari = ps.koulutusmoduuli match {
          case d: Diaarinumerollinen => d.perusteenDiaarinumero.getOrElse("")
          case _ => ""
        }
        logger.warn(s"Ammatillisen suoritusken E-peruste validaatio poissa päältä mutta epäonnistuisi virheillä: '${error.errorString.getOrElse("")}' " +
          s"opiskeluoikeus_oid: ${oo.oid.getOrElse("")} koulutustoimija_oid: ${oo.koulutustoimija.map(_.oid).getOrElse("")} oppilaitos_oid: ${oo.oppilaitos.map(_.oid).getOrElse("")} " +
          s"tutkinnon_diaarinumero: ${diaari} tutkinnon_nimi: '${ps.koulutusmoduuli.nimi.get("fi")}' tunnustettu: ${if (suoritus.tunnustettu.isDefined) "kyllä" else "ei"}"
        )
      }
    })

    HttpStatus.fold(List(osaStatus) ++ osaAlueStatuses)
  }

  private def validateLukio2019Diaarinumero(s: LukionPäätasonSuoritus2019) = {
    val diaarinumerorajaus = s.oppimäärä.koodiarvo match {
      case "aikuistenops" =>
        Perusteet.AikuistenLukiokoulutuksenOpetussuunnitelmanPerusteet2019
      case "nuortenops" =>
        Perusteet.LukionOpetussuunnitelmanPerusteet2019
      case _ =>
        Diaarinumerot(List())
    }
    val diaarinumero = s.koulutusmoduuli.perusteenDiaarinumero.getOrElse("")

    if (diaarinumerorajaus.matches(diaarinumero)) {
      HttpStatus.ok
    } else {
      KoskiErrorCategory.badRequest.validation.rakenne.vääräDiaari(s"""Väärä diaarinumero "$diaarinumero" suorituksella ${s.tyyppi.koodiarvo}, sallitut arvot: $diaarinumerorajaus""")
    }
  }

  private def validateLukio2015Diaarinumero(d: Diaarinumerollinen) = {
    val diaarinumero = d.perusteenDiaarinumero.getOrElse("")

    val lops2021Diaarinumerot = List(
      Perusteet.LukionOpetussuunnitelmanPerusteet2019.diaari,
      Perusteet.AikuistenLukiokoulutuksenOpetussuunnitelmanPerusteet2019.diaari
    )

    if (lops2021Diaarinumerot.contains(diaarinumero)) {
      KoskiErrorCategory.badRequest.validation.rakenne.vääräDiaari(s"Lukion aiemman opetusohjelman mukaisessa suorituksessa ei voi käyttää lukion 2019 opetussuunnitelman diaarinumeroa")
    } else {
      HttpStatus.ok
    }
  }

  def validateKoulutustyypinLöytyminenAmmatillisissa(oo: KoskeenTallennettavaOpiskeluoikeus): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = {
    // Ammatillisille tutkinnoille varmistetaan että koulutustyyppi löytyi (halutaan erottaa
    // ammatilliset perustutkinnot, erityisammattitutkinnot, yms - muissa tapauksissa jo suorituksen tyyppi
    // on riittävä tarkkuus)
    koulutustyyppiTraversal.toIterable(oo).collectFirst { case k: AmmatillinenTutkintoKoulutus if k.koulutustyyppi.isEmpty => k } match {
      case Some(koulutus) if koulutus.perusteenDiaarinumero.isEmpty => Left(KoskiErrorCategory.badRequest.validation.rakenne.diaariPuuttuu())
      case Some(koulutus) if !koulutus.perusteenDiaarinumero.exists(onKoodistossa) => Left(KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari(s"Tutkinnon perustetta ei löydy diaarinumerolla ${koulutus.perusteenDiaarinumero.getOrElse("")}"))
      case Some(koulutus) => Left(KoskiErrorCategory.badRequest.validation.koodisto.koulutustyyppiPuuttuu(s"Koulutuksen ${koulutus.tunniste.koodiarvo} koulutustyyppiä ei löydy koulutustyyppi-koodistosta."))
      case None => Right(oo)
    }
  }

  def validateTaiteenPerusopetuksenPerusteenDiaarinumero2017(s: TaiteenPerusopetuksenPäätasonSuoritus): HttpStatus = {
    def validateDiaarinumero(s: TaiteenPerusopetuksenPäätasonSuoritus, expected: String): HttpStatus = {
      HttpStatus.validate(s.koulutusmoduuli.perusteenDiaarinumero.contains(expected)) {
        KoskiErrorCategory.badRequest.validation.rakenne.vääräDiaari(s"""Väärä diaarinumero "${s.koulutusmoduuli.perusteenDiaarinumero.getOrElse("")}" suorituksella ${s.tyyppi.koodiarvo}, sallitut arvot: $expected""")
      }
    }

    s match {
      case s: TaiteenPerusopetuksenPäätasonSuoritus if s.koulutusmoduuli.perusteenDiaarinumero.exists(onKoodistossa) => HttpStatus.ok
      case s: TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus =>
        validateDiaarinumero(s, Perusteet.TaiteenPerusopetuksenYleisenOppimääränPerusteet2017.diaari)
      case s: TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus =>
        validateDiaarinumero(s, Perusteet.TaiteenPerusopetuksenYleisenOppimääränPerusteet2017.diaari)
      case s: TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus =>
        validateDiaarinumero(s, Perusteet.TaiteenPerusopetuksenLaajanOppimääränPerusteet2017.diaari)
      case s: TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus =>
        validateDiaarinumero(s, Perusteet.TaiteenPerusopetuksenLaajanOppimääränPerusteet2017.diaari)
    }
  }
}
