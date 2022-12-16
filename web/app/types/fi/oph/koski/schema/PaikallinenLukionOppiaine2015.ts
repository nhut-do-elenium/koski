import { LaajuusKursseissa } from './LaajuusKursseissa'
import { LocalizedString } from './LocalizedString'
import { PaikallinenKoodi } from './PaikallinenKoodi'

/**
 * Lukion/IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.PaikallinenLukionOppiaine2015`
 */
export type PaikallinenLukionOppiaine2015 = {
  $class: 'fi.oph.koski.schema.PaikallinenLukionOppiaine2015'
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
  kuvaus: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: PaikallinenKoodi
}

export const PaikallinenLukionOppiaine2015 = (o: {
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
  kuvaus: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: PaikallinenKoodi
}): PaikallinenLukionOppiaine2015 => ({
  $class: 'fi.oph.koski.schema.PaikallinenLukionOppiaine2015',
  ...o
})

export const isPaikallinenLukionOppiaine2015 = (
  a: any
): a is PaikallinenLukionOppiaine2015 =>
  a?.$class === 'PaikallinenLukionOppiaine2015'
