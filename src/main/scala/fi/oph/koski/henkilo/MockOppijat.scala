package fi.oph.koski.henkilo

import java.time.LocalDate

import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.{Loggable, Logging}
import fi.oph.koski.schema._

object MockOppijat {
  private val oppijat = new MockOppijat

  // Tällä oppijalla ei ole fixtuureissa opiskeluoikeuksia, eikä tätä lisätä henkilöpalveluun.
  val tyhjä = UusiHenkilö("230872-7258", "Tero", Some("Tero"), "Tyhjä")

  val hetuton = oppijat.addOppija(TäydellisetHenkilötiedot("1.2.246.562.24.99999999123", None, Some(LocalDate.of(1977, 2, 24)), "Heikki", "Heikki", "Hetuton", None, None))
  val syntymäajallinen = oppijat.addOppija(TäydellisetHenkilötiedot("1.2.246.562.24.99999999124", Some("220627-833V"), Some(LocalDate.of(1970, 1, 1)), "Sylvi", "Sylvi", "Syntynyt", None, None))
  val eero = oppijat.oppija("Esimerkki", "Eero", "010101-123N")
  val eerola = oppijat.oppija("Eerola", "Jouni", "081165-793C")
  val markkanen = oppijat.oppija("Markkanen-Fagerström", "Eéro Jorma-Petteri", "080154-770R")
  val teija = oppijat.oppija("Tekijä", "Teija", "251019-039B")
  val tero = oppijat.oppija("Tunkkila-Fagerlund", "Tero Petteri Gustaf", "280608-6619")
  val presidentti = oppijat.oppija("Presidentti", "Tasavallan", "")
  val koululainen = oppijat.oppija("Koululainen", "Kaisa", "220109-784L")
  val luokallejäänyt = oppijat.oppija("Luokallejäänyt", "Lasse", "170186-6520")
  val ysiluokkalainen = oppijat.oppija("Ysiluokkalainen", "Ylermi", "160932-311V")
  val monessaKoulussaOllut = oppijat.oppija("Monikoululainen", "Miia", "180497-112F")
  val lukiolainen = oppijat.oppija("Lukiolainen", "Liisa", "020655-2479")
  val lukioKesken = oppijat.oppija("Lukiokesken", "Leila", "190363-279X")
  val lukionAineopiskelija = oppijat.oppija("Lukioaineopiskelija", "Aino", "210163-2367")
  val ammattilainen = oppijat.oppija("Ammattilainen", "Aarne", "280618-402H")
  val amis = oppijat.oppija("Amis", "Antti", "211097-402L")
  val liiketalous = oppijat.oppija("Liiketalous", "Lilli", "160525-780Y")
  val dippainssi = oppijat.oppija("Dippainssi", "Dilbert", "100869-192W")
  val korkeakoululainen = oppijat.oppija("Korkeakoululainen", "Kikka", "150113-4146")
  val amkValmistunut = oppijat.oppija("Amis", "Valmis", "250686-102E")
  val amkKesken = oppijat.oppija("Amiskesken", "Jalmari", "090197-411W")
  val amkKeskeytynyt = oppijat.oppija("Pudokas", "Valtteri", "170691-3962")
  val monimutkainenKorkeakoululainen = oppijat.oppija("Korkeakoululainen", "Kompleksi", "060458-331R")
  val virtaEiVastaa = oppijat.oppija("Virtanen", "Eivastaa", "020507-984V")
  val oppiaineenKorottaja = oppijat.oppija("Oppiaineenkorottaja", "Olli", "110738-839L")
  val aikuisOpiskelija = oppijat.oppija("Aikuisopiskelija", "Aini", "280598-2415")
  val kymppiluokkalainen = oppijat.oppija("Kymppiluokkalainen", "Kaisa", "131025-6573")
  val luva = oppijat.oppija("Lukioonvalmistautuja", "Luke", "211007-442N")
  val valma = oppijat.oppija("Amikseenvalmistautuja", "Anneli", "130404-054C")
  val ylioppilas = oppijat.oppija("Ylioppilas", "Ynjevi", "010696-971K")
  val ylioppilasEiOppilaitosta = oppijat.oppija("Ylioppilas", "Yrjänä", "240775-720P")
  val toimintaAlueittainOpiskelija = oppijat.oppija("Toiminta", "Tommi", "031112-020J")
  val telma = oppijat.oppija("Telmanen", "Tuula", "021080-725C")
  val erikoisammattitutkinto = oppijat.oppija("Erikoinen", "Erja", "250989-419V")
  val reformitutkinto = oppijat.oppija("Reformi", "Reijo", "251176-003P")
  val osittainenammattitutkinto = oppijat.oppija("Osittainen", "Outi", "230297-6448")
  val paikallinenTunnustettu = oppijat.oppija("Tunnustettu", "Teuvo", "140176-449X")
  val tiedonsiirto = oppijat.oppija("Tiedonsiirto", "Tiina", "270303-281N")
  val omattiedot = oppijat.oppija(MockUsers.omattiedot.ldapUser.sukunimi, MockUsers.omattiedot.ldapUser.etunimet, "190751-739W", MockUsers.omattiedot.ldapUser.oid)
  val ibFinal = oppijat.oppija("IB-final", "Iina", "040701-432D")
  val ibPredicted = oppijat.oppija("IB-predicted", "Petteri", "071096-317K")
  val eskari = oppijat.oppija("Eskari", "Essi", "300996-870E")
  val master = oppijat.oppija("of Puppets", "Master", "101097-6107")
  val slave = oppijat.addOppija(TäydellisetHenkilötiedotWithMasterInfo(TäydellisetHenkilötiedot(oppijat.generateId(), Some("101097-6107"), None, "Slave", "Slave", "of Puppets", None, None), Some(master.henkilö)))
  val masterEiKoskessa = oppijat.addOppija(TäydellisetHenkilötiedot(oppijat.generateId(), Some("270366-697B"), None, "Master", "Master", "Master", None, None))
  val slaveMasterEiKoskessa = oppijat.addOppija(TäydellisetHenkilötiedotWithMasterInfo(TäydellisetHenkilötiedot(oppijat.generateId(), Some("270366-697B"), None, "Slave", "Slave", "Slave", None, None), Some(masterEiKoskessa.henkilö)))
  val omattiedotSlave = oppijat.addOppija(TäydellisetHenkilötiedotWithMasterInfo(TäydellisetHenkilötiedot(oppijat.generateId(), Some("190751-739W"), None, MockUsers.omattiedot.ldapUser.etunimet, MockUsers.omattiedot.ldapUser.etunimet, MockUsers.omattiedot.ldapUser.sukunimi, None, None), Some(omattiedot.henkilö)))
  val opiskeluoikeudenOidKonflikti = oppijat.oppija("Oidkonflikti", "Oskari", "260539-745W", "1.2.246.562.24.09090909090")
  val eiKoskessa = oppijat.oppija("EiKoskessa", "Eino", "270181-5263", "1.2.246.562.24.99999555555")
  val eiKoskessaHetuton = oppijat.addOppija(TäydellisetHenkilötiedot("1.2.246.562.24.99999555556", None, None, "Eino", "Eino", "EiKoskessaHetuton", None, None))
  val turvakielto = oppijat.oppija("Turvakielto", "Tero", "151067-2193", turvakielto = true)

  def defaultOppijat = oppijat.getOppijat

  def generateOid(counter: Int) = "1.2.246.562.24." + "%011d".format(counter)

  def oids = (defaultOppijat.map(_.henkilö.oid) ++ (1 to defaultOppijat.length + 100).map(generateOid).toList).distinct // oids that should be considered when deleting fixture data

  def asUusiOppija(oppija: HenkilöWithOid with Henkilötiedot) =
    UusiHenkilö(oppija.hetu.get, oppija.etunimet, Some(oppija.kutsumanimi), oppija.sukunimi)
}

class MockOppijat(private var oppijat: List[TäydellisetHenkilötiedotWithMasterInfo] = Nil) extends Logging {
  private var idCounter = oppijat.length
  val äidinkieli: Some[Koodistokoodiviite] = Some(Koodistokoodiviite("FI", None, "kieli", None))

  def oppija(suku: String, etu: String, hetu: String, oid: String = generateId(), kutsumanimi: Option[String] = None, turvakielto: Boolean = false): TäydellisetHenkilötiedotWithMasterInfo =
    addOppija(TäydellisetHenkilötiedot(oid, Some(hetu), None, etu, kutsumanimi.getOrElse(etu), suku, äidinkieli, None, Some(turvakielto)))

  def addOppija(oppija: TäydellisetHenkilötiedot): TäydellisetHenkilötiedotWithMasterInfo = addOppija(TäydellisetHenkilötiedotWithMasterInfo(oppija, None))

  def addOppija(oppija: TäydellisetHenkilötiedotWithMasterInfo): TäydellisetHenkilötiedotWithMasterInfo = {
    oppijat = oppija :: oppijat
    oppija
  }

  def getOppijat = oppijat

  def generateId(): String = this.synchronized {
    idCounter = idCounter + 1
    MockOppijat.generateOid(idCounter)
  }
}

class TestingException(text: String) extends RuntimeException(text) with Loggable {
  def logString = text
}
