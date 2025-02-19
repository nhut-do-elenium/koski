import { IBOppiaineenArviointi } from './IBOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBOppiaineenPredictedArviointi } from './IBOppiaineenPredictedArviointi'
import { IBAineRyhmäOppiaine } from './IBAineRyhmaOppiaine'
import { IBKurssinSuoritus } from './IBKurssinSuoritus'

/**
 * IBOppiaineenSuoritus
 *
 * @see `fi.oph.koski.schema.IBOppiaineenSuoritus`
 */
export type IBOppiaineenSuoritus = {
  $class: 'fi.oph.koski.schema.IBOppiaineenSuoritus'
  arviointi?: Array<IBOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'iboppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  predictedArviointi?: Array<IBOppiaineenPredictedArviointi>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBAineRyhmäOppiaine
  osasuoritukset?: Array<IBKurssinSuoritus>
}

export const IBOppiaineenSuoritus = (o: {
  arviointi?: Array<IBOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'iboppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  predictedArviointi?: Array<IBOppiaineenPredictedArviointi>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBAineRyhmäOppiaine
  osasuoritukset?: Array<IBKurssinSuoritus>
}): IBOppiaineenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'iboppiaine',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.IBOppiaineenSuoritus',
  ...o
})

IBOppiaineenSuoritus.className =
  'fi.oph.koski.schema.IBOppiaineenSuoritus' as const

export const isIBOppiaineenSuoritus = (a: any): a is IBOppiaineenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.IBOppiaineenSuoritus'
