import { PreIBMuutSuorituksetTaiVastaavat2019 } from './PreIBMuutSuorituksetTaiVastaavat2019'
import { PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 } from './PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Muiden lukio-opintojen suoritus Pre-IB-opinnoissa 2019
 *
 * @see `fi.oph.koski.schema.MuidenLukioOpintojenPreIBSuoritus2019`
 */
export type MuidenLukioOpintojenPreIBSuoritus2019 = {
  $class: 'fi.oph.koski.schema.MuidenLukioOpintojenPreIBSuoritus2019'
  koulutusmoduuli: PreIBMuutSuorituksetTaiVastaavat2019
  osasuoritukset?: Array<PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionmuuopinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export const MuidenLukioOpintojenPreIBSuoritus2019 = (o: {
  koulutusmoduuli: PreIBMuutSuorituksetTaiVastaavat2019
  osasuoritukset?: Array<PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'lukionmuuopinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}): MuidenLukioOpintojenPreIBSuoritus2019 => ({
  $class: 'fi.oph.koski.schema.MuidenLukioOpintojenPreIBSuoritus2019',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukionmuuopinto',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

MuidenLukioOpintojenPreIBSuoritus2019.className =
  'fi.oph.koski.schema.MuidenLukioOpintojenPreIBSuoritus2019' as const

export const isMuidenLukioOpintojenPreIBSuoritus2019 = (
  a: any
): a is MuidenLukioOpintojenPreIBSuoritus2019 =>
  a?.$class === 'fi.oph.koski.schema.MuidenLukioOpintojenPreIBSuoritus2019'
