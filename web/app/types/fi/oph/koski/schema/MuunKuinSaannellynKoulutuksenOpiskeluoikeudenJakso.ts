import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso`
 */
export type MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso = {
  $class: 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso'
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    'lasna' | 'hyvaksytystisuoritettu' | 'keskeytynyt' | 'mitatoity'
  >
  alku: string
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '14' | '15'>
}

export const MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso = (o: {
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    'lasna' | 'hyvaksytystisuoritettu' | 'keskeytynyt' | 'mitatoity'
  >
  alku: string
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '14' | '15'>
}): MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso => ({
  $class:
    'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso',
  ...o
})

export const isMuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso = (
  a: any
): a is MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso =>
  a?.$class === 'MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso'
