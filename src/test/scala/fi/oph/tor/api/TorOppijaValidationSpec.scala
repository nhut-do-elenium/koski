package fi.oph.tor.api

import java.time.LocalDate

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.json.Json
import fi.oph.tor.oppija.MockOppijat
import fi.oph.tor.schema._
import fi.oph.tor.toruser.MockUsers
import org.json4s.JValue
import org.json4s.JsonAST.JObject
import org.scalatest.FunSpec

class TorOppijaValidationSpec extends FunSpec with OpiskeluOikeusTestMethods {
  describe("Opiskeluoikeuden lisääminen") {
    describe("Valideilla tiedoilla") {
      it("palautetaan HTTP 200") {
        putOpiskeluOikeus(JObject()) {
          verifyResponseStatus(200)
        }
      }
    }

    describe("Ilman tunnistautumista") {
      it("palautetaan HTTP 401") {
        putOpiskeluOikeus(JObject(), headers = jsonContent) {
          verifyResponseStatus(401, TorErrorCategory.unauthorized("Käyttäjä ei ole tunnistautunut."))
        }
      }
    }

    describe("Epäkelpo JSON-dokumentti") {
      it("palautetaan HTTP 400 virhe" ) (request("api/oppija", "application/json", "not json", "put")
        (verifyResponseStatus(400, TorErrorCategory.badRequest.format.json("Invalid JSON"))))
    }

    describe("Väärä Content-Type") {
      it("palautetaan HTTP 415") {

        put("api/oppija", body = Json.write(makeOppija(defaultHenkilö, List(opiskeluoikeus()))), headers = authHeaders() ++ Map(("Content-type" -> "text/plain"))) {
          verifyResponseStatus(415, TorErrorCategory.unsupportedMediaType.jsonOnly("Wrong content type: only application/json content type allowed"))
        }
      }
    }

    describe("Henkilötiedot") {
      describe("Nimenä tyhjä merkkijono") {
        it("palautetaan HTTP 400 virhe" ) (putHenkilö(defaultHenkilö.copy(sukunimi = "")) (verifyResponseStatus(400)))
      }


      describe("Hetun ollessa") {
        describe("muodoltaan virheellinen") {
          it("palautetaan HTTP 400 virhe" ) (putHenkilö(defaultHenkilö.copy(hetu = "010101-123123"))
            (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.henkilötiedot.hetu("Virheellinen muoto hetulla: 010101-123123"))))
        }
        describe("muodoltaan oikea, mutta väärä tarkistusmerkki") {
          it("palautetaan HTTP 400 virhe" ) (putHenkilö(defaultHenkilö.copy(hetu = "010101-123P"))
            (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.henkilötiedot.hetu("Virheellinen tarkistusmerkki hetussa: 010101-123P"))))
        }
        describe("päivämäärältään tulevaisuudessa") {
          it("palautetaan HTTP 400 virhe" ) (putHenkilö(defaultHenkilö.copy(hetu = "141299A903C"))
            (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.henkilötiedot.hetu("Syntymäpäivä hetussa: 141299A903C on tulevaisuudessa"))))
        }
        describe("päivämäärältään virheellinen") {
          it("palautetaan HTTP 400 virhe" ) (putHenkilö(defaultHenkilö.copy(hetu = "300215-123T"))
            (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.henkilötiedot.hetu("Virheellinen syntymäpäivä hetulla: 300215-123T"))))
        }
        describe("validi") {
          it("palautetaan HTTP 200" ) (putHenkilö(defaultHenkilö.copy(hetu = "010101-123N"))
            (verifyResponseStatus(200)))
        }
      }

      describe("Käytettäessä oppijan oidia") {
        describe("Oid ok") {
          it("palautetaan HTTP 200" ) (putHenkilö(OidHenkilö(MockOppijat.eero.oid)) (verifyResponseStatus(200)))
        }

        describe("Oid virheellinen") {
          it("palautetaan HTTP 400" ) (putHenkilö(OidHenkilö("123.123.123")) (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.jsonSchema(".*ECMA 262 regex.*".r))))
        }

        describe("Oppijaa ei löydy oidilla") {
          it("palautetaan HTTP 404" ) (putHenkilö(OidHenkilö("1.2.246.562.24.19999999999")) (verifyResponseStatus(404, TorErrorCategory.notFound.oppijaaEiLöydy("Oppijaa 1.2.246.562.24.19999999999 ei löydy."))))
        }
      }

      describe("Käytettäessä oppijan kaikkia tietoja") {
        describe("Oid ok") {
          it("palautetaan HTTP 200" ) (putHenkilö(FullHenkilö(MockOppijat.eero.oid, "010101-123N", "Testi", "Testi", "Toivola", None, None)) (verifyResponseStatus(200)))
        }

        describe("Oid virheellinen") {
          it("palautetaan HTTP 400" ) (putHenkilö(FullHenkilö("123.123.123", "010101-123N", "Testi", "Testi", "Toivola", None, None)) (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.jsonSchema(".*ECMA 262 regex.*".r))))
        }
      }
    }

    describe("Opiskeluoikeudet") {
      describe("Jos lähetetään 0 opiskeluoikeutta") {
        it("palautetaan HTTP 400") {
          putOppija(TorOppija(defaultHenkilö, List())) {
            verifyResponseStatus(400, TorErrorCategory.badRequest.validation.tyhjäOpiskeluoikeusLista("Annettiin tyhjä lista opiskeluoikeuksia."))
          }
        }
      }

      describe("Päivitettäessä opiskeluoikeus käyttäen sen id:tä") {
        it("Id ok") {
          val opiskeluoikeus = lastOpiskeluOikeus(MockOppijat.eero.oid)
          putOppija(TorOppija(MockOppijat.eero, List(opiskeluoikeus))) {
            verifyResponseStatus(200)
          }
        }

        it("Tuntematon id") {
          val opiskeluoikeus = lastOpiskeluOikeus(MockOppijat.eero.oid)
          putOppija(TorOppija(MockOppijat.eero, List(opiskeluoikeus.withIdAndVersion(id = Some(0), versionumero = None)))) {
            verifyResponseStatus(404, TorErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta 0 ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
          }
        }
      }
    }

    describe("Oppilaitos") {
      def oppilaitoksella(oid: String) = Json.toJValue(opiskeluoikeus().copy(oppilaitos = Oppilaitos(oid)))

      describe("Kun opinto-oikeutta yritetään lisätä oppilaitokseen, johon käyttäjällä ei ole pääsyä") {
        it("palautetaan HTTP 403 virhe" ) { putOpiskeluOikeus(oppilaitoksella("1.2.246.562.10.93135224694"), headers = authHeaders(MockUsers.hiiri) ++ jsonContent) (
          verifyResponseStatus(403, TorErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon 1.2.246.562.10.93135224694")))
        }
      }

      describe("Kun opinto-oikeutta yritetään lisätä oppilaitokseen, joka ei ole oppilaitos") {
        it("palautetaan HTTP 400 virhe" ) { putOpiskeluOikeus(oppilaitoksella("1.2.246.562.10.346830761110")) (
          verifyResponseStatus(400, TorErrorCategory.badRequest.validation.organisaatio.vääränTyyppinen("Organisaatio 1.2.246.562.10.346830761110 ei ole Oppilaitos")))
        }
      }

      describe("Kun opinto-oikeutta yritetään lisätä oppilaitokseen, jota ei löydy organisaatiopalvelusta") {
        it("palautetaan HTTP 400 virhe" ) { putOpiskeluOikeus(oppilaitoksella("1.2.246.562.10.146810761111")) (
          verifyResponseStatus(400, TorErrorCategory.badRequest.validation.organisaatio.tuntematon("Organisaatiota 1.2.246.562.10.146810761111 ei löydy organisaatiopalvelusta")))
        }
      }

      describe("Kun oppilaitoksen oid on virheellistä muotoa") {
        it("palautetaan HTTP 400 virhe" ) { putOpiskeluOikeus(oppilaitoksella("asdf")) (
          verifyResponseStatus(400, TorErrorCategory.badRequest.validation.jsonSchema(".*ECMA 262 regex.*".r)))
        }
      }
    }

    describe("Tutkinnon perusteet ja rakenne") {
      describe("Kun yritetään lisätä opinto-oikeus tuntemattomaan tutkinnon perusteeseen") {
        it("palautetaan HTTP 400 virhe" ) {
          val suoritus: AmmatillinenTutkintoSuoritus = tutkintoSuoritus.copy(koulutusmoduuli = TutkintoKoulutus(KoodistoKoodiViite("351301", "koulutus"), Some("39/xxx/2014")))
          putTutkintoSuoritus(suoritus) (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Tutkinnon perustetta ei löydy diaarinumerolla 39/xxx/2014")))
        }
      }

      describe("Kun yritetään lisätä opinto-oikeus ilman tutkinnon perusteen diaarinumeroa") {
        it("palautetaan HTTP 200" ) {
          val suoritus: AmmatillinenTutkintoSuoritus = tutkintoSuoritus.copy(koulutusmoduuli = TutkintoKoulutus(KoodistoKoodiViite("351301", "koulutus"), None))
          putTutkintoSuoritus(suoritus) (verifyResponseStatus(200))
        }
      }

      describe("Kun yritetään lisätä opinto-oikeus tyhjällä diaarinumerolla") {
        it("palautetaan HTTP 400 virhe" ) {
          val suoritus = tutkintoSuoritus.copy(koulutusmoduuli = TutkintoKoulutus(KoodistoKoodiViite("351301", "koulutus"), Some("")))

          putTutkintoSuoritus(suoritus) (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.jsonSchema(".*perusteenDiaarinumero.*".r)))
        }
      }

      describe("Osaamisala ja suoritustapa") {
        describe("Osaamisala ja suoritustapa ok") {
          val suoritus = tutkintoSuoritus.copy(
            suoritustapa = Some(Suoritustapa(KoodistoKoodiViite("ops", "suoritustapa"))),
            osaamisala = Some(List(KoodistoKoodiViite("1527", "osaamisala"))))

          it("palautetaan HTTP 200") (putTutkintoSuoritus(suoritus)(verifyResponseStatus(200)))
        }
        describe("Suoritustapa virheellinen") {
          val suoritus = tutkintoSuoritus.copy(
            suoritustapa = Some(Suoritustapa(KoodistoKoodiViite("blahblahtest", "suoritustapa"))),
            osaamisala = Some(List(KoodistoKoodiViite("1527", "osaamisala"))))

          it("palautetaan HTTP 400") (putTutkintoSuoritus(suoritus)(verifyResponseStatus(400, TorErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia suoritustapa/blahblahtest ei löydy koodistosta"))))
        }
        describe("Osaamisala ei löydy tutkintorakenteesta") {
          val suoritus = tutkintoSuoritus.copy(
            suoritustapa = Some(Suoritustapa(KoodistoKoodiViite("ops", "suoritustapa"))),
            osaamisala = Some(List(KoodistoKoodiViite("3053", "osaamisala"))))

          it("palautetaan HTTP 400") (putTutkintoSuoritus(suoritus) (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.rakenne.tuntematonOsaamisala("Osaamisala 3053 ei löydy tutkintorakenteesta perusteelle 39/011/2014"))))
        }
        describe("Osaamisala virheellinen") {
          val suoritus = tutkintoSuoritus.copy(
            suoritustapa = Some(Suoritustapa(KoodistoKoodiViite("ops", "suoritustapa"))),
            osaamisala = Some(List(KoodistoKoodiViite("0", "osaamisala"))))

          it("palautetaan HTTP 400")(putTutkintoSuoritus(suoritus)(verifyResponseStatus(400, TorErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia osaamisala/0 ei löydy koodistosta"))))
        }
      }

      describe("Tutkinnon osat ja arvionnit") {
        val johtaminenJaHenkilöstönKehittäminen: OpsTutkinnonosa = OpsTutkinnonosa(KoodistoKoodiViite("104052", "tutkinnonosat"), true, None, None, None)

        describe("OPS-perusteinen tutkinnonosa") {
          describe("Tutkinnon osa ja arviointi ok") {
            it("palautetaan HTTP 200") (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus, tutkinnonSuoritustapaNäyttönä) (verifyResponseStatus(200)))
          }

          describe("Tutkinnon osa ei kuulu tutkintorakenteeseen") {
            it("palautetaan HTTP 400") (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(koulutusmoduuli = johtaminenJaHenkilöstönKehittäminen), tutkinnonSuoritustapaNäyttönä)(
              verifyResponseStatus(400, TorErrorCategory.badRequest.validation.rakenne.tuntematonTutkinnonOsa("Tutkinnon osa tutkinnonosat/104052 ei löydy tutkintorakenteesta perusteelle 39/011/2014 - suoritustapa naytto"))))
          }

          describe("Tutkinnon osaa ei ei löydy koodistosta") {
            it("palautetaan HTTP 400") (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus.copy(
              koulutusmoduuli = OpsTutkinnonosa(KoodistoKoodiViite("9923123", "tutkinnonosat"), true, None, None, None)), tutkinnonSuoritustapaNäyttönä)
              (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia tutkinnonosat/9923123 ei löydy koodistosta"))))
          }
        }

        describe("Paikallinen tutkinnonosa") {
          describe("Tutkinnon osa ja arviointi ok") {
            val suoritus = paikallinenTutkinnonOsaSuoritus
            it("palautetaan HTTP 200") (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä) (verifyResponseStatus(200)))
          }

          describe("Laajuus negatiivinen") {
            val suoritus = paikallinenTutkinnonOsaSuoritus.copy(koulutusmoduuli = paikallinenTutkinnonOsa.copy(laajuus = Some(laajuus.copy(arvo = -1))))
            it("palautetaan HTTP 400") (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä) (
              verifyResponseStatus(400, TorErrorCategory.badRequest.validation.jsonSchema(".*numeric instance is lower than the required minimum.*".r)))
            )
          }
        }

        describe("Tutkinnon osa toisesta tutkinnosta") {
          val autoalanTyönjohdonErikoisammattitutkinto: TutkintoKoulutus = TutkintoKoulutus(KoodistoKoodiViite("357305", "koulutus"), Some("40/011/2001"))

          def osanSuoritusToisestaTutkinnosta(tutkinto: TutkintoKoulutus, tutkinnonOsa: OpsTutkinnonosa): AmmatillinenOpsTutkinnonosaSuoritus = tutkinnonOsaSuoritus.copy(
            tutkinto = Some(tutkinto),
            koulutusmoduuli = tutkinnonOsa
          )

          describe("Kun tutkinto löytyy ja osa kuuluu sen rakenteeseen") {
            val suoritus = osanSuoritusToisestaTutkinnosta(autoalanTyönjohdonErikoisammattitutkinto, johtaminenJaHenkilöstönKehittäminen)
            it("palautetaan HTTP 200") (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(
              verifyResponseStatus(200)))
          }

          describe("Kun tutkintoa ei löydy") {
            val suoritus = osanSuoritusToisestaTutkinnosta(TutkintoKoulutus(KoodistoKoodiViite("123456", "koulutus"), Some("40/011/2001")), johtaminenJaHenkilöstönKehittäminen)
            it("palautetaan HTTP 400") (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(
              verifyResponseStatus(400, TorErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia koulutus/123456 ei löydy koodistosta"))))
          }

          describe("Kun osa ei kuulu annetun tutkinnon rakenteeseen") {
            val suoritus = osanSuoritusToisestaTutkinnosta(autoalanPerustutkinto, johtaminenJaHenkilöstönKehittäminen)
            it("palautetaan HTTP 200 (ei validoida rakennetta tässä)") (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(
              verifyResponseStatus(200)))
          }

          describe("Kun tutkinnolla ei ole diaarinumeroa") {
            val suoritus = osanSuoritusToisestaTutkinnosta(autoalanTyönjohdonErikoisammattitutkinto.copy(perusteenDiaarinumero = None), johtaminenJaHenkilöstönKehittäminen)
            it("palautetaan HTTP 200 (diaarinumeroa ei vaadita)") (putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(
                verifyResponseStatus(200)))
          }

          describe("Kun tutkinnon diaarinumero on virheellinen") {
            it("palautetaan HTTP 400") (putTutkinnonOsaSuoritus(osanSuoritusToisestaTutkinnosta(
              autoalanTyönjohdonErikoisammattitutkinto.copy(perusteenDiaarinumero = Some("Boom boom kah")),
              johtaminenJaHenkilöstönKehittäminen), tutkinnonSuoritustapaNäyttönä)(
                verifyResponseStatus(400, TorErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Tutkinnon perustetta ei löydy diaarinumerolla Boom boom kah"))))
          }
        }

        describe("Suoritustapa puuttuu") {
          it("palautetaan HTTP 400") (putTutkinnonOsaSuoritus(tutkinnonOsaSuoritus, None) {
            verifyResponseStatus(400, TorErrorCategory.badRequest.validation.rakenne.suoritustapaPuuttuu("Tutkinnolta puuttuu suoritustapa. Tutkinnon osasuorituksia ei hyväksytä."))
          })
        }

        describe("Suorituksen tila") {
          testSuorituksenTila[AmmatillinenOpsTutkinnonosaSuoritus](tutkinnonOsaSuoritus, "tutkinnonosat/100023", { suoritus => { f => putTutkinnonOsaSuoritus(suoritus, tutkinnonSuoritustapaNäyttönä)(f)} })

          describe("Kun tutkinto on VALMIS-tilassa ja sillä on osa, joka on KESKEN-tilassa") {
            val opiskeluOikeus: JValue = opiskeluoikeus().copy(suoritukset = List(tutkintoSuoritus.copy(
              suoritustapa = tutkinnonSuoritustapaNäyttönä, tila = tilaValmis, vahvistus = vahvistus,osasuoritukset = Some(List(tutkinnonOsaSuoritus))
            )))

            it("palautetaan HTTP 400") (putOpiskeluOikeus(opiskeluOikeus) (
              verifyResponseStatus(400, TorErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Suorituksella koulutus/351301 on keskeneräinen osasuoritus tutkinnonosat/100023 vaikka suorituksen tila on VALMIS"))))
          }
        }
      }
    }

    describe("Tutkinnon tila ja arviointi") {
      testSuorituksenTila[AmmatillinenTutkintoSuoritus](tutkintoSuoritus, "koulutus/351301", { suoritus => { f => {
        putOpiskeluOikeus(opiskeluoikeus().copy(suoritukset = List(suoritus)))(f)
      }}})
    }

    describe("Oppisopimus") {
      def toteutusOppisopimuksella(yTunnus: String): AmmatillinenTutkintoSuoritus = {
        tutkintoSuoritus.copy(järjestämismuoto = Some(OppisopimuksellinenJärjestämismuoto(KoodistoKoodiViite("20", "jarjestamismuoto"), Oppisopimus(Yritys("Reaktor", yTunnus)))))
      }

      describe("Kun ok") {
        it("palautetaan HTTP 200") (
          putOpiskeluOikeus(opiskeluoikeus(toteutusOppisopimuksella("1629284-5")))
            (verifyResponseStatus(200))
        )
      }

      describe("Virheellinen y-tunnus") {
        it("palautetaan HTTP 400") (
          putOpiskeluOikeus(opiskeluoikeus(toteutusOppisopimuksella("1629284x5")))
            (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.jsonSchema(".*ECMA 262 regex.*".r)))
        )
      }
    }

    describe("Opiskeluoikeuden päivämäärät") {
      describe("Päivämäärät kunnossa") {
        it("palautetaan HTTP 200" ) (putOpiskeluOikeus(Map(
          "alkamispäivä" -> "2015-08-01",
          "päättymispäivä" -> "2016-05-31",
          "arvioituPäättymispäivä" -> "2018-05-31"
        ))(verifyResponseStatus(200)))
      }
      describe("Päivämääräformaatti virheellinen") {
        it("palautetaan HTTP 400" ) (putOpiskeluOikeus(Map(
          "alkamispäivä" -> "2015.01-12"
        ))(verifyResponseStatus(400, TorErrorCategory.badRequest.format.pvm("Virheellinen päivämäärä: 2015.01-12"))))
      }
      describe("Päivämäärä virheellinen") {
        it("palautetaan HTTP 400" ) (putOpiskeluOikeus(Map(
          "alkamispäivä" -> "2015-01-32"
        ))(verifyResponseStatus(400, TorErrorCategory.badRequest.format.pvm("Virheellinen päivämäärä: 2015-01-32"))))
      }
      describe("Väärä päivämääräjärjestys") {
        it("alkamispäivä > päättymispäivä" ) (putOpiskeluOikeus(Map(
          "alkamispäivä" -> "2015-08-01",
          "päättymispäivä" -> "2014-05-31"
        ))(verifyResponseStatus(400, TorErrorCategory.badRequest.validation.date.loppuEnnenAlkua("alkamispäivä (2015-08-01) oltava sama tai aiempi kuin päättymispäivä(2014-05-31)"))))

        it("alkamispäivä > arvioituPäättymispäivä" ) (putOpiskeluOikeus(Map(
          "alkamispäivä" -> "2015-08-01",
          "arvioituPäättymispäivä" -> "2014-05-31"
        ))(verifyResponseStatus(400, TorErrorCategory.badRequest.validation.date.loppuEnnenAlkua("alkamispäivä (2015-08-01) oltava sama tai aiempi kuin arvioituPäättymispäivä(2014-05-31)"))))
      }
    }

    describe("Opiskeluoikeusjaksot"){
      describe("Päivämäärät kunnossa") {
        it("palautetaan HTTP 200") (putOpiskeluOikeus(Map("opiskeluoikeudenTila" -> Map("opiskeluoikeusjaksot" -> List(
          Map( "alku" -> "2015-08-01", "loppu" -> "2015-12-31", "tila" -> Map("koodiarvo" -> "aktiivinen", "koodistoUri" -> "opiskeluoikeudentila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "keskeyttanyt", "koodistoUri" -> "opiskeluoikeudentila")),
          Map( "alku" -> "2016-06-01", "tila" -> Map("koodiarvo" -> "paattynyt", "koodistoUri" -> "opiskeluoikeudentila"))
        )))) (verifyResponseStatus(200)))
      }
      describe("alku > loppu") {
        it("palautetaan HTTP 400") (putOpiskeluOikeus(Map("opiskeluoikeudenTila" -> Map("opiskeluoikeusjaksot" -> List(
          Map( "alku" -> "2016-08-01", "loppu" -> "2015-12-31", "tila" -> Map("koodiarvo" -> "aktiivinen", "koodistoUri" -> "opiskeluoikeudentila"))
        )))) (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.date.loppuEnnenAlkua(
          "opiskeluoikeudenTila.opiskeluoikeusjaksot.alku (2016-08-01) oltava sama tai aiempi kuin opiskeluoikeudenTila.opiskeluoikeusjaksot.loppu(2015-12-31)"))))
      }
      describe("ei-viimeiseltä jaksolta puuttuu loppupäivä") {
        it("palautetaan HTTP 400") (putOpiskeluOikeus(Map("opiskeluoikeudenTila" -> Map("opiskeluoikeusjaksot" -> List(
          Map("alku" -> "2015-08-01", "tila" -> Map("koodiarvo" -> "aktiivinen", "koodistoUri" -> "opiskeluoikeudentila")),
          Map("alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "keskeyttanyt", "koodistoUri" -> "opiskeluoikeudentila"))
        )))) (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.date.jaksonLoppupäiväPuuttuu("opiskeluoikeudenTila.opiskeluoikeusjaksot: ei-viimeiseltä jaksolta puuttuu loppupäivä")))) }
      describe("jaksot ovat päällekkäiset") {
        it("palautetaan HTTP 400") (putOpiskeluOikeus(Map("opiskeluoikeudenTila" -> Map("opiskeluoikeusjaksot" -> List(
          Map( "alku" -> "2015-08-01", "loppu" -> "2016-01-01", "tila" -> Map("koodiarvo" -> "aktiivinen", "koodistoUri" -> "opiskeluoikeudentila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "keskeyttanyt", "koodistoUri" -> "opiskeluoikeudentila"))
        ))))(verifyResponseStatus(400, TorErrorCategory.badRequest.validation.date.jaksotEivätMuodostaJatkumoa("opiskeluoikeudenTila.opiskeluoikeusjaksot: jaksot eivät muodosta jatkumoa"))))
      }
      describe("jaksojen väliin jää tyhjää") {
        it("palautetaan HTTP 400") (putOpiskeluOikeus(Map("opiskeluoikeudenTila" -> Map("opiskeluoikeusjaksot" -> List(
          Map( "alku" -> "2015-08-01", "loppu" -> "2015-10-01", "tila" -> Map("koodiarvo" -> "aktiivinen", "koodistoUri" -> "opiskeluoikeudentila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "keskeyttanyt", "koodistoUri" -> "opiskeluoikeudentila"))
        ))))(verifyResponseStatus(400, TorErrorCategory.badRequest.validation.date.jaksotEivätMuodostaJatkumoa("opiskeluoikeudenTila.opiskeluoikeusjaksot: jaksot eivät muodosta jatkumoa"))))
      }
    }

    describe("Läsnäolojaksot") {
      describe("Päivämäärät kunnossa") {
        it("palautetaan HTTP 200") (putOpiskeluOikeus(Map("läsnäolotiedot" -> Map("läsnäolojaksot" -> List(
          Map( "alku" -> "2015-08-01", "loppu" -> "2015-12-31", "tila" -> Map("koodiarvo" -> "lasna", "koodistoUri" -> "lasnaolotila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "poissa", "koodistoUri" -> "lasnaolotila")),
          Map( "alku" -> "2016-06-01", "tila" -> Map("koodiarvo" -> "lasna", "koodistoUri" -> "lasnaolotila"))
        ))))(verifyResponseStatus(200)))
      }
      describe("alku > loppu") {
        it("palautetaan HTTP 400") (putOpiskeluOikeus(Map("läsnäolotiedot" -> Map("läsnäolojaksot" -> List(
          Map( "alku" -> "2016-08-01", "loppu" -> "2015-12-31", "tila" -> Map("koodiarvo" -> "lasna", "koodistoUri" -> "lasnaolotila"))
        ))))(verifyResponseStatus(400, TorErrorCategory.badRequest.validation.date.loppuEnnenAlkua("läsnäolotiedot.läsnäolojaksot.alku (2016-08-01) oltava sama tai aiempi kuin läsnäolotiedot.läsnäolojaksot.loppu(2015-12-31)"))))
      }
      describe("ei-viimeiseltä jaksolta puuttuu loppupäivä") {
        it("palautetaan HTTP 400") (putOpiskeluOikeus(Map("läsnäolotiedot" -> Map("läsnäolojaksot" -> List(
          Map( "alku" -> "2015-08-01", "tila" -> Map("koodiarvo" -> "lasna", "koodistoUri" -> "lasnaolotila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "poissa", "koodistoUri" -> "lasnaolotila"))
        ))))(verifyResponseStatus(400, TorErrorCategory.badRequest.validation.date.jaksonLoppupäiväPuuttuu("läsnäolotiedot.läsnäolojaksot: ei-viimeiseltä jaksolta puuttuu loppupäivä"))))
      }
      describe("jaksot ovat päällekkäiset") {
        it("palautetaan HTTP 400") (putOpiskeluOikeus(Map("läsnäolotiedot" -> Map("läsnäolojaksot" -> List(
          Map( "alku" -> "2015-08-01", "loppu" -> "2016-01-01", "tila" -> Map("koodiarvo" -> "lasna", "koodistoUri" -> "lasnaolotila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "poissa", "koodistoUri" -> "lasnaolotila"))
        ))))(verifyResponseStatus(400, TorErrorCategory.badRequest.validation.date.jaksotEivätMuodostaJatkumoa("läsnäolotiedot.läsnäolojaksot: jaksot eivät muodosta jatkumoa"))))
      }
      describe("jaksojen väliin jää tyhjää") {
        it("palautetaan HTTP 400") (putOpiskeluOikeus(Map("läsnäolotiedot" -> Map("läsnäolojaksot" -> List(
          Map( "alku" -> "2015-08-01", "loppu" -> "2015-10-01", "tila" -> Map("koodiarvo" -> "lasna", "koodistoUri" -> "lasnaolotila")),
          Map( "alku" -> "2016-01-01", "loppu" -> "2016-05-31", "tila" -> Map("koodiarvo" -> "poissa", "koodistoUri" -> "lasnaolotila"))
        ))))(verifyResponseStatus(400, TorErrorCategory.badRequest.validation.date.jaksotEivätMuodostaJatkumoa("läsnäolotiedot.läsnäolojaksot: jaksot eivät muodosta jatkumoa"))))
      }
    }
  }

  def testSuorituksenTila[T <: Suoritus](suoritus: T, desc: String, put: (T => ((=> Unit) => Unit))): Unit = {
    def copySuoritus(suoritus: T, t: KoodistoKoodiViite, a: Option[List[Arviointi]], v: Option[Vahvistus], ap: Option[LocalDate] = None): T = {
      val alkamispäivä = ap.orElse(suoritus.alkamispäivä)
      (suoritus match {
        case s: AmmatillinenTutkintoSuoritus => s.copy(tila = t, arviointi = a, vahvistus = v, alkamispäivä = alkamispäivä)
        case s: AmmatillinenOpsTutkinnonosaSuoritus => s.copy(tila = t, arviointi = a, vahvistus = v, alkamispäivä = alkamispäivä)
        case s: AmmatillinenPaikallinenTutkinnonosaSuoritus => s.copy(tila = t, arviointi = a, vahvistus = v, alkamispäivä = alkamispäivä)
      }).asInstanceOf[T]
    }


    def testKesken(tila: KoodistoKoodiViite): Unit = {
      describe("Arviointi puuttuu") {
        it("palautetaan HTTP 200") (put(copySuoritus(suoritus, tila, None, None)) (
          verifyResponseStatus(200)
        ))
      }
      describe("Arviointi annettu") {
        it("palautetaan HTTP 200") (put(copySuoritus(suoritus, tila, arviointiHyvä(), None)) (
          verifyResponseStatus(200)
        ))
      }
      describe("Vahvistus annettu") {
        it("palautetaan HTTP 400") (put(copySuoritus(suoritus, tila, arviointiHyvä(), vahvistus)) (
          verifyResponseStatus(400, TorErrorCategory.badRequest.validation.tila.vahvistusVäärässäTilassa("Suorituksella " + desc + " on vahvistus, vaikka suorituksen tila on " + tila.koodiarvo))
        ))
      }
    }
    describe("Kun suorituksen tila on KESKEN") {
      testKesken(tilaKesken)
    }

    describe("Kun suorituksen tila on KESKEYTYNYT") {
      testKesken(tilaKesken)
    }

    describe("Kun suorituksen tila on VALMIS") {
      describe("Suorituksella arviointi ja vahvistus") {
        it("palautetaan HTTP 200") (put(copySuoritus(suoritus, tilaValmis, arviointiHyvä(), vahvistus)) (
          verifyResponseStatus(200)
        ))
      }
      describe("Vahvistus annettu, mutta arviointi puuttuu") {
        it("palautetaan HTTP 200") (put(copySuoritus(suoritus, tilaValmis, None, vahvistus)) (
          verifyResponseStatus(200)
        ))
      }

      describe("Vahvistus puuttuu") {
        it("palautetaan HTTP 400") (put(copySuoritus(suoritus, tilaValmis, arviointiHyvä(), None)) (
          verifyResponseStatus(400, TorErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta " + desc + " puuttuu vahvistus, vaikka suorituksen tila on VALMIS"))
        ))
      }
    }

    describe("Arviointi") {
      describe("Arviointiasteikko on tuntematon") {
        it("palautetaan HTTP 400") (put(copySuoritus(suoritus, suoritus.tila, Some(List(Arviointi(KoodistoKoodiViite("2", "vääräasteikko"), None))), None))
          (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.jsonSchema(".*not found in enum.*".r))))
      }

      describe("Arvosana ei kuulu perusteiden mukaiseen arviointiasteikkoon") {
        it("palautetaan HTTP 400") (put(copySuoritus(suoritus, suoritus.tila, Some(List(Arviointi(KoodistoKoodiViite("x", "arviointiasteikkoammatillinent1k3"), None))), None))
          (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia arviointiasteikkoammatillinent1k3/x ei löydy koodistosta"))))
      }
    }

    describe("Suorituksen päivämäärät") {
      def päivämäärillä(alkamispäivä: String, arviointipäivä: String, vahvistuspäivä: String) = {
        copySuoritus(suoritus, tilaValmis, arviointiHyvä(Some(LocalDate.parse(arviointipäivä))), Some(Vahvistus(Some(LocalDate.parse(vahvistuspäivä)))), Some(LocalDate.parse(alkamispäivä)))
      }

      describe("Päivämäärät kunnossa") {
        it("palautetaan HTTP 200" ) (put(päivämäärillä("2015-08-01", "2016-05-30", "2016-05-31"))(
          verifyResponseStatus(200)))
      }

      describe("alkamispäivä > arviointi.päivä") {
        it("palautetaan HTTP 200" ) (put(päivämäärillä("2017-08-01", "2016-05-31", "2016-05-31"))(
          verifyResponseStatus(400, TorErrorCategory.badRequest.validation.date.loppuEnnenAlkua("suoritus.alkamispäivä (2017-08-01) oltava sama tai aiempi kuin suoritus.arviointi.päivä(2016-05-31)"))))
      }

      describe("arviointi.päivä > vahvistus.päivä") {
        it("palautetaan HTTP 200" ) (put(päivämäärillä("2015-08-01", "2016-05-31", "2016-05-30"))(
          verifyResponseStatus(400, TorErrorCategory.badRequest.validation.date.loppuEnnenAlkua("suoritus.arviointi.päivä (2016-05-31) oltava sama tai aiempi kuin suoritus.vahvistus.päivä(2016-05-30)"))))
      }
    }
  }
}
