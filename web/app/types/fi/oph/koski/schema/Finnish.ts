/**
 * Lokalisoitu teksti. Vähintään yksi kielistä (fi/sv/en) vaaditaan
 * Lokalisoitu teksti, jossa mukana suomi
 *
 * @see `fi.oph.koski.schema.Finnish`
 */
export type Finnish = {
  $class: 'fi.oph.koski.schema.Finnish'
  fi: string
  sv?: string
  en?: string
}

export const Finnish = (o: {
  fi: string
  sv?: string
  en?: string
}): Finnish => ({ $class: 'fi.oph.koski.schema.Finnish', ...o })

export const isFinnish = (a: any): a is Finnish => a?.$class === 'Finnish'
