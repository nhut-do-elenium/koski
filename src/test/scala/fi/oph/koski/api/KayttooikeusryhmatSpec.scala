package fi.oph.koski.api

import java.time.LocalDate

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers.{evira, korkeakouluViranomainen, perusopetusViranomainen, toinenAsteViranomainen}
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.koski.luovutuspalvelu.HetuRequestV1
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.scalatest.{FreeSpec, Matchers}

class KäyttöoikeusryhmätSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen with SearchTestMethods with QueryTestMethods {
  import fi.oph.koski.schema.KoskiSchema.deserializationContext
  "koski-oph-pääkäyttäjä" - {
    val user = MockUsers.paakayttaja
    "voi muokata kaikkia opiskeluoikeuksia" in {
      resetFixtures
      putOpiskeluoikeus(defaultOpiskeluoikeus, headers = authHeaders(user) ++ jsonContent) {
        verifyResponseStatusOk()
      }
    }

    "voi hakea kaikkia opiskeluoikeuksia" in {
      searchForNames("eero", user) should equal(List("Jouni Eerola", "Eero Esimerkki", "Eéro Jorma-Petteri Markkanen-Fagerström"))
    }

    "voi hakea ja katsella kaikkia opiskeluoikeuksia" in {
      queryOppijat(user = user).length should be >= 10
      authGet("api/oppija/" + MockOppijat.ammattilainen.oid, user) {
        verifyResponseStatusOk()
      }
    }
  }

  "koski-viranomainen-katselija" - {
    val user = MockUsers.viranomainen

    "ei voi muokata opiskeluoikeuksia" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus, headers = authHeaders(user) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon 1.2.246.562.10.52251087186"))
      }
    }

    "voi hakea ja katsella kaikkia opiskeluoikeuksia" in {
      queryOppijat(user = user).length should be >= 10
      authGet("api/oppija/" + MockOppijat.ammattilainen.oid, user) {
        verifyResponseStatusOk()
      }
    }

    "voi hakea ja katsella ytr-ylioppilastutkintosuorituksia" in {
      haeOpiskeluoikeudetHetulla("250493-602S", user).filter(_.tyyppi.koodiarvo == "ylioppilastutkinto").length should equal(1)
    }

    "voi hakea ja katsella virta-ylioppilastutkintosuorituksia" in {
      haeOpiskeluoikeudetHetulla("250668-293Y", user).filter(_.tyyppi.koodiarvo == "korkeakoulutus").length should be >= 1
    }
  }

  "tallennusoikeudet muttei luottamuksellinen-roolia" - {
    val user = MockUsers.tallentajaEiLuottamuksellinen
    "ei voi muokata opiskeluoikeuksia" in {
      putOpiskeluoikeus(opiskeluoikeusLähdejärjestelmästä, henkilö = OidHenkilö(MockOppijat.markkanen.oid), headers = authHeaders(user) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon 1.2.246.562.10.51720121923"))
      }
    }
  }

  "koski-oppilaitos-palvelukäyttäjä" - {
    val user = MockUsers.omniaPalvelukäyttäjä
    "voi muokata opiskeluoikeuksia omassa organisaatiossa" in {
      putOpiskeluoikeus(opiskeluoikeusLähdejärjestelmästä, henkilö = OidHenkilö(MockOppijat.markkanen.oid), headers = authHeaders(user) ++ jsonContent) {
        verifyResponseStatusOk()
      }
    }

    "voi muokata vain lähdejärjestelmällisiä opiskeluoikeuksia" in {
      putOpiskeluoikeus(opiskeluoikeusOmnia, henkilö = OidHenkilö(MockOppijat.markkanen.oid), headers = authHeaders(user) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.lähdejärjestelmäIdPuuttuu("Käyttäjä on palvelukäyttäjä mutta lähdejärjestelmää ei ole määritelty"))
      }
    }

    "voi hakea ja katsella opiskeluoikeuksia vain omassa organisaatiossa" in {
      searchForNames("eero", user) should equal(List("Eéro Jorma-Petteri Markkanen-Fagerström"))
      authGet("api/oppija/" + MockOppijat.markkanen.oid, user) {
        verifyResponseStatusOk()
      }
    }

    "voi hakea opiskeluoikeuksia kyselyrajapinnasta" in {
      queryOppijat(user = user).map(_.henkilö.asInstanceOf[TäydellisetHenkilötiedot].sukunimi) should equal(List("Markkanen-Fagerström"))
    }

    "ei voi muokata opiskeluoikeuksia muussa organisaatiossa" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus, headers = authHeaders(user) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon 1.2.246.562.10.52251087186"))
      }
    }

    "ei voi katsella opiskeluoikeuksia muussa organisaatiossa" in {
      authGet("api/oppija/" + MockOppijat.eero.oid, user) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa 1.2.246.562.24.00000000001 ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
      }
    }

    "voi hakea ja katsella ytr-ylioppilastutkintosuorituksia" - {
      "vain omassa organisaatiossaan" in {
        haeOpiskeluoikeudetHetulla("250493-602S", MockUsers.omniaPalvelukäyttäjä).filter(_.tyyppi.koodiarvo == "ylioppilastutkinto").length should equal(0)
        haeOpiskeluoikeudetHetulla("250493-602S", MockUsers.kalle).filter(_.tyyppi.koodiarvo == "ylioppilastutkinto").length should equal(1)
      }
    }

    "voi hakea ja katsella virta-ylioppilastutkintosuorituksia" - {
      "vain omassa organisaatiossaan" in {
        haeOpiskeluoikeudetHetulla("250668-293Y", MockUsers.omniaPalvelukäyttäjä).filter(_.tyyppi.koodiarvo == "korkeakoulutus").length should equal(0)
        haeOpiskeluoikeudetHetulla("250668-293Y", MockUsers.kalle).filter(_.tyyppi.koodiarvo == "korkeakoulutus").length should be >= 1
      }
    }

    "näkee luottamuksellisen datan" in {
      resetFixtures
      authGet("api/oppija/" + MockOppijat.markkanen.oid, user) {
        verifyResponseStatusOk()
        sensitiveDataShown(body)
      }
    }
  }

  "vastuukäyttäjä" - {
    val user = MockUsers.stadinVastuukäyttäjä
    "ei näe luottamuksellista dataa" in {
      authGet("api/oppija/" + MockOppijat.eero.oid, user) {
        verifyResponseStatusOk()
        sensitiveDataHidden(body)
      }
    }
  }

  "palvelukäyttäjä, jolla useampi juuriorganisaatio" - {
    "ei voi tallentaa tietoja" in {
      putOpiskeluoikeus(opiskeluoikeusLähdejärjestelmästä, headers = authHeaders(MockUsers.kahdenOrganisaatioPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.juuriorganisaatioPuuttuu("Automaattisen tiedonsiirron palvelukäyttäjällä ei yksiselitteistä juuriorganisaatiota"))
      }
    }
  }

  "koski-oppilaitos-katselija" - {
    val user = MockUsers.omniaKatselija
    "ei voi muokata opiskeluoikeuksia omassa organisaatiossa" in {
      putOpiskeluoikeus(opiskeluoikeusOmnia, henkilö = OidHenkilö(MockOppijat.markkanen.oid), headers = authHeaders(user) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon 1.2.246.562.10.51720121923"))
      }
    }

    "voi hakea ja katsella opiskeluoikeuksia omassa organisaatiossa" in {
      searchForNames("eero", user) should equal(List("Eéro Jorma-Petteri Markkanen-Fagerström"))
      authGet("api/oppija/" + MockOppijat.markkanen.oid, user) {
        verifyResponseStatusOk()
        sensitiveDataShown(body)
      }
    }
  }

  "koski-oppilaitos-tallentaja" - {
    val user = MockUsers.omniaTallentaja
    "voi muokata opiskeluoikeuksia omassa organisaatiossa" in {
      putOpiskeluoikeus(opiskeluoikeusOmnia, henkilö = OidHenkilö(MockOppijat.markkanen.oid), headers = authHeaders(user) ++ jsonContent) {
        verifyResponseStatusOk()
      }
    }

    "ei voi tallentaa opiskeluoikeuksia käyttäen lähdejärjestelmä-id:tä" in {
      putOpiskeluoikeus(opiskeluoikeusLähdejärjestelmästä, henkilö = OidHenkilö(MockOppijat.markkanen.oid), headers = authHeaders(user) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.lähdejärjestelmäIdEiSallittu("Lähdejärjestelmä määritelty, mutta käyttäjä ei ole palvelukäyttäjä"))
      }
    }

    "ei voi muokata lähdejärjestelmän tallentamia opiskeluoikeuksia" - {
      val oppija = MockOppijat.tyhjä
      "ilman opiskeluoikeuden oid:ia luodaan uusi opiskeluoikeus" in {
        resetFixtures
        putOpiskeluoikeus(opiskeluoikeusLähdejärjestelmästä, henkilö = oppija, headers = authHeaders(MockUsers.omniaPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatusOk()
          haeOpiskeluoikeudetHetulla(oppija.hetu, user).filter(_.tyyppi.koodiarvo == "ammatillinenkoulutus").length should equal(1)
          putOpiskeluoikeus(opiskeluoikeusOmnia, henkilö = oppija, headers = authHeaders(user) ++ jsonContent) {
            verifyResponseStatusOk()
            haeOpiskeluoikeudetHetulla(oppija.hetu, user).filter(_.tyyppi.koodiarvo == "ammatillinenkoulutus").length should equal(2)
          }
        }
      }
      "opiskeluoikeus-oid:ia käytettäessä muutos estetään" in {
        val oid = haeOpiskeluoikeudetHetulla(oppija.hetu, user).filter(_.tyyppi.koodiarvo == "ammatillinenkoulutus").filter(_.lähdejärjestelmänId.isDefined)(0).oid.get
        putOpiskeluoikeus(opiskeluoikeusOmnia.copy(oid = Some(oid)), henkilö = oppija, headers = authHeaders(user) ++ jsonContent) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyMuutos("Opiskeluoikeuden lähdejärjestelmäId:tä ei voi poistaa."))
        }
      }
    }
  }

  "viranomainen jolla oikeudet kaikkiin koulutusmuotoihin" - {
    "ei näe luottamuksellista dataa" in {
      authGet("api/oppija/" + MockOppijat.eero.oid, evira) {
        verifyResponseStatusOk()
        sensitiveDataHidden(body)
      }
    }

    "ei voi muokata opiskeluoikeuksia" in {
      putOpiskeluoikeus(opiskeluoikeusLähdejärjestelmästä, henkilö = OidHenkilö(MockOppijat.markkanen.oid), headers = authHeaders(evira) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon 1.2.246.562.10.51720121923"))
      }
    }

    "voi hakea kaikkia opiskeluoikeuksia" in {
      searchForNames("eero", evira) should equal(List("Jouni Eerola", "Eero Esimerkki", "Eéro Jorma-Petteri Markkanen-Fagerström"))
    }

    "voi hakea ja katsella kaikkia opiskeluoikeuksia" in {
      queryOppijat(user = evira).length should be >= 10
      authGet("api/oppija/" + MockOppijat.ammattilainen.oid, evira) {
        verifyResponseStatusOk()
      }
    }
  }

  "viranomainen jolla oikeudet vain perusopetukseen" - {
    "voi hakea perusopetuksen opiskeluoikeuksia" in {
      searchForNames("eero", perusopetusViranomainen) should be(empty)
      searchForNames("kaisa", perusopetusViranomainen) should be(List("Kaisa Koululainen", "Kaisa Kymppiluokkalainen"))
    }

    "näkee vain perusopetuksen opiskeluoikeudet" in {
      queryOppijat(user = perusopetusViranomainen).flatMap(_.opiskeluoikeudet).map(_.tyyppi.koodiarvo).distinct.sorted should be(List("aikuistenperusopetus", "esiopetus", "perusopetukseenvalmistavaopetus", "perusopetuksenlisaopetus", "perusopetus"))
    }

    "ei näe muun typpisiä opiskeluoikeuksia" in {
      authGet("api/oppija/" + MockOppijat.ammattilainen.oid, perusopetusViranomainen) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia(s"Oppijaa ${MockOppijat.ammattilainen.oid} ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
      }
    }
  }

  "viranomainen jolla oikeudet toiseen asteeseen" - {
    "voi hakea toisen asteen opiskeluoikeuksia" in {
      searchForNames("ylermi", toinenAsteViranomainen) should be(empty)
      searchForNames(MockOppijat.dippainssi.hetu.get, toinenAsteViranomainen) should be(empty)
      searchForNames(MockOppijat.ylioppilas.hetu.get, toinenAsteViranomainen) should equal(List("Ynjevi Ylioppilas"))
      searchForNames("eero", toinenAsteViranomainen) should equal(List("Jouni Eerola", "Eero Esimerkki", "Eéro Jorma-Petteri Markkanen-Fagerström"))
    }

    "näkee vain toisen asteen opiskeluoikeudet" in {
      queryOppijat(user = toinenAsteViranomainen).flatMap(_.opiskeluoikeudet).map(_.tyyppi.koodiarvo).distinct.sorted should be(List("ammatillinenkoulutus", "ibtutkinto", "lukiokoulutus", "luva"))
      authGet("api/oppija/" + MockOppijat.ylioppilas.oid, toinenAsteViranomainen) {
        verifyResponseStatusOk()
      }
    }

    "ei näe muun typpisiä opiskeluoikeuksia" in {
      authGet("api/oppija/" + MockOppijat.ysiluokkalainen.oid, toinenAsteViranomainen) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia(s"Oppijaa ${MockOppijat.ysiluokkalainen.oid} ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
      }
    }
  }

  "viranomainen jolla oikeudet korkeakouluun" - {
    "voi hakea korkeakouluopiskeluoikeuksia" in {
      searchForNames(MockOppijat.ylioppilas.hetu.get, korkeakouluViranomainen) should be(empty)
      searchForNames(MockOppijat.dippainssi.hetu.get, korkeakouluViranomainen) should be(List("Dilbert Dippainssi"))
      searchForNames("eero", korkeakouluViranomainen) should be(empty)
    }

    "näkee vain korkeakouluopiskeluoikeudet" in {
      queryOppijat(user = korkeakouluViranomainen) should be(empty)
      authGet("api/oppija/" + MockOppijat.dippainssi.oid, korkeakouluViranomainen) {
        verifyResponseStatusOk()
      }
    }

    "ei näe muun typpisiä opiskeluoikeuksia" in {
      authGet("api/oppija/" + MockOppijat.ysiluokkalainen.oid, korkeakouluViranomainen) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia(s"Oppijaa ${MockOppijat.ysiluokkalainen.oid} ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
      }
    }
  }

  "viranomainen jolla luovutuspalveluoikeudet" - {
    "voi kutsua luovutuspalveluapeja" in {
      val body = HetuRequestV1(1, MockOppijat.ysiluokkalainen.hetu.get, List("perusopetus"))
      post("api/luovutuspalvelu/hetu", JsonSerializer.writeWithRoot(body), headers = authHeaders(MockUsers.luovutuspalveluKäyttäjä) ++ jsonContent) {
        verifyResponseStatusOk()
      }
    }

    "ei voi kutsua muita apeja" in {
      authGet("api/henkilo/hetu/010101-123N", MockUsers.luovutuspalveluKäyttäjä) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
      }
      authGet("api/oppija/" + MockOppijat.ysiluokkalainen.oid, MockUsers.luovutuspalveluKäyttäjä) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
      }
      authGet("api/tiedonsiirrot/yhteenveto", MockUsers.luovutuspalveluKäyttäjä) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
      }
      authGet("api/opiskeluoikeus/" + lastOpiskeluoikeus(MockOppijat.eero.oid).oid.get, MockUsers.luovutuspalveluKäyttäjä) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
      }
      authGet("api/opiskeluoikeus/perustiedot", MockUsers.luovutuspalveluKäyttäjä) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
      }
      authGet("api/opiskeluoikeus/historia/" + lastOpiskeluoikeus(MockOppijat.eero.oid).oid.get, MockUsers.luovutuspalveluKäyttäjä) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
      }
      authGet("api/oppilaitos", MockUsers.luovutuspalveluKäyttäjä) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
      }
      authGet(s"api/raportit/opiskelijavuositiedot?oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31&password=dummy&downloadToken=test123", MockUsers.luovutuspalveluKäyttäjä) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
      }
    }
  }

  "viranomainen jolla ei ole luovutuspalveluoikeuksia" - {
    "ei voi kutsua luovutuspalveluapeja" in {
      val body = HetuRequestV1(1, MockOppijat.ysiluokkalainen.hetu.get, List("perusopetus"))
      post("api/luovutuspalvelu/hetu", JsonSerializer.writeWithRoot(body), headers = authHeaders(MockUsers.perusopetusViranomainen) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainViranomainen())
      }
    }
  }

  private val opiskeluoikeusOmnia: AmmatillinenOpiskeluoikeus = defaultOpiskeluoikeus.copy(
    oppilaitos = Some(Oppilaitos(MockOrganisaatiot.omnia)),
    suoritukset = List(autoalanPerustutkinnonSuoritus().copy(toimipiste = Oppilaitos(MockOrganisaatiot.omnia)))
  )

  private def opiskeluoikeusLähdejärjestelmästä = opiskeluoikeusOmnia.copy(lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId))

  private def haeOpiskeluoikeudetHetulla(hetu: String, käyttäjä: UserWithPassword) = searchForHenkilötiedot(hetu).map(_.oid).flatMap { oid =>
    getOpiskeluoikeudet(oid, käyttäjä)
  }

  private def sensitiveDataShown(body: String) = sensitiveFieldValueEquals(body, expected = Some(List(Aikajakso(LocalDate.of(2001,1,1),None))))
  private def sensitiveDataHidden(body: String) = sensitiveFieldValueEquals(body, expected = None)

  private def sensitiveFieldValueEquals(body: String, expected: Option[List[Aikajakso]]) = {
    val sensitiveField = SchemaValidatingExtractor.extract[Oppija](body).map(_.opiskeluoikeudet.head.lisätiedot.get).map { case l: AmmatillisenOpiskeluoikeudenLisätiedot => l.vankilaopetuksessa }
    sensitiveField should equal(Right(expected))
  }
}
