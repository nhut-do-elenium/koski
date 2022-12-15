import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Ylioppilastutkinnon tunnistetiedot
 *
 * @see `fi.oph.koski.schema.Ylioppilastutkinto`
 */
export type Ylioppilastutkinto = {
  $class: 'fi.oph.koski.schema.Ylioppilastutkinto'
  tunniste: Koodistokoodiviite<'koulutus', '301000'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const Ylioppilastutkinto = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutus', '301000'>
    perusteenDiaarinumero?: string
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  } = {}
): Ylioppilastutkinto => ({
  $class: 'fi.oph.koski.schema.Ylioppilastutkinto',
  tunniste: Koodistokoodiviite({
    koodiarvo: '301000',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isYlioppilastutkinto = (a: any): a is Ylioppilastutkinto =>
  a?.$class === 'Ylioppilastutkinto'
