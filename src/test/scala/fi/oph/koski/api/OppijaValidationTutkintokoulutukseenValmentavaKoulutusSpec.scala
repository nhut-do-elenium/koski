package fi.oph.koski.api

import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory.fromAnyRef
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.documentation.ExamplesTutkintokoulutukseenValmentavaKoulutus._
import fi.oph.koski.documentation.LukioExampleData
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.{JsonFiles, JsonSerializer}
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.koskiuser.MockUsers.stadinAmmattiopistoTallentaja
import fi.oph.koski.oppija.HenkilönOpiskeluoikeusVersiot
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema._
import fi.oph.koski.validation.KoskiValidator
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class OppijaValidationTutkintokoulutukseenValmentavaKoulutusSpec extends AnyFreeSpec with PutOpiskeluoikeusTestMethods[TutkintokoulutukseenValmentavanOpiskeluoikeus] with KoskiHttpSpec {
  def tag = implicitly[reflect.runtime.universe.TypeTag[TutkintokoulutukseenValmentavanOpiskeluoikeus]]

  "Tutkintokoulutukseen valmentava koulutus" - {
    resetFixtures()

    "Suoritukset" - {
      "valmistuneen päätason suorituksen kesto ja osasuoritukset vaatimusten mukaiset" in {
        putOpiskeluoikeus(tuvaOpiskeluOikeusValmistunut, henkilö = tuvaHenkilöValmis, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }
      }

      "keskeneräisen päätason suorituksen kesto ja osasuoritukset vaatimusten mukaiset" in {
        putOpiskeluoikeus(tuvaOpiskeluOikeusEiValmistunut, henkilö = tuvaHenkilöEiValmis, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }
      }

      "suoritusten laajuudet lasketaan automaattisesti oikein" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = None).copy( // laajuus lasketaan ja täytetään automaattisesti
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenValinnaisenOsanSuoritus(
                  arviointiPäivä = Some(date(2021, 9, 1)),
                  laajuus = None // laajuus lasketaan ja täytetään automaattisesti
                ).copy(
                  osasuoritukset = Some(
                    List(
                      tuvaKoulutuksenValinnaisenOsanOsasuoritus(
                        kurssinNimi = "Ohjelmointi 1",
                        paikallinenKoodi = "ohj1",
                        paikallisenKoodinNimi = "Paikallinen ohjelmointikurssi",
                        laajuusViikoissa = 4
                      ),
                      tuvaKoulutuksenValinnaisenOsanOsasuoritus(
                        kurssinNimi = "Ohjelmointi 2",
                        paikallinenKoodi = "ohj2",
                        paikallisenKoodinNimi = "Paikallinen ohjelmointikurssi",
                        laajuusViikoissa = 4
                      ),
                    )
                  )
                )
              )
            )
          ))
        )

        val tuva = putAndGetOpiskeluoikeus(oo, tuvaHenkilöValmis)
        tuva.suoritukset.head.koulutusmoduuli.laajuusArvo(0) shouldBe 12.0
        tuva.suoritukset.head.osasuoritusLista.last.koulutusmoduuli.laajuusArvo(0.0) shouldBe 8.0
      }

      "valmistuneen päätason suorituksen laajuus liian pieni (ja osasuorituksia puuttuu)" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = Some(3)).copy(
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(laajuus = Some(1)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                )
              )
            )
          ))
        )

        putOpiskeluoikeus(oo, henkilö = tuvaHenkilöValmis, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.tuvaPäätasonSuoritusVääräLaajuus())
        }
      }

      "valmistuneen päätason suorituksen laajuus liian suuri" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = Some(39)).copy(
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(laajuus = Some(20)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(laajuus = Some(17)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                )
              )
            )
          ))
        )

        putOpiskeluoikeus(oo, henkilö = tuvaHenkilöValmis, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.tuvaPäätasonSuoritusVääräLaajuus())
        }
      }

      "valmistuneen päätason suorituksen osasuorituksen laajuus liian pieni" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = Some(4)).copy(
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = Some(1)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(laajuus = Some(1)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                )
              )
            )
          ))
        )

        putOpiskeluoikeus(oo, henkilö = tuvaHenkilöValmis, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatus(
            expectedStatus = 400,
            KoskiErrorCategory.badRequest.validation.laajuudet.tuvaOsaSuoritusVääräLaajuus(
              "Tutkintokoulutukseen valmentavan koulutuksen opiskelu- ja urasuunnittelutaitojen osasuorituksen laajuus on oltava vähintään 2 ja enintään 10 viikkoa."
            )
          )
        }
      }

      "valmistuneen päätason suorituksesta puuttuu opiskelu ja urasuunnittelutaitojen osasuoritus" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = Some(4)).copy(
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                )
              )
            )
          ))
        )

        putOpiskeluoikeus(oo, henkilö = tuvaHenkilöValmis, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuvaOpiskeluJaUrasuunnittelutaitojenOsasuoritusPuuttuu())
        }
      }

      "valmistuneen päätason suorituksesta puuttuu riittävä määrä eri osasuorituksia" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus(laajuus = Some(4)).copy(
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(laajuus = Some(2)),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                )
              )
            )
          ))
        )

        putOpiskeluoikeus(oo, henkilö = tuvaHenkilöValmis, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuvaOsasuorituksiaLiianVähän())
        }
      }
    }

    "Katsotaan eronneeksi tilaan päättyneellä opiskeluoikeudella ei saa olla arvioimattomia osasuorituksia" in {
      val oo = tuvaOpiskeluOikeusValmistunut.copy(
        tila = tuvaTilaKatsotaanEronneeksi,
        suoritukset = List(
          tuvaPäätasonSuoritus(laajuus = Some(12)).copy(
            vahvistus = None,
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaAmmatillisenKoulutuksenOpinnot(laajuus = Some(1)),
                  arviointiPäivä = Some(date(2021, 10, 1)),
                  koodistoviite = "tuvaammatillinenkoulutus"
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaLukiokoulutuksenOpinnot(laajuus = Some(1)),
                  arviointiPäivä = None,
                  koodistoviite = "tuvalukiokoulutus"
                ).copy(
                  tunnustettu = Some(
                    OsaamisenTunnustaminen(
                      osaaminen = Some(
                        LukioExampleData.kurssisuoritus(
                          LukioExampleData.valtakunnallinenKurssi("ENA1")
                        ).copy(arviointi = LukioExampleData.numeerinenArviointi(8))
                      ),
                      selite = finnish("Tunnustettu lukion kurssi")
                    )
                  )
                )
              )
            )
          )
        )
      )

      putOpiskeluoikeus(oo, henkilö = tuvaHenkilöValmis, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
        verifyResponseStatus(400,
          KoskiErrorCategory.badRequest.validation.tila.eronneeksiKatsotunOpiskeluoikeudenArvioinnit(
            "Katsotaan eronneeksi -tilaan päättyvällä opiskeluoikeudella ei saa olla osasuorituksia, joista puuttuu arviointi"
          ))
      }
    }

    "Opiskeluoikeudet" - {
      "opiskeluoikeuden järjestämislupa ei saa muuttua opiskeluoikeuden luonnin jälkeen" in {
        putOpiskeluoikeus(
          tuvaOpiskeluOikeusEiValmistunut
            .copy(järjestämislupa = Koodistokoodiviite("ammatillinen", "tuvajarjestamislupa"), lisätiedot = None),
          henkilö = tuvaHenkilöEiValmis,
          headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent
        ) {
          verifyResponseStatus(
            400,
            KoskiErrorCategory
              .badRequest(
                "Olemassaolevan tutkintokoulutukseen valmentavan koulutuksen opiskeluoikeuden järjestämislupaa ei saa muuttaa."
              )
          )
        }
      }
    }

    "Opiskeluoikeuden tila" - {
      """Opiskeluoikeuden tila "loma" sallitaan, kun opiskeluoikeuden järjestämislupa on ammatillisen koulutuksen järjestämisluvan piirissä""" in {
        putOpiskeluoikeus(
          tuvaOpiskeluOikeusLoma.copy(
            järjestämislupa = Koodistokoodiviite("ammatillinen", "tuvajarjestamislupa"),
            lisätiedot = Some(TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot(
              maksuttomuus = Some(
                List(
                  Maksuttomuus(
                    alku = date(2021, 8, 1),
                    loppu = None,
                    maksuton = true
                  )
                )
              )
            ))),
          henkilö = tuvaHenkilöLoma,
          headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent
        ) {
          verifyResponseStatusOk()
        }
      }
      """Opiskeluoikeuden tila ei saa olla "loma", kun opiskeluoikeuden järjestämislupa on jokin muu kuin ammatillisen koulutuksen järjestämisluvan piirissä""" in {
        putOpiskeluoikeus(
          tuvaOpiskeluOikeusLoma.copy(
            lisätiedot = Some(
              TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot(
                maksuttomuus = Some(
                  List(
                    Maksuttomuus(
                      alku = date(2021, 8, 1),
                      loppu = None,
                      maksuton = true
                    )
                  )
                )
              ))),
          henkilö = tuvaHenkilöLoma,
          headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent
        ) {
          verifyResponseStatus(
            400,
            KoskiErrorCategory
              .badRequest.validation.tila.tuvaSuorituksenOpiskeluoikeidenTilaVääräKoodiarvo(
              """Tutkintokoulutukseen valmentavan koulutuksen opiskeluoikeuden tila ei voi olla "loma", jos opiskeluoikeuden järjestämislupa ei ole ammatillisen koulutuksen järjestämisluvan piirissä."""
            )
          )
        }
      }
    }

    "Deserialisointi osaa päätellä skeeman ja täydentää optionaaliset @DefaultValuella annotoidut kentät, jos niiltä puuttuu arvo" in {
      val json = JsonFiles.readFile("src/test/resources/opiskeluoikeus_puuttuvilla_defaultvalue_propertyilla.json")
      val ooVersiot = putOppija(json) {
        verifyResponseStatusOk()
        JsonSerializer.parse[HenkilönOpiskeluoikeusVersiot](response.body)
      }
      val oo = getOpiskeluoikeus(ooVersiot.opiskeluoikeudet.last.oid)
      val lisätiedot = oo.lisätiedot.get.asInstanceOf[TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot]
      lisätiedot.pidennettyPäättymispäivä should equal(Some(false))
      lisätiedot.koulutusvienti should equal(Some(false))
    }

    "Erityisen tuen ja vammaisuuden jaksot perusopetuksen järjestämisluvalla" - {
      val alku = LocalDate.of(2021, 8, 5)

      val opiskeluoikeus = tuvaOpiskeluOikeusEiValmistunut
      val lisätiedot = opiskeluoikeus.lisätiedot.get.asInstanceOf[TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot].copy(
        maksuttomuus = None
      )

      "Validointi onnistuu, kun opiskeluoikeus sisältää erityisen tuen päätöksen mutta ei vammaisuusjaksoja" in {
        val oo = opiskeluoikeus.copy(
          lisätiedot = Some(
            lisätiedot.copy(
              erityisenTuenPäätökset = Some(List(
                TuvaErityisenTuenPäätös(
                  alku = Some(alku),
                  loppu = None
                ),
              )),
              vammainen = None,
              vaikeastiVammainen = None
            )
          )
        )

        validate(oo).isRight should equal(true)
      }

      "Validointi onnistu, kun vammaisuusjaksoja on lomittain ja ne kaikki osuvat johonkin lomittaiseen erityisen tuen päätökseen" in {
        val jakso1 = Aikajakso(alku.plusDays(0), Some(alku.plusDays(4)))
        val jakso2 = Aikajakso(alku.plusDays(3), Some(alku.plusDays(7)))
        val jakso3 = Aikajakso(alku.plusDays(6), Some(alku.plusDays(10)))

        val erityisenTuenPäätökset = List(jakso1, jakso2, jakso3).map(j => TuvaErityisenTuenPäätös.apply(Some(j.alku), j.loppu))

        val jakso4 = Aikajakso(alku.plusDays(1), Some(alku.plusDays(5)))
        val jakso5 = Aikajakso(alku.plusDays(9), Some(alku.plusDays(10)))

        val vammainenJaksot = List(jakso4, jakso5)

        val jakso6 = Aikajakso(alku.plusDays(6), Some(alku.plusDays(8)))
        val jakso7 = Aikajakso(alku.plusDays(8), Some(alku.plusDays(8)))

        val vaikeastiVammainenJaksot = List(jakso6, jakso7)

        val oo = opiskeluoikeus.copy(
          lisätiedot = Some(
            lisätiedot.copy(
              erityisenTuenPäätökset = Some(erityisenTuenPäätökset),
              vammainen = Some(vammainenJaksot),
              vaikeastiVammainen = Some(vaikeastiVammainenJaksot)
            )
          )
        )

        validate(oo).isRight should equal(true)
      }

      "Validointi ei onnistu, kun opiskeluoikeus sisältää osittain päällekäiset eri vammaisuuden jaksot" in {
        val jakso1 = Aikajakso(alku, None)
        val jakso2 = Aikajakso(alku.plusDays(5), Some(alku.plusDays(12)))

        val oo = opiskeluoikeus.copy(
          lisätiedot = Some(
            lisätiedot.copy(
              erityisenTuenPäätökset = Some(List(
                TuvaErityisenTuenPäätös(
                  alku = Some(alku),
                  loppu = None
                ),
              )),
              vammainen = Some(List(
                jakso1
              )),
              vaikeastiVammainen = Some(List(
                jakso2
              ))
            )
          )
        )

        validate(oo).left.get should equal(KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Vaikeasti vammaisuuden ja muun kuin vaikeasti vammaisuuden aikajaksot eivät voi olla voimassa samana päivänä"))
      }

      "Validointi onnistuu ennen rajapäivää, vaikka opiskeluoikeus sisältää osittain päällekäiset eri vammaisuuden jaksot" in {
        val jakso1 = Aikajakso(alku, None)
        val jakso2 = Aikajakso(alku.plusDays(5), Some(alku.plusDays(12)))

        val oo = opiskeluoikeus.copy(
          lisätiedot = Some(
            lisätiedot.copy(
              erityisenTuenPäätökset = Some(List(
                TuvaErityisenTuenPäätös(
                  alku = Some(alku),
                  loppu = None
                ),
              )),
              vammainen = Some(List(
                jakso1
              )),
              vaikeastiVammainen = Some(List(
                jakso2
              ))
            )
          )
        )

        validate(oo, 1).isRight should be(true)
      }

      "Validointi ei onnistu, kun vammaisuusjaksoja on osittain erityisen tuen päätösten ulkopuolella" in {
        val jakso1 = Aikajakso(alku, None)
        val jakso2 = Aikajakso(alku.plusDays(5), Some(alku.plusDays(12)))

        val oo = opiskeluoikeus.copy(
          lisätiedot = Some(
            lisätiedot.copy(
              erityisenTuenPäätökset = Some(List(
                TuvaErityisenTuenPäätös(
                  alku = Some(jakso2.alku),
                  loppu = jakso2.loppu
                ),
              )),
              vammainen = Some(List(
                jakso1
              ))
            )
          )
        )

        validate(oo).left.get should equal(KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso("Vammaisuusjaksot sisältävät päiviä, joina ei ole voimassaolevaa erityisen tuen jaksoa"))
      }

      def validate(oo: Opiskeluoikeus, voimaanastumispäivänOffsetTästäPäivästä: Long = 0): Either[HttpStatus, Oppija] = {
        val oppija = Oppija(defaultHenkilö, List(oo))

        implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
        implicit val accessType = AccessType.write

        val config = KoskiApplicationForTests.config.withValue("validaatiot.pidennetynOppivelvollisuudenYmsValidaatiotAstuvatVoimaan", fromAnyRef(LocalDate.now().plusDays(voimaanastumispäivänOffsetTästäPäivästä).toString))

        mockKoskiValidator(config).updateFieldsAndValidateAsJson(oppija)
      }

      def mockKoskiValidator(config: Config) = {
        new KoskiValidator(
          KoskiApplicationForTests.organisaatioRepository,
          KoskiApplicationForTests.possu,
          KoskiApplicationForTests.henkilöRepository,
          KoskiApplicationForTests.ePerusteetValidator,
          KoskiApplicationForTests.ePerusteetFiller,
          KoskiApplicationForTests.validatingAndResolvingExtractor,
          KoskiApplicationForTests.suostumuksenPeruutusService,
          config
        )
      }

    }
  }

  def putAndGetOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus, henkilö: Henkilö): TutkintokoulutukseenValmentavanOpiskeluoikeus = putOpiskeluoikeus(
    oo,
    henkilö = henkilö,
    headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent
  ) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }.asInstanceOf[TutkintokoulutukseenValmentavanOpiskeluoikeus]

  override def defaultOpiskeluoikeus: TutkintokoulutukseenValmentavanOpiskeluoikeus = tuvaOpiskeluOikeusValmistunut
}
