package fi.oph.koski.henkilo

import java.time.LocalDate

import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.schema.UusiHenkilö

object KoskiSpecificMockOppijat {
  private val koskiSpecificOppijat = new MockOppijat

  // Tällä oppijalla ei ole fixtuureissa opiskeluoikeuksia, eikä tätä lisätä henkilöpalveluun.
  val tyhjä = UusiHenkilö("230872-7258", "Tero", Some("Tero"), "Tyhjä")

  val hetuton = koskiSpecificOppijat.addOppija(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.99999999123", sukunimi = "Hetuton", etunimet = "Heikki", kutsumanimi = "Heikki", hetu = None, syntymäaika = Some(LocalDate.of(1977, 2, 24)), yksilöity = false))
  val syntymäajallinen = koskiSpecificOppijat.addOppija(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.99999999124", sukunimi = "Syntynyt", etunimet = "Sylvi", kutsumanimi = "Sylvi", hetu = Some("220627-833V"), syntymäaika = Some(LocalDate.of(1970, 1, 1))))
  val eero = koskiSpecificOppijat.oppija("Esimerkki", "Eero", "010101-123N")
  val eerola = koskiSpecificOppijat.oppija("Çelik-Eerola", "Jouni", "081165-793C")
  val markkanen = koskiSpecificOppijat.oppija("Markkanen-Fagerström", "Eéro Jorma-Petteri", "080154-770R")
  val teija = koskiSpecificOppijat.oppija("Tekijä", "Teija", "251019-039B")
  val tero = koskiSpecificOppijat.oppija("Tunkkila-Fagerlund", "Tero Petteri Gustaf", "280608-6619")
  val presidentti = koskiSpecificOppijat.oppija("Presidentti", "Tasavallan", "")
  val koululainen = koskiSpecificOppijat.oppija("Koululainen", "Kaisa", "220109-784L")
  val suoritusTuplana = koskiSpecificOppijat.oppija("Tupla", "Toivo", "270298-533H")
  val luokallejäänyt = koskiSpecificOppijat.oppija("Luokallejäänyt", "Lasse", "170186-6520")
  val ysiluokkalainen = koskiSpecificOppijat.oppija("Ysiluokkalainen", "Ylermi", "160932-311V", kotikunta = Some("179"))
  val vuosiluokkalainen = koskiSpecificOppijat.oppija("Vuosiluokkalainen", "Ville", "010100-325X")
  val monessaKoulussaOllut = koskiSpecificOppijat.oppija("Monikoululainen", "Miia", "180497-112F")
  val lukiolainen = koskiSpecificOppijat.oppija("Lukiolainen", "Liisa", "020655-2479", kotikunta = Some("179"))
  val lukioKesken = koskiSpecificOppijat.oppija("Lukiokesken", "Leila", "190363-279X")
  val uusiLukio = koskiSpecificOppijat.oppija("Uusilukio", "Ulla", "250605A518Y")
  val uusiLukionAineopiskelija = koskiSpecificOppijat.oppija("Uusilukionaineopiskelija", "Urho", "010705A6119")
  val lukionAineopiskelija = koskiSpecificOppijat.oppija("Lukioaineopiskelija", "Aino", "210163-2367")
  val lukionAineopiskelijaAktiivinen = koskiSpecificOppijat.oppija("Lukioaineopiskelija", "Aktiivinen", "200300-624E")
  val lukionEiTiedossaAineopiskelija = koskiSpecificOppijat.oppija("Erkki", "Eitiedossa", "151132-746V")
  val ammattilainen = koskiSpecificOppijat.oppija("Ammattilainen", "Aarne", "280618-402H")
  val tutkinnonOsaaPienempiKokonaisuus = koskiSpecificOppijat.oppija("Pieni-Kokonaisuus", "Pentti", "040754-054W")
  val muuAmmatillinen = koskiSpecificOppijat.oppija("Muu-Ammatillinen", "Marjo", "130320-899Y")
  val muuAmmatillinenKokonaisuuksilla = koskiSpecificOppijat.oppija("Kokonaisuuksilla", "Keijo", "130174-452V")
  val ammatilliseenTetäväänValmistavaMuuAmmatillinen = koskiSpecificOppijat.oppija("Tehtävään-Valmistava", "Tauno", "050192-777U")
  val erkkiEiperusteissa = koskiSpecificOppijat.oppija("Eiperusteissa", "Erkki", "201137-361Y")
  val amis = koskiSpecificOppijat.oppija("Amis", "Antti", "211097-402L")
  val liiketalous = koskiSpecificOppijat.oppija("Liiketalous", "Lilli", "160525-780Y")
  val dippainssi = koskiSpecificOppijat.oppija("Dippainssi", "Dilbert", "100869-192W")
  val korkeakoululainen = koskiSpecificOppijat.oppija("Korkeakoululainen", "Kikka", "150113-4146")
  val amkValmistunut = koskiSpecificOppijat.oppija("Amis", "Valmis", "250686-102E", vanhaHetu = Some("250686-6493"))
  val opintojaksotSekaisin = koskiSpecificOppijat.oppija("Hassusti", "Opintojaksot", "090992-3237")
  val amkKesken = koskiSpecificOppijat.oppija("Amiskesken", "Jalmari", "090197-411W")
  val amkKeskeytynyt = koskiSpecificOppijat.oppija("Pudokas", "Valtteri", "170691-3962")
  val monimutkainenKorkeakoululainen = koskiSpecificOppijat.oppija("Korkeakoululainen", "Kompleksi", "060458-331R")
  val virtaEiVastaa = koskiSpecificOppijat.oppija("Virtanen", "Eivastaa", "250390-680P")
  val oppiaineenKorottaja = koskiSpecificOppijat.oppija("Oppiaineenkorottaja", "Olli", "110738-839L")
  val montaOppiaineenOppimäärääOpiskeluoikeudessa = koskiSpecificOppijat.oppija("Mervi", "Monioppiaineinen", "131298-5248")
  val virtaKaksiPäätösonSuoritusta = koskiSpecificOppijat.oppija("Kaksi-Päinen", "Ville", "270680-459P")
  val aikuisOpiskelija = koskiSpecificOppijat.oppija("Aikuisopiskelija", "Aini", "280598-2415", vanhaHetu = Some("280598-326W"))
  val aikuisOpiskelijaMuuKuinVos = koskiSpecificOppijat.oppija("Aikuinen", "AikuisopiskelijaMuuKuinVos", "241001B7650")
  val aikuisAineOpiskelijaMuuKuinVos = koskiSpecificOppijat.oppija("Aikuinen", "AikuisAineOpiskelijaMuuKuinVos", "241001B7649")
  val aikuisOpiskelijaVieraskielinen = koskiSpecificOppijat.oppija("Aikuinen", "Vieraskielinen", "241001B7651", äidinkieli = Some("en"))
  val aikuisOpiskelijaVieraskielinenMuuKuinVos = koskiSpecificOppijat.oppija("Aikuinen", "VieraskielinenMuuKuinVos", "241001B7647", äidinkieli = Some("en"))
  val aikuisOpiskelijaMuuRahoitus = koskiSpecificOppijat.oppija("Aikuinen", "MuuRahoitus", "241001C4647")
  val kymppiluokkalainen = koskiSpecificOppijat.oppija("Kymppiluokkalainen", "Kaisa", "131025-6573", sukupuoli = Some("2"), kotikunta = Some("Kontu"))
  val luva = koskiSpecificOppijat.oppija("Lukioonvalmistautuja", "Luke", "211007-442N")
  val luva2019 = koskiSpecificOppijat.oppija("Lukioonvalmistautuja2019", "Luke", "270926-380M")
  val valma = koskiSpecificOppijat.oppija("Amikseenvalmistautuja", "Anneli", "130404-054C")
  val ylioppilas = koskiSpecificOppijat.oppija("Ylioppilas", "Ynjevi", "210244-374K", vanhaHetu = Some("210244-073V"))
  val ylioppilasLukiolainen = koskiSpecificOppijat.oppija("Ylioppilaslukiolainen", "Ynjevi", "080698-967F")
  val ylioppilasEiOppilaitosta = koskiSpecificOppijat.oppija("Ylioppilas", "Yrjänä", "240775-720P")
  val toimintaAlueittainOpiskelija = koskiSpecificOppijat.oppija("Toiminta", "Tommi", "031112-020J")
  val telma = koskiSpecificOppijat.oppija("Telmanen", "Tuula", "021080-725C")
  val erikoisammattitutkinto = koskiSpecificOppijat.oppija("Erikoinen", "Erja", "250989-419V")
  val reformitutkinto = koskiSpecificOppijat.oppija("Reformi", "Reijo", "251176-003P")
  val osittainenammattitutkinto = koskiSpecificOppijat.oppija("Osittainen", "Outi", "230297-6448")
  val ammatillisenOsittainenRapsa = koskiSpecificOppijat.oppija("Ammatillinen-Osittainen", "Raitsu", "140493-2798")
  val paikallinenTunnustettu = koskiSpecificOppijat.oppija("Tunnustettu", "Teuvo", "140176-449X")
  val tiedonsiirto = koskiSpecificOppijat.oppija("Tiedonsiirto", "Tiina", "270303-281N")
  val perusopetuksenTiedonsiirto = koskiSpecificOppijat.oppija("Perusopetuksensiirto", "Pertti", "010100-071R")
  val omattiedot = koskiSpecificOppijat.oppija(MockUsers.omattiedot.ldapUser.sukunimi, MockUsers.omattiedot.ldapUser.etunimet, "190751-739W", MockUsers.omattiedot.ldapUser.oid)
  val ibFinal = koskiSpecificOppijat.oppija("IB-final", "Iina", "040701-432D")
  val ibPredicted = koskiSpecificOppijat.oppija("IB-predicted", "Petteri", "071096-317K")
  val ibPreIB2019 = koskiSpecificOppijat.oppija("IB-Pre-IB-uusilukio", "Pate", "180300A8736")
  val dia = koskiSpecificOppijat.oppija("Dia", "Dia", "151013-2195")
  val internationalschool = koskiSpecificOppijat.oppija("International", "Ida", "170186-854H")
  val eskari = koskiSpecificOppijat.oppija("Eskari", "Essi", "300996-870E", kotikunta = Some("179"))
  val eskariAikaisillaLisätiedoilla = koskiSpecificOppijat.oppija("Lisä-Eskari", "Essiina", "300996-872E")
  val master = koskiSpecificOppijat.oppija("of Puppets", "Master", "101097-6107")
  val slave = koskiSpecificOppijat.addOppija(OppijaHenkilöWithMasterInfo(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.00000051473", sukunimi = "of Puppets", etunimet = "Slave", kutsumanimi = "Slave", hetu = Some("101097-6107"), syntymäaika = None), Some(master)))
  val masterEiKoskessa = koskiSpecificOppijat.addOppija(LaajatOppijaHenkilöTiedot(oid = koskiSpecificOppijat.generateId(), sukunimi = "Master", etunimet = "Master", kutsumanimi = "Master", hetu = Some("270366-697B"), syntymäaika = None))
  val slaveMasterEiKoskessa = koskiSpecificOppijat.addOppija(OppijaHenkilöWithMasterInfo(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.41000051473", hetu = Some("270366-697B"), syntymäaika = None, sukunimi = "Slave", etunimet = "Slave", kutsumanimi = "Slave"), Some(masterEiKoskessa)))
  val omattiedotSlave = koskiSpecificOppijat.addOppija(OppijaHenkilöWithMasterInfo(LaajatOppijaHenkilöTiedot(oid = koskiSpecificOppijat.generateId(), hetu = Some("190751-739W"), syntymäaika = None, etunimet = MockUsers.omattiedot.ldapUser.etunimet, kutsumanimi = MockUsers.omattiedot.ldapUser.etunimet, sukunimi = MockUsers.omattiedot.ldapUser.sukunimi), Some(omattiedot)))
  val opiskeluoikeudenOidKonflikti = koskiSpecificOppijat.oppija("Oidkonflikti", "Oskari", "260539-745W", "1.2.246.562.24.09090909090")
  val eiKoskessa = koskiSpecificOppijat.oppija("EiKoskessa", "Eino", "270181-5263", "1.2.246.562.24.99999555555", vanhaHetu = Some("270181-517T"))
  val eiKoskessaHetuton = koskiSpecificOppijat.addOppija(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.99999555556", sukunimi = "EiKoskessaHetuton", etunimet = "Eino", kutsumanimi = "Eino", hetu = None, syntymäaika = None))
  val turvakielto = koskiSpecificOppijat.oppija("Turvakielto", "Tero", "151067-2193", turvakielto = true)
  val montaJaksoaKorkeakoululainen = koskiSpecificOppijat.oppija("Korkeakoululainen", "Monta-Opintojaksoa", "030199-3419")
  val organisaatioHistoria = koskiSpecificOppijat.oppija("Historoitsija", "Hiisi", "200994-834A")
  val montaKoulutuskoodiaAmis = koskiSpecificOppijat.oppija("Koodari", "Monthy", "151099-036E")
  val tunnisteenKoodiarvoPoistettu = koskiSpecificOppijat.oppija("ePerusteidenKoulutuksen-koodi", "Poistettu", "161097-132N")
  val valtuutusOppija = koskiSpecificOppijat.oppija("Sydänmaanlakka-Horttanainen", "Katariina Eeva Marjatta", "020190-9521", kutsumanimi = Some("Katariina"))
  val siirtoOpiskelijaVirta = koskiSpecificOppijat.oppija("SiirtoOpiskelijaVirta", "Siiri", "141199-418X")
  val faija = koskiSpecificOppijat.oppija("EiOpintojaKoskessa", "Faija", "030300-5215")
  val faijaFeilaa = koskiSpecificOppijat.oppija("EiOpintojaKoskessaLastenHakuFailaa", "Faija", "030300-7053")
  val koulusivistyskieliYlioppilas = koskiSpecificOppijat.oppija("Koulu", "SivistysKieli", "020401-368M")
  val montaKoulusivityskieltäYlioppilas = koskiSpecificOppijat.oppija("MontaKoulu", "SivistysKieltä", "020401-746U")
  val labammattikoulu = koskiSpecificOppijat.oppija("Lahti", "LAB", "260308-361W")
  val valviraaKiinnostavaTutkinto = koskiSpecificOppijat.oppija("Valviralle", "Veera", "120100A2365")
  val valviraaKiinnostavaTutkintoKesken = koskiSpecificOppijat.oppija("Valviralle-Kesken", "Ville", "131099-633D")
  val kelaErityyppisiaOpiskeluoikeuksia = koskiSpecificOppijat.oppija("Kelalle", "Useita", "100800A057R")
  val lukioDiaIbInternationalOpiskelijamaaratRaportti_nuortenOppimaara = koskiSpecificOppijat.oppija("nuorten", "oppimaara", "180900A955N")
  val lukioDiaIbInternationalOpiskelijamaaratRaportti_aikuistenOppimaara = koskiSpecificOppijat.oppija("aikuisten", "oppimaara", "180900A991U")
  val lukioDiaIbInternationalOpiskelijamaaratRaportti_aineopiskelija = koskiSpecificOppijat.oppija("aine", "opiskelija", "180900A945B")
  val lukioDiaIbInternationalOpiskelijamaaratRaportti_dia = koskiSpecificOppijat.oppija("dia", "opiskelija", "180900A985M")
  val lukioDiaIbInternationalOpiskelijamaaratRaportti_ib = koskiSpecificOppijat.oppija("ib", "opiskelija", "180900A919H")
  val lukioDiaIbInternationalOpiskelijamaaratRaportti_international = koskiSpecificOppijat.oppija("international", "opiskelija", "180900A9074")
  val perusopetusOppijaMaaratRaportti_tavallinen = koskiSpecificOppijat.oppija("t", "tavallinen", "241001A8751")
  val perusopetusOppijaMaaratRaportti_erikois = koskiSpecificOppijat.oppija("e", "erikois", "180900A2298")
  val perusopetusOppijaMaaratRaportti_virheellisestiSiirretty = koskiSpecificOppijat.oppija("v", "virheellisestiSiirretty", "050501A093H")
  val perusopetusOppijaMaaratRaportti_virheellisestiSiirrettyVieraskielinen = koskiSpecificOppijat.oppija("v", "virheellisestiSiirrettyVieraskielinen", "131100A355P", äidinkieli = Some("en"))
  val organisaatioHistoriallinen = koskiSpecificOppijat.oppija("o", "organisaatioHistoriallinen", "210728-156E")
  val lukioKurssikertymaRaportti_oppimaara = koskiSpecificOppijat.oppija("Kurssikertyma", "Oppimaara", "280900A945T")
  val lukioKurssikertymaRaportti_aineopiskelija_eronnut = koskiSpecificOppijat.oppija("Kurssikertyma", "Eronnut Aineopiskelija", "280900A9554")
  val lukioKurssikertymaRaportti_aineopiskelija_valmistunut = koskiSpecificOppijat.oppija("Kurssikertyma", "Valmistunut Aineopiskelija", "140802A010A")
  val luvaOpiskelijamaaratRaportti_nuortenOppimaara = koskiSpecificOppijat.oppija("Luva", "Nuorten", "300900A9818", kotikunta = Some("035"))
  val luvaOpiskelijamaaratRaportti_aikuistenOppimaara = koskiSpecificOppijat.oppija("Luva", "Aikuisten", "300900A9774")
  val paallekkaisiOpiskeluoikeuksia = koskiSpecificOppijat.oppija("Paallekkaisia", "Pekka", "171100A9438")
  val vapaaSivistystyöOppivelvollinen = koskiSpecificOppijat.oppija("Vapaa-Sivistys", "Oppivelvollinen", "080177-870W")
  val vapaaSivistystyöMaahanmuuttajienKotoutus = koskiSpecificOppijat.oppija("Vapaa-Sivistys", "Kotoutuja", "260769-598H")
  val vapaaSivistystyöLukutaitoKotoutus = koskiSpecificOppijat.oppija("Vapaa-Sivistys", "Lukutaitokouluttautuja", "231158-467R")
  val oikeusOpiskelunMaksuttomuuteen = koskiSpecificOppijat.oppija("Oikeus", "Maksuttomuuteen", "010104A6094", syntymäaika = Some(LocalDate.of(2004, 1, 1)))
  val eiOikeuttaMaksuttomuuteen = koskiSpecificOppijat.oppija("EiOikeutta", "Maksuttomuuteen", "311203A1454", syntymäaika = Some(LocalDate.of(2003, 12, 31)))
  val etk18vSyntynytKesäkuunEnsimmäisenäPäivänä = koskiSpecificOppijat.oppija("Nopea", "Nina", "010698-6646", syntymäaika = Some(LocalDate.of(1998, 6, 1)))
  val etk18vSyntynytToukokuunViimeisenäPäivänä = koskiSpecificOppijat.oppija("Nopea", "Noa", "310598-4959", syntymäaika = Some(LocalDate.of(1998, 5, 31)))
  //Jos luot uuden oppijan voi hetun generoida täältä: http://www.lintukoto.net/muut/henkilotunnus/index.php
  //Huomaa, että hetun pitää olla oikean kaltainen

  val virtaOppija = koskiSpecificOppijat.addOppija(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.57060795845", sukunimi = "Virta", etunimet = "Veikko", kutsumanimi = "Veikko", hetu = Some("270191-4208"), syntymäaika = Some(LocalDate.of(1978, 3, 25)), äidinkieli = None, kansalaisuus = None))
  val virtaOppijaHetuton = koskiSpecificOppijat.addOppija(OppijaHenkilöWithMasterInfo(
    LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.20170814313", sukunimi = "Virta", etunimet = "Veikko", kutsumanimi = "Veikko", hetu = None, syntymäaika = Some(LocalDate.of(1978, 3, 25)), äidinkieli = None, kansalaisuus = None),
    Some(virtaOppija)))

  def defaultOppijat = koskiSpecificOppijat.getOppijat
}
