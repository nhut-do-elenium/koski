
package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, OppijaHenkilö}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.validation.MaksuttomuusValidation
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilö, ValpasOppijaLaajatTiedot}
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}
import fi.oph.scalaschema.annotation.SyntheticProperty
import slick.jdbc.GetResult

class ValpasOppijaSearchService(application: KoskiApplication) extends Logging {
  private val henkilöRepository = application.henkilöRepository
  private val hetuValidator = application.hetu
  private val accessResolver = new ValpasAccessResolver
  private val oppijaLaajatTiedotService = application.valpasOppijaLaajatTiedotService
  private val opiskeluoikeusRepository = application.opiskeluoikeusRepository
  private val rajapäivätService = application.valpasRajapäivätService
  private val db = application.raportointiDatabase

  def findHenkilöSuorittaminen
    (query: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    findHenkilö(ValpasRooli.OPPILAITOS_SUORITTAMINEN, query)
  }

  def findHenkilöKunta
    (query: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    findHenkilö(ValpasRooli.KUNTA, query)
  }

  def findHenkilöMaksuttomuus
    (query: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    accessResolver.assertAccessToAnyOrg(ValpasRooli.OPPILAITOS_MAKSUTTOMUUS)
      .flatMap(_ => findHenkilö(asMaksuttomuusHenkilöhakuResultIlmanOikeustarkistusta _, query))
  }

  def findHenkilöOidillaIlmanOikeustarkastusta
    (oppijaOid: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    henkilöRepository
      .findByOid(oppijaOid, findMasterIfSlaveOid = true)
      .toRight(ValpasErrorCategory.notFound.oppijaaEiLöydyOpintopolusta())
      .flatMap(asMaksuttomuusHenkilöhakuResultIlmanOikeustarkistusta)
  }

  def findHenkilöHetullaIlmanOikeustarkastusta
    (hetu: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    henkilöRepository
      .findByHetuOrCreateIfInYtrOrVirta(hetu)
      .toRight(ValpasErrorCategory.notFound.oppijaaEiLöydyOpintopolusta())
      .flatMap(asMaksuttomuusHenkilöhakuResultIlmanOikeustarkistusta)
  }

  private def findHenkilö
    (rooli: ValpasRooli.Role, query: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    accessResolver.assertAccessToAnyOrg(rooli)
      .flatMap(_ => findHenkilö(asYksinkertainenHenkilöhakuResult(rooli) _, query))
  }

  private def findHenkilö
    (asHenkilöhakuResult: (OppijaHenkilö) => Either[HttpStatus, ValpasHenkilöhakuResult], query: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    if (hetuValidator.validate(query).isRight) {
      searchByHetu(asHenkilöhakuResult, query)
    } else if (Henkilö.isValidHenkilöOid(query)) {
      searchByOppijaOid(asHenkilöhakuResult, query)
    } else {
      Left(ValpasErrorCategory.badRequest.validation.epävalidiHenkilöhakutermi())
    }
  }

  private def searchByHetu
    (asHenkilöhakuResult: (OppijaHenkilö) => Either[HttpStatus, ValpasHenkilöhakuResult], hetu: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] =
    asSearchResult(asHenkilöhakuResult, henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(hetu))

  private def searchByOppijaOid
    (asHenkilöhakuResult: (OppijaHenkilö) => Either[HttpStatus, ValpasHenkilöhakuResult], oid: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] =
    asSearchResult(asHenkilöhakuResult, henkilöRepository.findByOid(oid, findMasterIfSlaveOid = true))

  private def asSearchResult
    (asResult: (OppijaHenkilö) => Either[HttpStatus, ValpasHenkilöhakuResult], oppijaHenkilö: Option[OppijaHenkilö])
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    oppijaHenkilö match {
      case None => Right(ValpasEiLöytynytHenkilöhakuResult())
      case Some(henkilö) => {
        asResult(henkilö)
      }
    }
  }

  private def asMaksuttomuusHenkilöhakuResultIlmanOikeustarkistusta
    (henkilö: OppijaHenkilö)
  : Either[HttpStatus, ValpasHenkilöhakuResult] = {
    val perusopetuksenAikavälit = opiskeluoikeusRepository.getPerusopetuksenAikavälitIlmanKäyttöoikeustarkistusta(henkilö.oid)
    val onMahdollisestiLainPiirissä =
      MaksuttomuusValidation.eiOppivelvollisuudenLaajentamislainPiirissäSyyt(
        henkilö.syntymäaika,
        perusopetuksenAikavälit,
        rajapäivätService
      ).isEmpty

    if (onMahdollisestiLainPiirissä) {
      oppijaLaajatTiedotService.getOppijaLaajatTiedotIlmanOikeustarkastusta(henkilö.oid)
        .map({
          case Some(o) if o.onOikeusValvoaMaksuttomuutta => ValpasLöytyiHenkilöhakuResult(o)
          // Henkilö, jonka tiedot löytyvät, mutta jolla maksuttomuus on päättynyt esim. toiselta asteelta
          // valmistumiseen, ei ole enää maksuttomuuden piirissä:
          case Some(o) => ValpasEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult(Some(o.henkilö.oid), o.henkilö.hetu)
          case None => asLaajatOppijaHenkilöTiedot(henkilö) match {
            case Some(h) if !h.turvakielto && h.laajennetunOppivelvollisuudenUlkopuolinenKunnanPerusteella => ValpasEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult(Some(h.oid), h.hetu)
            case _ => ValpasEiLöytynytHenkilöhakuResult()
          }
        })
    } else {
      Right(ValpasEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult(Some(henkilö.oid), henkilö.hetu))
    }
  }

  private def asLaajatOppijaHenkilöTiedot(henkilö: OppijaHenkilö): Option[LaajatOppijaHenkilöTiedot] = {
    henkilö match {
      case h: LaajatOppijaHenkilöTiedot => Some(h)
      case _ => henkilöRepository.findByOid(henkilö.oid, findMasterIfSlaveOid = true)
    }
  }

  private def asYksinkertainenHenkilöhakuResult
    (rooli: ValpasRooli.Role)
    (henkilö: OppijaHenkilö)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasLöytyiHenkilöhakuResult] = {
    oppijaLaajatTiedotService.getOppijaLaajatTiedot(rooli, henkilö.oid)
      .map(ValpasLöytyiHenkilöhakuResult.apply)
  }

  implicit private val getResultValpasLöytyiHenkilöhakuResult: GetResult[ValpasLöytyiHenkilöhakuResult] = GetResult(row =>
    ValpasLöytyiHenkilöhakuResult(
      oid = row.rs.getString("oid"),
      hetu = Option(row.rs.getString("hetu")),
      etunimet = row.rs.getString("etunimet"),
      sukunimi = row.rs.getString("sukunimi"),
    ))
}

object ValpasLöytyiHenkilöhakuResult {
  def apply(oppija: ValpasOppijaLaajatTiedot): ValpasLöytyiHenkilöhakuResult =
    ValpasLöytyiHenkilöhakuResult(
      oid = oppija.henkilö.oid,
      hetu = oppija.henkilö.hetu,
      etunimet = oppija.henkilö.etunimet,
      sukunimi = oppija.henkilö.sukunimi,
    )
}

trait ValpasHenkilöhakuResult {
  @SyntheticProperty
  def ok: Boolean

  def cleanUpForUserSearch: ValpasHenkilöhakuResult = this
}

case class ValpasLöytyiHenkilöhakuResult(
  oid: ValpasHenkilö.Oid,
  hetu: Option[String],
  etunimet: String,
  sukunimi: String,
) extends ValpasHenkilöhakuResult {
  def ok = true
}

case class ValpasEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult(
  oid: Option[ValpasHenkilö.Oid],
  hetu: Option[String],
  eiLainTaiMaksuttomuudenPiirissä: Boolean = true
) extends ValpasHenkilöhakuResult {
  def ok = false

  override def cleanUpForUserSearch: ValpasHenkilöhakuResult = this.copy(oid = None, hetu = None)
}

case class ValpasEiLöytynytHenkilöhakuResult() extends ValpasHenkilöhakuResult {
  def ok = false
}
