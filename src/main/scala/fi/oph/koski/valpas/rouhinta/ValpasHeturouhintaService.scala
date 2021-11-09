package fi.oph.koski.valpas.rouhinta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.henkilo.{Hetu, OppijaHenkilö}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.valpas.ValpasErrorCategory

import java.time.LocalDate

class ValpasHeturouhintaService(application: KoskiApplication)
  extends ValpasRouhintaTiming
    with DatabaseConverters
    with Logging
{
  private val rajapäivätService = application.valpasRajapäivätService
  private val oppijanumerorekisteri = application.opintopolkuHenkilöFacade
  private val oppijaService = application.valpasOppijaService
  private val rouhintaOvKeskeytyksetService = application.valpasRouhintaOppivelvollisuudenKeskeytysService

  private val maxHetuCount = application.config.getInt("valpas.rouhintaMaxHetuCount")

  def haeHetulistanPerusteellaIlmanOikeustarkastusta
    (hetut: Seq[String])
  : Either[HttpStatus, HeturouhinnanTulos] = {
    cleanedHetuList(hetut).flatMap(hetut => {

      rouhintaTimed("haeHetulistanPerusteella", hetut.size) {
        val (validitHetut, virheellisetHetut) = hetut.partition(hetu => Hetu.validate(hetu, acceptSynthetic = false).isRight)
        val oppijatKoskessa = oppijaService.getOppijaOiditHetuillaIlmanOikeustarkastusta(validitHetut)
        val koskestaLöytymättömätHetut = validitHetut.diff(oppijatKoskessa.map(_.hetu))

        val (oppijatJotkaOnrissaMuttaEiKoskessa, oppijanumerorekisterinUlkopuolisetHetut) = haeOppijanumerorekisteristä(koskestaLöytymättömätHetut)

        val (oppivelvollisetOnrissa, oppivelvollisuudenUlkopuolisetOnrissa) = oppijatJotkaOnrissaMuttaEiKoskessa.partition(onOppivelvollinen)
        val (oppivelvollisetKoskessa, oppivelvollisuudenUlkopuolisetKoskessa) = oppijatKoskessa.partition(_.oppivelvollisuusVoimassa)

        oppijaService
          // Kunnan käyttäjällä on aina oikeudet kaikkiin oppijoihin, joilla on oppivelvollisuus voimassa, joten
          // käyttöoikeustarkistusta ei tarvitse tehdä
          .getOppijalistaIlmanOikeustarkastusta(oppivelvollisetKoskessa.map(_.masterOid))
          .map(oppivelvollisetKoskessa => {
            val (suorittavatKoski, eiSuorittavatKoski) =
              oppivelvollisetKoskessa.map(ValpasRouhintaOppivelvollinen.apply).partition(_.suorittaaOppivelvollisuutta)

            val eiSuorittavatOnr =
              oppivelvollisetOnrissa.map(ValpasRouhintaOppivelvollinen.apply)

            val suorittavat = suorittavatKoski
            val eiSuorittavatKeskeytyksillä =
              rouhintaOvKeskeytyksetService.fetchOppivelvollisuudenKeskeytykset(eiSuorittavatKoski) ++ eiSuorittavatOnr

            HeturouhinnanTulos(
              eiOppivelvollisuuttaSuorittavat = eiSuorittavatKeskeytyksillä,
              oppivelvollisuuttaSuorittavat = suorittavat.flatMap(_.hetu).map(RouhintaPelkkäHetu),
              oppijanumerorekisterinUlkopuoliset = oppijanumerorekisterinUlkopuolisetHetut.map(RouhintaPelkkäHetu),
              oppivelvollisuudenUlkopuoliset = (oppivelvollisuudenUlkopuolisetKoskessa.map(_.hetu) ++ oppivelvollisuudenUlkopuolisetOnrissa.flatMap(_.hetu)).map(RouhintaPelkkäHetu),
              virheellisetHetut = virheellisetHetut.map(RouhintaPelkkäHetu),
            )
          })
      }
    })

  }

  private def haeOppijanumerorekisteristä(hetut: Seq[String]): (Seq[OppijaHenkilö], Seq[String]) = {
    rouhintaTimed("haeOppijanumerorekisteristä", hetut.size) {
      val oppijat = oppijanumerorekisteri.findOppijatByHetusNoSlaveOids(hetut)
      val hetutRekisterissä = oppijat.flatMap(_.hetu)
      (
        oppijat,
        hetut.filterNot(hetutRekisterissä.contains)
      )
    }
  }

  private def onOppivelvollinen(oppija: OppijaHenkilö): Boolean = onOppivelvollinen(oppija.syntymäaika)

  private def onOppivelvollinen(syntymäaika: Option[LocalDate]): Boolean = {
    syntymäaika match {
      case Some(syntymäaika) => {
        val oppivelvollisuusAlkaa = rajapäivätService.oppivelvollisuusAlkaa(syntymäaika)
        val oppivelvollisuusLoppuu = syntymäaika.plusYears(rajapäivätService.oppivelvollisuusLoppuuIka.toLong)
        !oppivelvollisuusAlkaa.isAfter(rajapäivätService.tarkastelupäivä) && oppivelvollisuusLoppuu.isAfter(rajapäivätService.tarkastelupäivä)
      }
      case None => false
    }
  }

  private def cleanedHetuList(hetut: Seq[String]): Either[HttpStatus, Seq[String]] = {
    val list = hetut.map(_.trim).filter(_.nonEmpty)
    if (list.length > maxHetuCount) {
      Left(ValpasErrorCategory.badRequest.requestTooLarge(s"Kyselyssä oli liian monta hetua (${list.length} / $maxHetuCount)"))
    } else {
      Right(list)
    }
  }
}

case class HeturouhinnanTulos(
  eiOppivelvollisuuttaSuorittavat: Seq[ValpasRouhintaOppivelvollinen],
  oppivelvollisuuttaSuorittavat: Seq[RouhintaPelkkäHetu],
  oppijanumerorekisterinUlkopuoliset: Seq[RouhintaPelkkäHetu],
  oppivelvollisuudenUlkopuoliset: Seq[RouhintaPelkkäHetu],
  virheellisetHetut: Seq[RouhintaPelkkäHetu],
) {
  def palautetutOppijaOidit: Seq[Henkilö.Oid] = eiOppivelvollisuuttaSuorittavat.map(_.oppijanumero)
}

case class RouhintaPelkkäHetu(
  hetu: String,
)
