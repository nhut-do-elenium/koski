/**
 * OpiskeluoikeusAvaintaEiLöydy
 *
 * @see `fi.oph.koski.schema.OpiskeluoikeusAvaintaEiLöydy`
 */
export type OpiskeluoikeusAvaintaEiLöydy = {
  $class: 'fi.oph.koski.schema.OpiskeluoikeusAvaintaEiLöydy'
  tyyppi: string
  arvo: string
}

export const OpiskeluoikeusAvaintaEiLöydy = (o: {
  tyyppi: string
  arvo: string
}): OpiskeluoikeusAvaintaEiLöydy => ({
  $class: 'fi.oph.koski.schema.OpiskeluoikeusAvaintaEiLöydy',
  ...o
})

export const isOpiskeluoikeusAvaintaEiLöydy = (
  a: any
): a is OpiskeluoikeusAvaintaEiLöydy =>
  a?.$class === 'OpiskeluoikeusAvaintaEiLöydy'
