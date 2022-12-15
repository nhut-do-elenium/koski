import { NäytönArviointi } from './NaytonArviointi'
import { NäytönSuorituspaikka } from './NaytonSuorituspaikka'
import { LocalizedString } from './LocalizedString'
import { NäytönSuoritusaika } from './NaytonSuoritusaika'

/**
 * Tutkinnon tai koulutuksen osan suoritukseen kuuluvan ammattiosaamisen näytön tiedot.
 *
 * @see `fi.oph.koski.schema.Näyttö`
 */
export type Näyttö = {
  $class: 'fi.oph.koski.schema.Näyttö'
  arviointi?: NäytönArviointi
  suorituspaikka?: NäytönSuorituspaikka
  haluaaTodistuksen?: boolean
  työssäoppimisenYhteydessä: boolean
  kuvaus?: LocalizedString
  suoritusaika?: NäytönSuoritusaika
}

export const Näyttö = (
  o: {
    arviointi?: NäytönArviointi
    suorituspaikka?: NäytönSuorituspaikka
    haluaaTodistuksen?: boolean
    työssäoppimisenYhteydessä?: boolean
    kuvaus?: LocalizedString
    suoritusaika?: NäytönSuoritusaika
  } = {}
): Näyttö => ({
  työssäoppimisenYhteydessä: false,
  $class: 'fi.oph.koski.schema.Näyttö',
  ...o
})

export const isNäyttö = (a: any): a is Näyttö => a?.$class === 'Näyttö'
