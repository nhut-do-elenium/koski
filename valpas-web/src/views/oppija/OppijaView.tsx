import bem from "bem-ts"
import React from "react"
import { fetchOppija, fetchOppijaCache } from "../../api/api"
import { ApiMethodState, useApiWithParams } from "../../api/apiHooks"
import { isSuccess, mapError, mapLoading, mapSuccess } from "../../api/apiUtils"
import { ButtonLabel } from "../../components/buttons/ButtonLabel"
import { FlatLink } from "../../components/buttons/FlatButton"
import {
  BorderlessCard,
  CardBody,
  CardHeader,
} from "../../components/containers/cards"
import { Column, ColumnsContainer } from "../../components/containers/Columns"
import { Page } from "../../components/containers/Page"
import { BackIcon } from "../../components/icons/Icon"
import { Spinner } from "../../components/icons/Spinner"
import { InfoTooltip } from "../../components/tooltip/InfoTooltip"
import { Heading } from "../../components/typography/headings"
import { T, t } from "../../i18n/i18n"
import { OppijaHakutilanteillaLaajatTiedot } from "../../state/oppijat"
import {
  createPerusopetusPathWithOrg as createPerusopetusPathWithOrg,
  createPerusopetusPathWithoutOrg as createPerusopetusPathWithoutOrg,
  OppijaViewRouteProps,
  parseQueryFromProps as parseSearchQueryFromProps,
} from "../../state/paths"
import { plainComponent } from "../../utils/plaincomponent"
import { OppijanHaut } from "./OppijanHaut"
import { OppijanOpiskeluhistoria } from "./OppijanOpiskeluhistoria"
import { OppijanOppivelvollisuustiedot } from "./OppijanOppivelvollisuustiedot"
import { OppijanYhteystiedot } from "./OppijanYhteystiedot"
import "./OppijaView.less"

const b = bem("oppijaview")

export type OppijaViewProps = OppijaViewRouteProps

export const OppijaView = (props: OppijaViewProps) => {
  const searchQuery = parseSearchQueryFromProps(props)
  const queryOid = props.match.params.oppijaOid
  const oppija = useApiWithParams(fetchOppija, [queryOid], fetchOppijaCache)

  return (
    <Page id="oppija">
      <BackNav
        organisaatioRef={searchQuery.organisaatioRef}
        oppija={isSuccess(oppija) ? oppija.data : undefined}
      />
      <OppijaHeadings oppija={oppija} oid={queryOid} />

      {mapLoading(oppija, () => (
        <Spinner />
      ))}

      {mapSuccess(oppija, (oppijaData: OppijaHakutilanteillaLaajatTiedot) => (
        <>
          <ColumnsContainer>
            <Column size={4}>
              <BorderlessCard id="oppivelvollisuustiedot">
                <CardHeader>
                  <T id="oppija__oppivelvollisuustiedot_otsikko" />
                </CardHeader>
                <CardBody>
                  <OppijanOppivelvollisuustiedot oppija={oppijaData} />
                </CardBody>
              </BorderlessCard>
            </Column>
            <Column size={8}>
              <BorderlessCard id="yhteystiedot">
                <CardHeader>
                  <T id="oppija__yhteystiedot_otsikko" />
                  <InfoTooltip>
                    <T id="oppija__yhteystiedot_tooltip" />
                  </InfoTooltip>
                </CardHeader>
                <CardBody>
                  <OppijanYhteystiedot
                    henkilö={oppijaData.oppija.henkilö}
                    yhteystiedot={oppijaData.yhteystiedot}
                  />
                </CardBody>
              </BorderlessCard>
            </Column>
          </ColumnsContainer>
          <ColumnsContainer>
            <Column size={4}>
              <BorderlessCard id="opiskeluhistoria">
                <CardHeader>
                  <T id="oppija__opiskeluhistoria_otsikko" />
                </CardHeader>
                <CardBody>
                  <OppijanOpiskeluhistoria oppija={oppijaData} />
                </CardBody>
              </BorderlessCard>
            </Column>
            <Column size={8}>
              <BorderlessCard id="haut">
                <CardHeader>
                  <T id="oppija__haut_otsikko" />
                </CardHeader>
                <CardBody>
                  <OppijanHaut oppija={oppijaData} />
                </CardBody>
              </BorderlessCard>
            </Column>
          </ColumnsContainer>
        </>
      ))}
    </Page>
  )
}

type BackNavProps = {
  organisaatioRef?: string
  oppija?: OppijaHakutilanteillaLaajatTiedot
}

const BackNav = (props: BackNavProps) => {
  const organisaatioOid =
    props.organisaatioRef || props.oppija?.oppija.oikeutetutOppilaitokset[0]
  const targetPath = organisaatioOid
    ? createPerusopetusPathWithOrg("", { organisaatioOid })
    : createPerusopetusPathWithoutOrg("")

  return (
    <div className={b("backbutton")}>
      <FlatLink to={targetPath}>
        <BackIcon />
        <ButtonLabel>
          <T id="oppija__muut_oppijat" />
        </ButtonLabel>
      </FlatLink>
    </div>
  )
}

const OppijaHeadings = (props: {
  oppija: ApiMethodState<OppijaHakutilanteillaLaajatTiedot>
  oid: string
}) => (
  <>
    <Heading>
      {mapLoading(props.oppija, () => t("oppija__oletusotsikko"))}
      {mapSuccess(
        props.oppija,
        (oppija) =>
          `${oppija.oppija.henkilö.sukunimi} ${oppija.oppija.henkilö.etunimet} (${oppija.oppija.henkilö.hetu})`
      )}
      {mapError(props.oppija, () => t("oppija__oletusotsikko"))}
    </Heading>
    <SecondaryOppijaHeading>
      {mapSuccess(props.oppija, (oppija) =>
        t("oppija__oppija_oid", { oid: oppija.oppija.henkilö.oid })
      )}
      {mapError(props.oppija, () => t("oppija__ei_löydy", { oid: props.oid }))}
    </SecondaryOppijaHeading>
  </>
)

const SecondaryOppijaHeading = plainComponent("h2", b("secondaryheading"))
