package fi.oph.koski.valpas.opiskeluoikeusfixture

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers

import java.time.LocalDate

object ValpasMockOppijat {
  private val valpasOppijat = new MockOppijat

  val oppivelvollinenYsiluokkaKeskenKeväällä2021 = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollinen-ysiluokka-kesken-keväällä-2021", "Valpas", "221105A3023", kotikunta = Some("091"))
  val eiOppivelvollinenSyntynytEnnen2004 = valpasOppijat.oppijaSyntymäaikaHetusta("Ei-oppivelvollinen-syntynyt-ennen-2004", "Valpas", "210303A707J", kotikunta = Some("091"))
  val päällekkäisiäOpiskeluoikeuksia = valpasOppijat.oppijaSyntymäaikaHetusta("Päällekkäisiä", "Oppivelvollisuuksia", "060605A083N", kotikunta = Some("091"))
  val lukioOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Lukio-opiskelija", "Valpas", "070504A717P", kotikunta = Some("091"))
  val kasiluokkaKeskenKeväällä2021 = valpasOppijat.oppijaSyntymäaikaHetusta("Kasiluokka-kesken-keväällä-2021", "Valpas", "191106A1384", kotikunta = Some("091"))
  val kotiopetusMeneilläänOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Kotiopetus-meneillä", "Valpas", "210905A2151", kotikunta = Some("091"))
  val kotiopetusMenneisyydessäOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Kotiopetus-menneisyydessä", "Valpas", "060205A8805", kotikunta = Some("091"))
  val eronnutOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Eroaja-aiemmin", "Valpas", "240905A0078", kotikunta = Some("091"))
  val luokalleJäänytYsiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("LuokallejäänytYsiluokkalainen", "Valpas", "020805A5625", kotikunta = Some("091"))
  val luokallejäänytYsiluokkalainenJollaUusiYsiluokka = valpasOppijat.oppijaSyntymäaikaHetusta("LuokallejäänytYsiluokkalainenJatkaa", "Valpas", "060205A7222", kotikunta = Some("091"))
  val valmistunutYsiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Ysiluokka-valmis-keväällä-2021", "Valpas", "190605A006K", kotikunta = Some("091"))
  val luokalleJäänytYsiluokkalainenVaihtanutKoulua = valpasOppijat.oppijaSyntymäaikaHetusta("LuokallejäänytYsiluokkalainenKouluvaihto", "Valpas", "050605A7684", kotikunta = Some("091"))
  val luokalleJäänytYsiluokkalainenVaihtanutKouluaMuualta = valpasOppijat.oppijaSyntymäaikaHetusta("LuokallejäänytYsiluokkalainenKouluvaihtoMuualta", "Valpas", "021105A624K", kotikunta = Some("091"))
  val kasiinAstiToisessaKoulussaOllut = valpasOppijat.oppijaSyntymäaikaHetusta("KasiinAstiToisessaKoulussaOllut", "Valpas", "170805A613F", äidinkieli = Some("sv"), kotikunta = Some("091"))
  val lukionAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("LukionAloittanut", "Valpas", "290405A871A", äidinkieli = Some("en"), kotikunta = Some("091"))
  val lukionLokakuussaAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("LukionLokakuussaAloittanut", "Valpas", "180405A819J", kotikunta = Some("091"))
  val oppivelvollinenMonellaOppijaOidillaMaster = valpasOppijat.oppijaSyntymäaikaHetusta("Kahdella-oppija-oidilla", "Valpas", "150205A490C", kotikunta = Some("091"))
  val oppivelvollinenMonellaOppijaOidillaToinen = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaMaster)
  val oppivelvollinenMonellaOppijaOidillaKolmas = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaMaster)
  val aapajoenPeruskoulustaValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Aaapajoen-peruskoulusta-valmistunut", "Valpas", "160205A301X", kotikunta = Some("091"))
  val ennenLainRajapäivääPeruskoulustaValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Ennen-lain-rajapäivää-peruskoulusta-valmistunut", "Valpas", "080905A0798", kotikunta = Some("091"))
  val yli2kkAiemminPeruskoulustaValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Yli-2-kk-aiemmin-peruskoulusta-valmistunut", "Valpas", "010204A079U", kotikunta = Some("091"))
  val useampiYsiluokkaSamassaKoulussa = valpasOppijat.oppijaSyntymäaikaHetusta("UseampiYsiluokkaSamassaKoulussa", "Valpas", "250805A605C", kotikunta = Some("091"))
  val turvakieltoOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Turvakielto", "Valpas", "290904A4030", valpasOppijat.generateId(), None, turvakielto = true, kotikunta = Some("999"))
  val eronnutOppijaTarkastelupäivänä = valpasOppijat.oppijaSyntymäaikaHetusta("Eroaja-samana-päivänä", "Valpas", "270805A084V", kotikunta = Some("091"))
  val eronnutOppijaTarkastelupäivänJälkeen = valpasOppijat.oppijaSyntymäaikaHetusta("Eroaja-myöhemmin", "Valpas", "290905A840B", kotikunta = Some("091"))
  val oppivelvollinenAloittanutJaEronnutTarkastelupäivänJälkeen = valpasOppijat.oppijaSyntymäaikaHetusta("Aloittanut-ja-eronnut-myöhemmin", "Valpas", "270405A450E", kotikunta = Some("091"))
  val hakukohteidenHakuEpäonnistuu = valpasOppijat.oppijaSyntymäaikaHetusta("Epäonninen", "Valpas", "301005A336J", kotikunta = Some("091"))
  val kulosaarenYsiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Kulosaarelainen", "Oppija", "190105A788S", kotikunta = Some("091"))
  val kulosaarenYsiluokkalainenJaJyväskylänLukiolainen = valpasOppijat.oppijaSyntymäaikaHetusta("Jkl-Lukio-Kulosaarelainen", "Valpas", "010104A187H", kotikunta = Some("091"))
  val kulosaarenYsiluokkalainenJaJyväskylänNivelvaiheinen = valpasOppijat.oppijaSyntymäaikaHetusta("Jkl-Nivel-Kulosaarelainen", "Valpas", "010104A787V", kotikunta = Some("091"))
  val kulosaarenYsiluokkalainenJaJyväskylänEsikoululainen = valpasOppijat.oppijaSyntymäaikaHetusta("Jkl-Esikoulu-Kulosaarelainen", "Valpas", "220304A4173", kotikunta = Some("091"))
  val lukionAineopinnotAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("LukionAineopinnotAloittanut", "Valpas", "040305A559A", kotikunta = Some("091"))
  val valmistunutYsiluokkalainenJollaIlmoitus = valpasOppijat.oppijaSyntymäaikaHetusta("Ysiluokka-valmis-keväällä-2021-ilmo", "Valpas", "260805A3571", kotikunta = Some("091"))
  val kasiinAstiToisessaKoulussaOllutJollaIlmoitus = valpasOppijat.oppijaSyntymäaikaHetusta("KasiinAstiToisessaKoulussaOllut-ilmo", "Valpas", "020505A164W", äidinkieli = Some("sv"), kotikunta = Some("091"))
  val kahdenKoulunYsiluokkalainenJollaIlmoitus = valpasOppijat.oppijaSyntymäaikaHetusta("KahdenKoulunYsi-ilmo", "Valpas", "211104A0546", kotikunta = Some("091"))
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster = valpasOppijat.oppijaSyntymäaikaHetusta("Kahdella-oppija-oidilla-ilmo", "Valpas", "040605A0123", kotikunta = Some("091"))
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusToinen = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster)
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusKolmas = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster)
  val lukionAloittanutJollaVanhaIlmoitus = valpasOppijat.oppijaSyntymäaikaHetusta("LukionAloittanut-ilmo", "Valpas", "110405A435M", kotikunta = Some("091"))
  val lukionAloittanutJaLopettanutJollaIlmoituksia = valpasOppijat.oppijaSyntymäaikaHetusta("LukionAloittanutJaLopettanut-ilmo", "Valpas", "050405A249S", kotikunta = Some("091"))
  val ammattikoulustaValmistunutOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-valmistunut-opiskelija", "Valpas", "180304A082P", kotikunta = Some("091"))
  val eronnutMaaliskuussa17VuottaTäyttäväKasiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Eronnut-maaliskuussa-17-vuotta-täyttävä-8-luokkalainen", "Valpas", "280904A2768", kotikunta = Some("091"))
  val eronnutKeväänValmistumisJaksolla17VuottaTäyttäväKasiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Eronnut-kevään-valmistumisjaksolla-17-vuotta-täyttävä-8-luokkalainen", "Valpas", "121004A189X", kotikunta = Some("091"))
  val eronnutElokuussa17VuottaTäyttäväKasiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Eronnut-elokuussa-17-vuotta-täyttävä-8-luokkalainen", "Valpas", "110904A007L", kotikunta = Some("091"))
  val valmistunutYsiluokkalainenVsop = valpasOppijat.oppijaSyntymäaikaHetusta("Ysiluokka-valmis-keväällä-2021-vsop", "Valpas", "190705A575R", kotikunta = Some("091"))
  val ysiluokkaKeskenVsop = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollinen-ysiluokka-kesken-vsop", "Valpas", "240305A7103", kotikunta = Some("091"))
  val valmistunutKasiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Valmistunut-kasiluokkalainen-alle-17-vuotias", "Valpas", "090605A768P", kotikunta = Some("091"))
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster2 = valpasOppijat.oppijaSyntymäaikaHetusta("Kahdella-oppija-oidilla-ilmo-2", "Valpas", "030605A476D", kotikunta = Some("091"))
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusToinen2 = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster2)
  val ilmoituksenLisätiedotPoistettu = valpasOppijat.oppijaSyntymäaikaHetusta("Ilmoituksen-lisätiedot–poistettu", "Valpas", "190505A3019", kotikunta = Some("091"))
  val lukiostaValmistunutOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Lukio-opiskelija-valmistunut", "Valpas", "271105A835H", kotikunta = Some("091"))
  val ammattikouluOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-opiskelija", "Valpas", "231005A2431", kotikunta = Some("091"))
  val kaksoistutkinnostaValmistunutOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Kaksois-tutkinnosta-valmistunut", "Valpas", "260905A7672", kotikunta = Some("091"))
  val nivelvaiheestaValmistunutOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Nivelvaiheesta-valmistunut", "Valpas", "201005A022Y", kotikunta = Some("091"))
  val oppivelvollisuusKeskeytetty = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollisuus-keskeytetty-määräajaksi", "Valpas", "181005A1560", kotikunta = Some("091"))
  val oppivelvollisuusKeskeytettyToistaiseksi = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollisuus-keskeytetty-toistaiseksi", "Valpas", "150905A1823", kotikunta = Some("091"))
  val eiOppivelvollisuudenSuorittamiseenKelpaaviaOpiskeluoikeuksia = valpasOppijat.oppijaSyntymäaikaHetusta("Ei-oppivelvollisuuden-suorittamiseen-kelpaavia-opiskeluoikeuksia", "Valpas", "061005A671V", kotikunta = Some("091"))
  val hetuton = valpasOppijat.oppija("Hetuton", "Valpas", "", syntymäaika = Some(LocalDate.of(2005, 1, 1)), kotikunta = Some("091"))
  val oppivelvollinenJollaHetu = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollinen-hetullinen", "Valpas", "030105A7507", kotikunta = Some("091"))
  val oppivelvollinenJollaHetuHetutonSlave = valpasOppijat.duplicate(oppivelvollinenJollaHetu.copy(hetu = None))
  val ammattikouluOpiskelijaValma = valpasOppijat.oppijaSyntymäaikaHetusta("Valma-opiskelija", "Valpas", "190105A839D", kotikunta = Some("091"))
  val ammattikouluOpiskelijaTelma = valpasOppijat.oppijaSyntymäaikaHetusta("Telma-opiskelija", "Valpas", "020805A7784", kotikunta = Some("091"))
  val amisEronnutEiUuttaOpiskeluoikeutta = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut", "Valpas", "010805A852V", kotikunta = Some("091"))
  val amisEronnutUusiOpiskeluoikeusTulevaisuudessaKeskeyttänyt = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-oo-tulevaisuudessa-keskeyttänyt", "Valpas", "240905A539D", kotikunta = Some("091"))
  val amisEronnutUusiOpiskeluoikeusVoimassa = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-oo-voimassa", "Valpas", "241005A214R", kotikunta = Some("091"))
  val amisEronnutUusiOpiskeluoikeusPeruskoulussaKeskeyttänytTulevaisuudessa = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-peruskoulussa-keskeyttänyt-tulevaisuudessa", "Valpas", "100205A291R", kotikunta = Some("091"))
  val amisEronnutUusiOpiskeluoikeusNivelvaiheessa = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-nivelvaiheessa", "Valpas", "180605A898P", kotikunta = Some("091"))
  val amisEronnutUusiOpiskeluoikeusNivelvaiheessa2 = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-nivelvaiheessa-valmassa", "Valpas", "040804A0600", kotikunta = Some("091"))
  val amisEronnutMontaUuttaOpiskeluoikeutta = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-monta-uutta-oota", "Valpas", "241005A449A", kotikunta = Some("091"))
  val amisEronnutUusiKelpaamatonOpiskeluoikeusNivelvaiheessa = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-nivelvaihe-ei-kelpaa", "Valpas", "101105A1703", kotikunta = Some("091"))
  val amisEronnutUusiKelpaamatonOpiskeluoikeusNivelvaiheessa2 = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-nivelvaihe-vstssa-ei-kelpaa", "Valpas", "090604A305H", kotikunta = Some("091"))
  val ammattikouluOpiskelijaMontaOpiskeluoikeutta = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-monta-oota", "Valpas", "280105A505E", kotikunta = Some("091"))
  val amisAmmatillinenJaNäyttötutkintoonValmistava = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-useita-pts", "Valpas", "280505A418V", kotikunta = Some("091"))
  val opiskeluoikeudetonOppivelvollisuusikäinenOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Ei-opiskeluoikeuksia-oppivelvollisuusikäinen", "Valpas", "110405A6951", kotikunta = Some("091"))
  val opiskeluoikeudetonEiOppivelvollisuusikäinenOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Ei-opiskeluoikeuksia-vanha", "Valpas", "070302A402D", kotikunta = Some("091"))
  val lukioVäliaikaisestiKeskeytynyt = valpasOppijat.oppijaSyntymäaikaHetusta("Lukio-väliaikaisesti-keskeytynyt", "Valpas", "300504A157F", kotikunta = Some("091"))
  val amisLomalla = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-lomalla", "Valpas", "030905A194R", kotikunta = Some("091"))
  val internationalSchoolista9LuokaltaEnnen2021Valmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Inter-valmistunut-9-2020", "Valpas", "090605A517L", kotikunta = Some("091"))
  val internationalSchoolista9Luokalta2021Valmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Inter-valmistunut-9-2021", "Valpas", "200405A780K", kotikunta = Some("091"))
  val peruskoulustaValmistunutIlman9Luokkaa = valpasOppijat.oppijaSyntymäaikaHetusta("Valmistunut-ei-ysiluokkaa", "Valpas", "240905A4064", kotikunta = Some("091"))
  val peruskoulustaLokakuussaValmistunutIlman9Luokkaa = valpasOppijat.oppijaSyntymäaikaHetusta("Valmistunut-lokakuussa-ei-ysiluokkaa", "Valpas", "110505A1818", kotikunta = Some("091"))
  val lukioVanhallaOpsilla = valpasOppijat.oppijaSyntymäaikaHetusta("LukioVanhallaOpsilla", "Valpas", "060704A687P", kotikunta = Some("091"))
  val muuttanutUlkomaille = valpasOppijat.oppijaSyntymäaikaHetusta("MuuttanutUlkomaille", "Valpas", "130805A850J")
  val turvakieltoOppijaTyhjälläKotikunnalla = valpasOppijat.oppijaSyntymäaikaHetusta("TurvakieltoTyhjälläKotikunnalla", "Valpas", "280705A584U", valpasOppijat.generateId(), None, turvakielto = true, kotikunta = Some(""))
  val oppivelvollinenIntSchoolYsiluokkaKeskenKeväällä2021 = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollinen-int-school-kesken-keväällä-2021", "Valpas", "180205A026B", kotikunta = Some("091"))
  val intSchoolKasiluokkaKeskenKeväällä2021 = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-kasiluokka-kesken-keväällä-2021", "Valpas", "030705A638E", kotikunta = Some("091"))
  val intSchool9LuokaltaKeskenEronnutOppija =valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokalta-kesken-eroaja-aiemmin", "Valpas", "180205A6682", kotikunta = Some("091"))
  val intSchool9LuokaltaKeskenEronnutOppijaTarkastelupäivänä = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokalta-kesken-eroaja-samana-päivänä", "Valpas", "150905A020V", kotikunta = Some("091"))
  val intSchool9LuokaltaKeskenEronnutOppijaTarkastelupäivänJälkeen = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokalta-kesken-eroaja-myöhemmin", "Valpas", "210405A014H", kotikunta = Some("091"))
  val intSchool9LuokaltaValmistumisenJälkeenEronnutOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokalta-valmistumisen-jälkeen-eronnut-aiemmin", "Valpas", "170405A683H", kotikunta = Some("091"))
  val intSchool9LuokaltaValmistumisenJälkeenEronnutOppijaTarkastelupäivänä = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokalta-valmistumisen-jälkeen-eronnut-samana-päivänä", "Valpas", "090905A633S", kotikunta = Some("091"))
  val intSchool9LuokaltaValmistumisenJälkeenEronnutOppijaTarkastelupäivänJälkeen = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokalta-valmistumisen-jälkeen-eronnut-myöhemmin", "Valpas", "100705A034F", kotikunta = Some("091"))
  val intSchool9LuokanJälkeenLukionAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokan-jälkeen-lukion-aloittanut", "Valpas", "120505A3434", kotikunta = Some("091"))
  val intSchool9LuokanJälkeenIntSchoolin10LuokallaAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokan-jälkeen-int-schoolin-10-luokalla-aloittanut", "Valpas", "220205A6867", kotikunta = Some("091"))
  val intSchool9LuokanJälkeenLukionLokakuussaAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokan-jälkeen-lukion-lokakuussa-aloittanut", "Valpas", "070105A7969", kotikunta = Some("091"))
  val intSchool9LuokanJälkeenIntSchoolin10LuokallaLokakuussaAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokan-jälkeen-int-schoolin-10-luokalla-lokakuussa-aloittanut", "Valpas", "080405A722Y", kotikunta = Some("091"))
  val intSchoolin9LuokaltaYli2kkAiemminValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-yli-2kk-aiemmin-9-valmistunut", "Valpas", "231005A872A", kotikunta = Some("091"))
  val intSchoolin9LuokaltaYli2kkAiemminValmistunut10Jatkanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-yli-2kk-aiemmin-9-valmistunut-10-jatkanut", "Valpas", "111105A3651", kotikunta = Some("091"))
  val intSchoolistaEronnutMaaliskuussa17VuottaTäyttäväKasiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-eronnut-maaliskuussa-17-vuotta-täyttävä-8-luokkalainen", "Valpas", "100304A1358", kotikunta = Some("091"))
  val intSchoolistaEronnutElokuussa17VuottaTäyttäväKasiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-eronnut-elokuussa-17-vuotta-täyttävä-8-luokkalainen", "Valpas", "220804A101X", kotikunta = Some("091"))
  val intSchool10LuokaltaAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-10-luokalta-aloittanut", "Valpas", "090605A676R", kotikunta = Some("091"))
  val intSchool11LuokaltaAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-11-luokalta-aloittanut", "Valpas", "050405A222W", kotikunta = Some("091"))
  val intSchool8LuokanSyksyllä2021Aloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-8-luokan-syksyllä-2021-aloittanut", "Valpas", "040305A8601", kotikunta = Some("091"))
  val intSchool9LuokanSyksyllä2021Aloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokan-syksyllä-2021-aloittanut", "Valpas", "210805A187A", kotikunta = Some("091"))
  val intSchoolLokakuussaPerusopetuksenSuorittanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-vahvistettu-lokakuussa", "Valpas", "221105A467D", kotikunta = Some("091"))
  val intSchool10LuokallaIlmanAlkamispäivää = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-10-luokalla-ilman-alkamispäivää", "Valpas", "140305A455D", kotikunta = Some("091"))
  val aikuistenPerusopetuksessa = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksessa", "Valpas", "020304A145D", kotikunta = Some("091"))
  val aikuistenPerusopetuksessaSyksynRajapäivänJälkeenAloittava = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksessa-syksyn-rajapäivän-jälkeen", "Valpas", "250204A640D", kotikunta = Some("091"))
  val aikuistenPerusopetuksessaPeruskoulustaValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksesta-pk-valmistunut", "Valpas", "050304A177C", kotikunta = Some("091"))
  val aikuistenPerusopetuksestaKeväänValmistujaksollaValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksesta-keväällä-valmistunut", "Valpas", "070304A3464", kotikunta = Some("091"))
  val aikuistenPerusopetuksestaEronnut = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksesta-eronnut", "Valpas", "040404A8818", kotikunta = Some("091"))
  val aikuistenPerusopetuksestaYli2kkAiemminValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksesta-yli-2kk-aiemmin-valmistunut", "Valpas", "300104A657C", kotikunta = Some("091"))
  val aikuistenPerusopetuksestaAlle2kkAiemminValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksesta-alle-2kk-aiemmin-valmistunut", "Valpas", "131004A1477", kotikunta = Some("091"))
  val aikuistenPerusopetuksestaLähitulevaisuudessaValmistuva = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksesta-lähitulevaisuudessa-valmistuva", "Valpas", "220304A365D", kotikunta = Some("091"))
  val aikuistenPerusopetuksestaTulevaisuudessaValmistuva = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksesta-tulevaisuudessa-valmistuva", "Valpas", "121104A0176", kotikunta = Some("091"))
  val aikuistenPerusopetuksessaAineopiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksessa-aineopiskelija", "Valpas", "010604A727Y", kotikunta = Some("091"))
  val luva = valpasOppijat.oppijaSyntymäaikaHetusta("Luva", "Valpas", "290404A725B", kotikunta = Some("091"))
  val kymppiluokka = valpasOppijat.oppijaSyntymäaikaHetusta("Kymppi", "Valpas", "160404A8577", kotikunta = Some("091"))
  val vstKops = valpasOppijat.oppijaSyntymäaikaHetusta("Vst-kops", "Valpas", "190504A564H", kotikunta = Some("091"))
  val valma = valpasOppijat.oppijaSyntymäaikaHetusta("Valma", "Valpas", "090104A303D", kotikunta = Some("091"))
  val telma = valpasOppijat.oppijaSyntymäaikaHetusta("Telma", "Valpas", "160304A7532", kotikunta = Some("091"))
  val telmaJaAmis = valpasOppijat.oppijaSyntymäaikaHetusta("Telma-ja-amis", "Valpas", "030204A7935", kotikunta = Some("091"))
  val kaksiToisenAsteenOpiskelua = valpasOppijat.oppijaSyntymäaikaHetusta("Kaksi-toisen-asteen-opiskelua", "Valpas", "120504A399N", kotikunta = Some("091"))

  // Kutsumanimi ja yhteystiedot haetaan oppijanumerorekisteristä Valpas-käyttäjälle, tallennetaan siksi käyttäjä myös "oppijana" mockeihin
  val käyttäjäValpasJklNormaalikoulu = valpasOppijat.oppija(
    hetu = "300850-4762",
    oid = ValpasMockUsers.valpasJklNormaalikoulu.oid,
    suku = ValpasMockUsers.valpasJklNormaalikoulu.lastname,
    etu = ValpasMockUsers.valpasJklNormaalikoulu.firstname,
    kutsumanimi = Some("Kutsu"),
    kotikunta = Some("091"),
  )

  def defaultOppijat = valpasOppijat.getOppijat
}
