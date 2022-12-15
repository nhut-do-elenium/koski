import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Korkeakoulututkinnon tunnistetiedot
 *
 * @see `fi.oph.koski.schema.Korkeakoulututkinto`
 */
export type Korkeakoulututkinto = {
  $class: 'fi.oph.koski.schema.Korkeakoulututkinto'
  tunniste: Koodistokoodiviite<'koulutus', string>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  virtaNimi?: LocalizedString
}

export const Korkeakoulututkinto = (o: {
  tunniste: Koodistokoodiviite<'koulutus', string>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  virtaNimi?: LocalizedString
}): Korkeakoulututkinto => ({
  $class: 'fi.oph.koski.schema.Korkeakoulututkinto',
  ...o
})

export const isKorkeakoulututkinto = (a: any): a is Korkeakoulututkinto =>
  a?.$class === 'Korkeakoulututkinto'
