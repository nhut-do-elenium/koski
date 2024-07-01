package fi.oph.koski.hakemuspalvelu

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.suoritusjako.common.{OpiskeluoikeusFacade, RawOppija}

class HakemuspalveluService(application: KoskiApplication) extends GlobalExecutionContext with Logging {
  private val opiskeluoikeusFacade = new OpiskeluoikeusFacade[HakemuspalveluOpiskeluoikeus](
    application,
    Some(HakemuspalveluYlioppilastutkinnonOpiskeluoikeus.fromKoskiSchema),
    Some(HakemuspalveluKorkeakoulunOpiskeluoikeus.fromKoskiSchema)
  )

  def findOppija(oppijaOid: String)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, HakemuspalveluOppija] = {

    val oppija = opiskeluoikeusFacade.haeOpiskeluoikeudet(oppijaOid, HakemuspalveluSchema.schemassaTuetutOpiskeluoikeustyypit)
      .map(teePalautettavaHakemuspalveluOppija)

    oppija
  }

  def findOppijaByHetu(hetu: String)
    (implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, HakemuspalveluOppija] = {

    val oppijaResult = application.opintopolkuHenkilöFacade.findOppijaByHetu(hetu)

    oppijaResult match {
      case Some(o) => findOppija(o.oid)
      case None => Left(KoskiErrorCategory.notFound.oppijaaEiLöydyHetulla())
    }
  }

  private def teePalautettavaHakemuspalveluOppija(
    rawOppija: RawOppija[HakemuspalveluOpiskeluoikeus]
  ): HakemuspalveluOppija = {
    HakemuspalveluOppija(
      henkilö = Henkilo.fromOppijaHenkilö(rawOppija.henkilö),
      opiskeluoikeudet = suodataPalautettavat(rawOppija.opiskeluoikeudet).toList
    )
  }

  private def suodataPalautettavat(opiskeluoikeudet: Seq[HakemuspalveluOpiskeluoikeus]): Seq[HakemuspalveluOpiskeluoikeus] = {
    opiskeluoikeudet
      .map { opiskeluoikeus =>
        opiskeluoikeus.withSuoritukset(
          opiskeluoikeus.suoritukset
            .filter(josKKTutkintoNiinVahvistettu)
            .filter(josYOTutkintoNiinVahvistettu)
            .filter(josEBTutkintoNiinVahvistettu)
            .filter(josDIATutkintoNiinVahvistettu)
        )
      }.filter(_.suoritukset.nonEmpty)
  }

  private def josKKTutkintoNiinVahvistettu(s: Suoritus): Boolean = {
    s match {
      case s: HakemuspalveluKorkeakoulututkinnonSuoritus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }

  private def josYOTutkintoNiinVahvistettu(s: Suoritus): Boolean = {
    s match {
      case s: HakemuspalveluYlioppilastutkinnonPäätasonSuoritus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }

  private def josEBTutkintoNiinVahvistettu(s: Suoritus): Boolean = {
    s match {
      case s: HakemuspalveluEBTutkinnonPäätasonSuoritus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }

  private def josDIATutkintoNiinVahvistettu(s: Suoritus): Boolean = {
    s match {
      case s: HakemuspalveluDIATutkinnonSuoritus
      => s.vahvistus.isDefined
      case _
      => true
    }
  }
}
