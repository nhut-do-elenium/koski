import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * PaikallinenLukionOpinto
 *
 * @see `fi.oph.koski.schema.PaikallinenLukionOpinto`
 */
export type PaikallinenLukionOpinto = {
  $class: 'fi.oph.koski.schema.PaikallinenLukionOpinto'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
  perusteenDiaarinumero: string
}

export const PaikallinenLukionOpinto = (o: {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
  perusteenDiaarinumero: string
}): PaikallinenLukionOpinto => ({
  $class: 'fi.oph.koski.schema.PaikallinenLukionOpinto',
  ...o
})

PaikallinenLukionOpinto.className =
  'fi.oph.koski.schema.PaikallinenLukionOpinto' as const

export const isPaikallinenLukionOpinto = (
  a: any
): a is PaikallinenLukionOpinto =>
  a?.$class === 'fi.oph.koski.schema.PaikallinenLukionOpinto'
