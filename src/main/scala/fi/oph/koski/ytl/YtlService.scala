package fi.oph.koski.ytl

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime}
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{KoskiTables, OpiskeluoikeusRow}
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, OppijaHenkilö}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.{JsonSerializer, SensitiveDataAllowed}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusQueryContext, OpiskeluoikeusQueryFilter, QueryOppijaHenkilö}
import fi.oph.koski.organisaatio.Oppilaitostyyppi
import fi.oph.koski.schema.{Henkilö, KoskiSchema}
import fi.oph.koski.util.Timing
import org.json4s.JsonAST.JValue
import org.json4s.MappingException
import rx.lang.scala.Observable

class YtlService(application: KoskiApplication) extends Logging with Timing {

  private lazy val opiskeluoikeudenTyyppiFilter =
    OpiskeluoikeusQueryFilter.OneOfOpiskeluoikeudenTyypit(
      YtlSchema.schemassaTuetutOpiskeluoikeustyypit.map(
        tyyppi => OpiskeluoikeusQueryFilter.OpiskeluoikeudenTyyppi(
          application.koodistoViitePalvelu.validateRequired("opiskeluoikeudentyyppi", tyyppi)
        )
      )
    )

  def streamOppijat(
    oidit: Seq[String],
    hetut: Seq[String],
    opiskeluoikeuksiaMuuttunutJälkeen: Option[Instant]
  )(implicit user: KoskiSpecificSession): Observable[JValue] = {
    val inputinKoko = oidit.length + hetut.length
    val blockName = "streamOppijat" + (inputinKoko match {
      case 1 => "1"
      case n if n <= 300 => "2To300"
      case n if n <= 600 => "301To600"
      case n if n <= 900 => "601To900"
      case n if n >= 1000 => "901To1000"
      case _ => throw new InternalError("Internal error")
    })
    timed(blockName, 10) {
      val pyydetytHenkilöt: Seq[OppijaHenkilö] =
        application.opintopolkuHenkilöFacade.findOppijatNoSlaveOids(oidit) ++
          application.opintopolkuHenkilöFacade.findOppijatByHetusNoSlaveOids(hetut)

      val oppijaOidToPyydettyHenkilö: Map[Henkilö.Oid, OppijaHenkilö] = pyydetytHenkilöt.map(h => h.oid -> h).toMap

      val pyydetytOppijaOidit: List[Henkilö.Oid] = oppijaOidToPyydettyHenkilö.keys.toList

      // Haku pitää tehdä master-oppijoiden kautta, jotta saadaan palautettua opiskeluoikeudet myös mahdollisilta
      // sisaruksena olevilta linkitetyiltä oppijoilta.
      val masterHenkilöt: Map[Henkilö.Oid, LaajatOppijaHenkilöTiedot] =
      application.opintopolkuHenkilöFacade.findMasterOppijat(pyydetytOppijaOidit)
      val masterOppijanLinkitetytOppijaOidit: Map[Henkilö.Oid, List[Henkilö.Oid]] = masterHenkilöt.collect {
        case (_, hlö) =>
          (hlö.oid, hlö.linkitetytOidit)
      }

      val masterOppijoidenOidit: Seq[Henkilö.Oid] = masterHenkilöt.values.map(_.oid).toSeq

      val haettavatOppijaOidit: Seq[Henkilö.Oid] =
        masterOppijoidenOidit ++ application.henkilöCache.resolveLinkedOids(masterOppijoidenOidit)

      val queryFilters = List(
        opiskeluoikeudenTyyppiFilter,
        OpiskeluoikeusQueryFilter.OppijaOidHaku(haettavatOppijaOidit)
      )

      def teePalautettavatYtlHenkilöt(tulosOppija: QueryOppijaHenkilö): Iterable[YtlHenkilö] = {
        // Luo YTLHenkilöt oppijoista, joilla oppijaa on alunperin pyydetty. Lista opiskeluoikeuksia palautetaan jokaiselle
        // niistä, jotta kutsuja saa oikean ja riittävän tiedon siitä, millä kutsujan antamilla oppija-oideilla
        // opiskeluoikeuksia löytyi.

        // Hieman epäloogisesti QueryOppijaHenkilö ei sisällä kaikkia oppijan linkitettyjä oideja, vaikka se
        // sen nimisen kentän sisältääkin: tietokantahausta tulee paluuarvona ainoastaan KOSKI-tietokannasta löytyviä
        // oppijaOideja. Siksi linkitetyt oidit pitää hakea erikseen.
        val etsittävätHenkilöOidit =
        Seq(tulosOppija.oid) ++ masterOppijanLinkitetytOppijaOidit(tulosOppija.oid)

        val pyynnössäEsiintyneetOppijaHenkilöt =
          oppijaOidToPyydettyHenkilö.collect { case (oid, hlö) if etsittävätHenkilöOidit.contains(oid) => hlö }

        pyynnössäEsiintyneetOppijaHenkilöt.map(hlö =>
          YtlHenkilö(
            hlö,
            hlö.äidinkieli.flatMap(k => application.koodistoViitePalvelu.validate("kieli", k.toUpperCase))
          )
        )
      }

      def teePalautettavatYtlOppijat(oppijaHenkilö: QueryOppijaHenkilö, opiskeluoikeusRows: List[OpiskeluoikeusRow]) = {
        // TODO: poista tämä paljon spämmäävä timed-lohko, kun suorituskykytestit tehty
        timed("teePalautettavatYtlOppijat", 1) {
          val oppijaOnLinkitetty = masterOppijanLinkitetytOppijaOidit(oppijaHenkilö.oid).nonEmpty

          lazy val haetaanIlmanAikaleimaaTaiOppijallaOnMuuttuneitaOpiskeluoikeuksia = opiskeluoikeuksiaMuuttunutJälkeen
            .map(Timestamp.from)
            .forall(opiskeluoikeuksiaMuuttunutJälkeen =>
              opiskeluoikeusRows.exists(!_.aikaleima.before(opiskeluoikeuksiaMuuttunutJälkeen))
            )

          lazy val haetaanAikaleimallaJaOppijallaOnUusiaMitätöityjäOpiskeluoikeuksia = opiskeluoikeuksiaMuuttunutJälkeen
            .map(Timestamp.from)
            .exists(opiskeluoikeuksiaMuuttunutJälkeen =>
              opiskeluoikeusRows.exists(oo => oo.mitätöity && !oo.aikaleima.before(opiskeluoikeuksiaMuuttunutJälkeen))
            )

          lazy val opiskeluoikeudet =
            opiskeluoikeusRows
              .filterNot(_.mitätöity)
              .map(toYtlOpiskeluoikeus)
              .flatMap(siivoaOpiskeluoikeus)

          if (oppijaOnLinkitetty || (
            haetaanIlmanAikaleimaaTaiOppijallaOnMuuttuneitaOpiskeluoikeuksia &&
              (opiskeluoikeudet.nonEmpty || haetaanAikaleimallaJaOppijallaOnUusiaMitätöityjäOpiskeluoikeuksia))
          ) {
            teePalautettavatYtlHenkilöt(oppijaHenkilö).map(ytlHenkilö =>
              Some(YtlOppija(
                henkilö = ytlHenkilö,
                opiskeluoikeudet = opiskeluoikeudet
              ))
            )
          } else {
            Seq.empty
          }
        }
      }

      OpiskeluoikeusQueryContext.streamingQueryGroupedByOid(application, queryFilters, None)
        .map {
          case (oppijaHenkilö: QueryOppijaHenkilö, opiskeluoikeusRows: List[OpiskeluoikeusRow]) =>
            teePalautettavatYtlOppijat(oppijaHenkilö, opiskeluoikeusRows)
        }
        .flatMap(oppijat => Observable.from(oppijat))
        .map(_.getOrElse(throw new InternalError("Internal error")))
        .doOnEach(auditLogOpiskeluoikeusKatsominen(_)(user))
        .map({
          // TODO: poista tämä paljon spämmäävä timed-lohko, kun suorituskykytestit tehty
          timed("serializeWithUser", 1) {
            JsonSerializer.serializeWithUser(user)
          }
        })
    }
  }

  private def toYtlOpiskeluoikeus(row: OpiskeluoikeusRow)(implicit user: SensitiveDataAllowed): YtlOpiskeluoikeus = {
    // TODO: poista tämä paljon spämmäävä timed-lohko, kun suorituskykytestit tehty
    timed("toYtlOpiskeluoikeus", 1) {
      deserializeYtlOpiskeluoikeus(row.data, row.oid, row.versionumero, row.aikaleima) match {
        case Right(oo) => oo
        case Left(errors) =>
          throw new MappingException(s"Error deserializing YTL opiskeluoikeus ${row.oid} for oppija ${row.oppijaOid}: ${errors}")
      }
    }
  }

  private def siivoaOpiskeluoikeus(oo: YtlOpiskeluoikeus): Option[YtlOpiskeluoikeus] = {
    val tarkistettavatOidit = oo.kaikkiMahdollisetOppilaitosOiditRakenteessa.toSet

    // TODO: organisaatiohierarkian läpikäynti saattaa olla sen verran hidasta, että tämä kannattaisi
    // siirtää organisaatioRepositoryyn ja tehdä sinne erillinen cache. Suorituskykytestataan nyt kuitenkin
    // ensin.
    // TODO: poista tämä paljon spämmäävä timed-lohko, kun suorituskykytestit tehty
    val onErityisoppilaitos = timed("onErityisoppilaitos", 1) {
      tarkistettavatOidit.exists(oid =>
        application.organisaatioService.organisaatioRepository.findWithOid(oid)
          .exists(_.children.exists(_.oppilaitostyyppi.contains(Oppilaitostyyppi.ammatillisetErityisoppilaitokset)))
      )
    }
    oo.siivoaTiedot(
      poistaOrganisaatiotiedot = onErityisoppilaitos
    )
  }

  private def deserializeYtlOpiskeluoikeus(data: JValue, oid: String, versionumero: Int, aikaleima: Timestamp): Either[HttpStatus, YtlOpiskeluoikeus] = {
    val json = KoskiTables.OpiskeluoikeusTable.readAsJValue(data, oid, versionumero, aikaleima)

    application.validatingAndResolvingExtractor.extract[YtlOpiskeluoikeus](
      KoskiSchema.lenientDeserializationWithIgnoringNonValidatingListItems
    )(json)
  }

  private def auditLogOpiskeluoikeusKatsominen(oppija: YtlOppija)(koskiSession: KoskiSpecificSession): Unit =
    AuditLog.log(KoskiAuditLogMessage(KoskiOperation.OPISKELUOIKEUS_KATSOMINEN, koskiSession, Map(KoskiAuditLogMessageField.oppijaHenkiloOid -> oppija.henkilö.oid)))
}

case class OidVersionTimestamp(oid: String, versionumero: Int, aikaleima: LocalDateTime)
