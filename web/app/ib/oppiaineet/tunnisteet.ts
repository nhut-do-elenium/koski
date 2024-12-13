import {
  isKoodistokoodiviite,
  Koodistokoodiviite
} from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { PaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { ItemOf } from '../../util/types'

export const koodiviiteTunnisteGuard =
  <U extends string, T extends readonly string[]>(uri: U, koodiarvot: T) =>
  (
    k?: Koodistokoodiviite | PaikallinenKoodi
  ): k is Koodistokoodiviite<U, ItemOf<T>> =>
    isKoodistokoodiviite(k) &&
    k.koodistoUri === uri &&
    koodiarvot.includes(k.koodiarvo)

export const tunnisteUriGuard =
  <U extends string>(uri: U) =>
  (k?: Koodistokoodiviite): k is Koodistokoodiviite<U> =>
    !!k && k.koodistoUri === uri

export const isIBOppiaineLanguageTunniste = koodiviiteTunnisteGuard(
  'oppiaineetib',
  ['A', 'A2', 'B', 'AB', 'CLA'] as const
)

export const isIBOppiaineMuuTunniste = koodiviiteTunnisteGuard('oppiaineetib', [
  'BIO',
  'BU',
  'CHE',
  'DAN',
  'DIS',
  'ECO',
  'FIL',
  'GEO',
  'HIS',
  'MAT',
  'MATFT',
  'MATST',
  'MUS',
  'PHI',
  'PHY',
  'POL',
  'PSY',
  'REL',
  'SOC',
  'ESS',
  'THE',
  'VA',
  'CS',
  'LIT',
  'INF',
  'DES',
  'SPO',
  'MATAA',
  'MATAI'
] as const)

export const isLukionMatematiikka2015Tunniste = koodiviiteTunnisteGuard(
  'koskioppiaineetyleissivistava',
  ['MA']
)

export const isLukionMuuValtakunnallinenOppiaine2015Tunniste =
  koodiviiteTunnisteGuard('koskioppiaineetyleissivistava', [
    'HI',
    'MU',
    'BI',
    'PS',
    'ET',
    'KO',
    'FI',
    'KE',
    'YH',
    'TE',
    'KS',
    'FY',
    'GE',
    'LI',
    'KU',
    'OP'
  ] as const)

export const isLukionUskonto2015Tunniste = koodiviiteTunnisteGuard(
  'koskioppiaineetyleissivistava',
  ['KT'] as const
)

export const isLukionÄidinkieliJaKirjallisuus2015Tunniste =
  koodiviiteTunnisteGuard('koskioppiaineetyleissivistava', ['AI'] as const)

export const isVierasTaiToinenKotimainenKieli2015Tunniste =
  koodiviiteTunnisteGuard('koskioppiaineetyleissivistava', [
    'A1',
    'A2',
    'B1',
    'B2',
    'B3',
    'AOM'
  ] as const)

export const isLukionMatematiikka2019Tunniste = koodiviiteTunnisteGuard(
  'koskioppiaineetyleissivistava',
  ['MA'] as const
)

export const isLukionMuuValtakunnallinenOppiaine2019Tunniste =
  koodiviiteTunnisteGuard('koskioppiaineetyleissivistava', [
    'HI',
    'MU',
    'BI',
    'PS',
    'ET',
    'FI',
    'KE',
    'YH',
    'TE',
    'FY',
    'GE',
    'LI',
    'KU',
    'OP'
  ] as const)

export const isLukionUskonto2019Tunniste = koodiviiteTunnisteGuard(
  'koskioppiaineetyleissivistava',
  ['KT'] as const
)

export const isLukionÄidinkieliJaKirjallisuus2019Tunniste =
  koodiviiteTunnisteGuard('koskioppiaineetyleissivistava', ['AI'] as const)

export const isVierasTaiToinenKotimainenKieli2019Tunniste =
  koodiviiteTunnisteGuard('koskioppiaineetyleissivistava', [
    'A',
    'B1',
    'B2',
    'B3',
    'AOM'
  ] as const)

export const isLukiodiplomit2019Tunniste = koodiviiteTunnisteGuard(
  'lukionmuutopinnot',
  ['LD'] as const
)

export const isMuutLukionSuoritukset2019Tunniste = koodiviiteTunnisteGuard(
  'lukionmuutopinnot',
  ['MS'] as const
)

export const isTemaattisetOpinnot2019Tunniste = koodiviiteTunnisteGuard(
  'lukionmuutopinnot',
  ['TO'] as const
)
