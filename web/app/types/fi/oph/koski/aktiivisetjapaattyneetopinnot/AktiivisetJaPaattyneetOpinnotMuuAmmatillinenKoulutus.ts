import {
  AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus,
  isAktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus
} from './AktiivisetJaPaattyneetOpinnotAmmatilliseenTehtavaanValmistavaKoulutus'
import {
  AktiivisetJaPäättyneetOpinnotPaikallinenMuuAmmatillinenKoulutus,
  isAktiivisetJaPäättyneetOpinnotPaikallinenMuuAmmatillinenKoulutus
} from './AktiivisetJaPaattyneetOpinnotPaikallinenMuuAmmatillinenKoulutus'

/**
 * AktiivisetJaPäättyneetOpinnotMuuAmmatillinenKoulutus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuAmmatillinenKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotMuuAmmatillinenKoulutus =
  | AktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus
  | AktiivisetJaPäättyneetOpinnotPaikallinenMuuAmmatillinenKoulutus

export const isAktiivisetJaPäättyneetOpinnotMuuAmmatillinenKoulutus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotMuuAmmatillinenKoulutus =>
  isAktiivisetJaPäättyneetOpinnotAmmatilliseenTehtäväänValmistavaKoulutus(a) ||
  isAktiivisetJaPäättyneetOpinnotPaikallinenMuuAmmatillinenKoulutus(a)
