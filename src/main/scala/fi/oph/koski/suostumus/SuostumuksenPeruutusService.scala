package fi.oph.koski.suostumus

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.db.{KoskiTables, PoistettuOpiskeluoikeusRow, QueryMethods}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusPoistoUtils
import fi.oph.koski.schema.Opiskeluoikeus.VERSIO_1
import fi.oph.koski.schema.{Opiskeluoikeus, SuostumusPeruttavissaOpiskeluoikeudelta}
import slick.jdbc.GetResult
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedot
import fi.oph.koski.suoritusjako.SuoritusIdentifier

case class SuostumuksenPeruutusService(protected val application: KoskiApplication) extends Logging with QueryMethods {

  lazy val db = application.masterDatabase.db
  lazy val perustiedotIndexer = application.perustiedotIndexer
  lazy val opiskeluoikeusRepository = application.opiskeluoikeusRepository
  lazy val henkilöRepository = application.henkilöRepository

  val eiLisättyjäRivejä = 0

  def listaaPerututSuostumukset() = {
    implicit val getResult: GetResult[PoistettuOpiskeluoikeusRow] = {
      GetResult[PoistettuOpiskeluoikeusRow](r =>
        PoistettuOpiskeluoikeusRow(r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.<<,r.nextArray[String]().toList,r.<<)
      )
    }

    runDbSync(
      sql"""
           select oid, oppija_oid, oppilaitos_nimi, oppilaitos_oid, paattymispaiva, lahdejarjestelma_koodi, lahdejarjestelma_id, mitatoity_aikaleima, suostumus_peruttu_aikaleima, koulutusmuoto, suoritustyypit, versio
           from poistettu_opiskeluoikeus
           order by coalesce(mitatoity_aikaleima, suostumus_peruttu_aikaleima) desc;
         """.as[PoistettuOpiskeluoikeusRow]
    )
  }

  def etsiPoistetut(oids: Seq[String]): Seq[PoistettuOpiskeluoikeusRow] =
    runDbSync(KoskiTables.PoistetutOpiskeluoikeudet.filter(r => r.oid inSet oids).result)

  def peruutaSuostumus(
    oid: String,
    suorituksenTyyppi: Option[String]
  )(implicit user: KoskiSpecificSession): HttpStatus = {
    henkilöRepository.findByOid(user.oid) match {
      case Some(henkilö) =>
        val opiskeluoikeudet = opiskeluoikeusRepository.findByCurrentUser(henkilö)(user).get
        val opiskeluoikeus = opiskeluoikeudet.filter(oo =>
          suostumusPeruttavissa(oo)
        ).find (_.oid.contains(oid))

        (opiskeluoikeus, suorituksenTyyppi) match {
          // Jos enemmän kuin yksi suoritus ja suorituksen tyyppi annettu, peru suostumus suoritukselta
          case (Some(oo), Some(tyyppi)) if oo.suoritukset.map(_.tyyppi.koodiarvo).contains(tyyppi) && oo.suoritukset.size > 1 =>
            peruutaSuostumusSuoritukselta(oid, user, henkilö, oo, tyyppi)
          // Jos täsmälleen yksi suoritus ja suorituksen tyyppi annettu, peru suostumus opiskeluoikeudelta
          case (Some(oo), Some(tyyppi)) if oo.suoritukset.map(_.tyyppi.koodiarvo).contains(tyyppi) => peruutaSuostumusOpiskeluoikeudelta(oid, user, henkilö, oo)
          // Jos suorituksen tyyppiä ei annettu, peru suostumus opiskeluoikeudelta
          case (Some(oo), None) => peruutaSuostumusOpiskeluoikeudelta(oid, user, henkilö, oo)
          case (_, _) =>
            KoskiErrorCategory.forbidden.opiskeluoikeusEiSopivaSuostumuksenPerumiselle(s"Opiskeluoikeuden $oid annettu suostumus ei ole peruttavissa. Joko opiskeluoikeudesta on tehty suoritusjako, " +
              s"viranomainen on käyttänyt opiskeluoikeuden tietoja päätöksenteossa, opiskeluoikeus on tyyppiä, jonka kohdalla annettua suostumusta ei voida perua " +
              s"tai opiskeluoikeudelta ei löytynyt annetun syötteen tyyppistä päätason suoritusta.")
        }
      case None => KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia()
    }
  }

  private def peruutaSuostumusSuoritukselta(
    oid: String,
    user: KoskiSpecificSession,
    henkilö: LaajatOppijaHenkilöTiedot,
    oo: Opiskeluoikeus,
    tyyppi: String
  ): HttpStatus = {
    val opiskeluoikeudenId = runDbSync(KoskiTables.OpiskeluOikeudet.filter(_.oid === oid).map(_.id).result).head
    val perustiedot = OpiskeluoikeudenPerustiedot
      .makePerustiedot(opiskeluoikeudenId, oo, application.henkilöRepository.opintopolku.withMasterInfo(henkilö))
    runDbSync(
      DBIO.seq(
        OpiskeluoikeusPoistoUtils
          .poistaPäätasonSuoritus(
            opiskeluoikeudenId,
            oid,
            oo,
            tyyppi,
            oo.versionumero.map(v => v + 1).getOrElse(VERSIO_1),
            henkilö.oid,
            false,
            application.historyRepository
          ),
        application.perustiedotSyncRepository.addToSyncQueue(perustiedot, true)
      )
    )
    teeLogimerkintäSähköpostinotifikaatiotaVarten(oid)
    AuditLog.log(
      KoskiAuditLogMessage(
        KoskiOperation.KANSALAINEN_SUOSTUMUS_PERUMINEN,
        user,
        Map(KoskiAuditLogMessageField.opiskeluoikeusOid -> oid, KoskiAuditLogMessageField.suorituksenTyyppi -> tyyppi)
      )
    )
    HttpStatus.ok
  }

  private def peruutaSuostumusOpiskeluoikeudelta(
    oid: String,
    user: KoskiSpecificSession,
    henkilö: LaajatOppijaHenkilöTiedot,
    oo: Opiskeluoikeus
  ): HttpStatus = {
    val opiskeluoikeudenId = runDbSync(KoskiTables.OpiskeluOikeudet.filter(_.oid === oid).map(_.id).result).head
    runDbSync(
      DBIO.seq(
        OpiskeluoikeusPoistoUtils
          .poistaOpiskeluOikeus(
            opiskeluoikeudenId,
            oid,
            oo,
            oo.versionumero.map(v => v + 1).getOrElse(VERSIO_1),
            henkilö.oid,
            false
          ),
        application.perustiedotSyncRepository.addDeleteToSyncQueue(opiskeluoikeudenId)
      )
    )
    teeLogimerkintäSähköpostinotifikaatiotaVarten(oid)
    AuditLog.log(
      KoskiAuditLogMessage(
        KoskiOperation.KANSALAINEN_SUOSTUMUS_PERUMINEN,
        user,
        Map(KoskiAuditLogMessageField.opiskeluoikeusOid -> oid)
      )
    )
    HttpStatus.ok
  }

  def teeTestimerkintäSähköpostinotifikaatiotaVarten(): Unit = {
    teeLogimerkintäSähköpostinotifikaatiotaVarten("[TÄMÄ ON TESTIVIESTI]")
  }

  private def teeLogimerkintäSähköpostinotifikaatiotaVarten(oid: String): Unit = {
    logger.warn(s"Kansalainen perui suostumuksen. Opiskeluoikeus ${oid}. Ks. tarkemmat tiedot ${application.config.getString("opintopolku.virkailija.url")}/koski/api/opiskeluoikeus/suostumuksenperuutus")
  }

  def suoritusjakoTekemättäWithAccessCheck(oid: String)(implicit user: KoskiSpecificSession): HttpStatus = {
    AuditLog.log(KoskiAuditLogMessage(KoskiOperation.KANSALAINEN_SUORITUSJAKO_TEKEMÄTTÄ_KATSOMINEN, user, Map(KoskiAuditLogMessageField.opiskeluoikeusOid -> oid)))
    henkilöRepository.findByOid(user.oid) match {
      case Some(henkilö) =>
        opiskeluoikeusRepository.findByCurrentUser(henkilö)(user).get.exists(oo =>
          oo.oid.contains(oid) && !suoritusjakoTehty(oo)) match {
          case true => HttpStatus.ok
          case false => KoskiErrorCategory.forbidden.opiskeluoikeusEiSopivaSuostumuksenPerumiselle(s"Opiskeluoikeuden $oid annettu suostumus ei ole peruttavissa. Suorituksesta on tehty suoritusjako.")
        }
      case None => KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia()
    }
  }

  private def suostumusPeruttavissa(oo: Opiskeluoikeus)(implicit user: KoskiSpecificSession) =
    suorituksetPeruutettavaaTyyppiä(oo) && !suoritusjakoTehty(oo)

  def suorituksetPeruutettavaaTyyppiä(oo: Opiskeluoikeus) = {
    val muitaPäätasonSuorituksiaKuinPeruttavissaOlevia = oo.suoritukset.exists {
      case _: SuostumusPeruttavissaOpiskeluoikeudelta => false
      case _ => true
    }
    !muitaPäätasonSuorituksiaKuinPeruttavissaOlevia
  }

  private def suoritusjakoTehty(oo: Opiskeluoikeus) = {
    opiskeluoikeusRepository.suoritusjakoTehtyIlmanKäyttöoikeudenTarkastusta(oo.oid.get)
  }

  // Kutsutaan vain fixtureita resetoitaessa
  def deleteAll() = {
    if (Environment.isMockEnvironment(application.config)) {
      runDbSync(KoskiTables.PoistetutOpiskeluoikeudet.delete)
    } else {
      throw new RuntimeException("Peruutettujen suostumusten taulua ei voi tyhjentää tuotantotilassa")
    }
  }
}
