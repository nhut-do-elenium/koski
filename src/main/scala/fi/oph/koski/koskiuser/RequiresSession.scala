package fi.oph.koski.koskiuser

trait RequiresSession extends KoskiAuthenticationSupport with HasKoskiSession {
  implicit def koskiSession: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requireSession
  }
}

