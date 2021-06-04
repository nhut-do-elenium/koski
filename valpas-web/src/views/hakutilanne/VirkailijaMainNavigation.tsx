import * as A from "fp-ts/Array"
import React, { useMemo } from "react"
import {
  MainNavigation,
  MainNavigationItem,
} from "../../components/navigation/MainNavigation"
import { t } from "../../i18n/i18n"
import { useKäyttöoikeusroolit } from "../../state/accessRights"
import { Kayttooikeusrooli, käyttöoikeusrooliEq } from "../../state/common"
import { isFeatureFlagEnabled } from "../../state/featureFlags"
import { intersects } from "../../utils/arrays"

type NavOption = MainNavigationItem & { visibleToRoles: Kayttooikeusrooli[] }

const allNavOptions: NavOption[] = [
  {
    display: t("ylänavi__hakeutumisvelvolliset"),
    linkTo: "/hakutilanne",
    visibleToRoles: ["OPPILAITOS_HAKEUTUMINEN"],
  },
  {
    display: t("ylänavi__maksuttomuusoikeuden_arviointi"),
    linkTo: "/maksuttomuus",
    visibleToRoles: ["OPPILAITOS_MAKSUTTOMUUS"],
  },
]

export const VirkailijaMainNavigation = () => {
  const roles = useKäyttöoikeusroolit()

  const navOptions: MainNavigationItem[] = useMemo(() => {
    const hasRole = intersects(käyttöoikeusrooliEq)(roles)
    return allNavOptions.filter((item) => hasRole(item.visibleToRoles))
  }, [roles])

  return isFeatureFlagEnabled("maksuttomuus") && A.isNonEmpty(navOptions) ? (
    <MainNavigation
      title={t("ylänavi__otsikko_oppilaitos")}
      options={navOptions}
    />
  ) : null
}
