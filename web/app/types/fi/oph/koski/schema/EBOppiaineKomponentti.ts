import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * EBOppiaineKomponentti
 *
 * @see `fi.oph.koski.schema.EBOppiaineKomponentti`
 */
export type EBOppiaineKomponentti = {
  $class: 'fi.oph.koski.schema.EBOppiaineKomponentti'
  tunniste: Koodistokoodiviite<'ebtutkinnonoppiaineenkomponentti', string>
}

export const EBOppiaineKomponentti = (o: {
  tunniste: Koodistokoodiviite<'ebtutkinnonoppiaineenkomponentti', string>
}): EBOppiaineKomponentti => ({
  $class: 'fi.oph.koski.schema.EBOppiaineKomponentti',
  ...o
})

EBOppiaineKomponentti.className =
  'fi.oph.koski.schema.EBOppiaineKomponentti' as const

export const isEBOppiaineKomponentti = (a: any): a is EBOppiaineKomponentti =>
  a?.$class === 'fi.oph.koski.schema.EBOppiaineKomponentti'
