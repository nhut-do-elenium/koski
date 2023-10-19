package fi.oph.koski.opiskeluoikeus

import com.typesafe.config.Config
import fi.oph.koski.db.KoskiTables._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db._
import fi.oph.koski.eperusteetvalidation.EPerusteetOpiskeluoikeusChangeValidator
import fi.oph.koski.henkilo._
import fi.oph.koski.history.{KoskiOpiskeluoikeusHistoryRepository, OpiskeluoikeusHistory}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.perustiedot.{OpiskeluoikeudenPerustiedot, PerustiedotSyncRepository}
import fi.oph.koski.schema._
import fi.oph.scalaschema.Serializer.format
import org.json4s._
import slick.dbio.Effect.{Read, Write}
import slick.dbio.{DBIOAction, NoStream}

import java.time.LocalDate

class PostgresKoskiOpiskeluoikeusRepositoryActions(
  val db: DB,
  val oidGenerator: OidGenerator,
  val henkilöRepository: OpintopolkuHenkilöRepository,
  val henkilöCache: KoskiHenkilöCache,
  val historyRepository: KoskiOpiskeluoikeusHistoryRepository,
  val tableCompanion: OpiskeluoikeusTableCompanion[KoskiOpiskeluoikeusRow],
  val organisaatioRepository: OrganisaatioRepository,
  val ePerusteetChangeValidator: EPerusteetOpiskeluoikeusChangeValidator,
  val perustiedotSyncRepository: PerustiedotSyncRepository,
  val config: Config
) extends PostgresOpiskeluoikeusRepositoryActions[KoskiOpiskeluoikeusRow, KoskiOpiskeluoikeusTable, KoskiOpiskeluoikeusHistoryTable] {
  lazy val validator = new OpiskeluoikeusChangeValidator(organisaatioRepository, ePerusteetChangeValidator, config)

  protected def Opiskeluoikeudet = KoskiOpiskeluOikeudet
  protected def OpiskeluOikeudetWithAccessCheck(implicit user: KoskiSpecificSession) = KoskiOpiskeluOikeudetWithAccessCheck

  protected def saveHistory(opiskeluoikeus: JValue, historia: OpiskeluoikeusHistory, diff: JArray): Int = {
    errorRepository.save(opiskeluoikeus, historia, diff)
  }

  protected def syncAction(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    result: Either[HttpStatus, CreateOrUpdateResult]
  )(implicit user: KoskiSpecificSession): DBIOAction[Any, NoStream, Read with Write] = {
    result match {
      case Right(result) if result.changed =>
        syncHenkilötiedotAction(result.id, oppijaOid.oppijaOid, opiskeluoikeus, result.henkilötiedot)
      case _ =>
        DBIO.successful(Unit)
    }
  }

  private def syncHenkilötiedotAction(id: Int, oppijaOid: String, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, henkilötiedot: Option[OppijaHenkilöWithMasterInfo]) = {
    henkilötiedot match {
      case _ if opiskeluoikeus.mitätöity && opiskeluoikeus.suoritukset.exists(_.tyyppi.koodiarvo == "vstvapaatavoitteinenkoulutus") =>
        perustiedotSyncRepository.addDeleteToSyncQueue(id)
      case Some(henkilö) =>
        val perustiedot = OpiskeluoikeudenPerustiedot.makePerustiedot(id, opiskeluoikeus, henkilö)
        perustiedotSyncRepository.addToSyncQueue(perustiedot, true)
      case None =>
        henkilöCache.getCachedAction(oppijaOid).flatMap {
          case Some(HenkilöRowWithMasterInfo(henkilöRow, masterHenkilöRow)) =>
            val perustiedot = OpiskeluoikeudenPerustiedot.makePerustiedot(id, opiskeluoikeus, henkilöRow, masterHenkilöRow)
            perustiedotSyncRepository.addToSyncQueue(perustiedot, true)
          case None =>
            throw new RuntimeException(s"Oppija not found: $oppijaOid")
        }
    }
  }

  protected override def createInsteadOfUpdate(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    rows: List[KoskiOpiskeluoikeusRow]
  )(implicit user: KoskiSpecificSession): DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write] = {
    val onVstJotpa = opiskeluoikeus.suoritukset.exists {
      case _: VapaanSivistystyönJotpaKoulutuksenSuoritus => true
      case _ => false
    }
    lazy val opiskeluoikeusPäättynyt = rows.exists(_.toOpiskeluoikeusUnsafe.tila.opiskeluoikeusjaksot.last.opiskeluoikeusPäättynyt)
    lazy val duplikoivanOpiskeluoikeudenLuontiSallittu = rows.exists(row => allowOpiskeluoikeusCreationOnConflict(opiskeluoikeus, row))

    if (onVstJotpa || (opiskeluoikeusPäättynyt && duplikoivanOpiskeluoikeudenLuontiSallittu)) {
      createAction(oppijaOid, opiskeluoikeus)
    } else {
      DBIO.successful(Left(KoskiErrorCategory.conflict.exists()))
    }
  }

  protected override def generateOid(oppija: OppijaHenkilöWithMasterInfo): String = {
    oidGenerator.generateKoskiOid(oppija.henkilö.oid)
  }

  protected def allowOpiskeluoikeusCreationOnConflict(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, row: KoskiOpiskeluoikeusRow): Boolean = {
    lazy val perusteenDiaarinumero: Option[String] = {
      val value = (row.data \ "suoritukset")(0) \ "koulutusmoduuli" \ "perusteenDiaarinumero"
      Option(value.extract[String])
    }

    opiskeluoikeus match {
      case oo: AmmatillinenOpiskeluoikeus =>
        // Jos oppilaitos ja perusteen diaarinumero ovat samat, ei sallita päällekkäisen opiskeluoikeuden luontia...
        (!oo.oppilaitos.exists(_.oid == row.oppilaitosOid) || // Tänne ei pitäisi tulla eriävällä oppilaitoksella, mutta tulevien mahdollisten muutosten varalta tehdään tässä eksplisiittinen tarkastus
          !oo.suoritukset
            .collect { case s: AmmatillisenTutkinnonSuoritus => s }
            .exists(s =>
              s.koulutusmoduuli.perusteenDiaarinumero.isDefined &&
                s.koulutusmoduuli.perusteenDiaarinumero == perusteenDiaarinumero

            )) ||
          // ...paitsi jos ne ovat toisistaan ajallisesti täysin erillään
          !Aikajakso(oo.alkamispäivä.getOrElse(LocalDate.of(0, 1, 1)), oo.päättymispäivä)
            .overlaps(Aikajakso(row.alkamispäivä.toLocalDate, row.päättymispäivä.map(_.toLocalDate)))
      case _ => true
    }
  }
}
