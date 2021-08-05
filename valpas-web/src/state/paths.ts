import { RouteComponentProps } from "react-router"
import { nonNull } from "../utils/arrays"
import { fromEntries, isEntry } from "../utils/objects"
import { Oid } from "./common"

export type QueryParams = Record<
  string,
  string | number | boolean | undefined | null
>

export const queryPath = (path: string, query: QueryParams) =>
  [path, queryString(query)].filter((s) => s.length > 0).join("?")

export const queryString = (query: QueryParams) =>
  Object.entries(query)
    .filter(([_key, value]) => nonNull(value))
    .map(
      ([key, value]) =>
        encodeURIComponent(key) + "=" + encodeURIComponent(value!!)
    )
    .join("&")

export const parseQueryFromProps = (
  props: RouteComponentProps
): Record<string, string> => {
  const query = props.location.search.split("?")[1]
  return query
    ? fromEntries(
        query
          .split("&")
          .map((entry) => entry.split("=").map(decodeURIComponent))
          .filter(isEntry)
      )
    : {}
}

// Etusivu

export const rootPath = (basePath: string = "") => `${basePath}/`

// Hakutilannenäkymä

export const hakutilannePathWithoutOrg = (basePath: string = "") =>
  `${basePath}/hakutilanne`

export const createHakutilannePathWithoutOrg = hakutilannePathWithoutOrg

export const hakutilannePathWithOrg = (basePath: string = "") =>
  `${basePath}/hakutilanne/:organisaatioOid`

export const createHakutilannePathWithOrg = (
  basePath: string = "",
  params: { organisaatioOid: Oid }
) => `${basePath}/hakutilanne/${params.organisaatioOid}`

export type HakutilanneViewRouteProps = RouteComponentProps<{
  organisaatioOid?: string
}>

// Oppijakohtainen näkymä

export const oppijaPath = (basePath: string = "") =>
  `${basePath}/oppija/:oppijaOid`

export const createOppijaPath = (
  basePath: string = "",
  params: {
    oppijaOid: Oid
    hakutilanneRef?: Oid
    kuntailmoitusRef?: Oid
    suorittaminenRef?: Oid
    prev?: string
  }
) =>
  queryPath(`${basePath}/oppija/${params.oppijaOid}`, {
    hakutilanneRef: params.hakutilanneRef,
    kuntailmoitusRef: params.kuntailmoitusRef,
    prev: params.prev,
  })

export type OppijaViewRouteProps = RouteComponentProps<{
  oppijaOid?: string
}>

// Suorittamisnäkymä
export const suorittaminenPath = (basePath: string = "") =>
  `${basePath}/suorittaminen`

export const createSuorittaminenPath = suorittaminenPath

export const suorittaminenPathWithOrg = (basePath: string = "") =>
  `${suorittaminenPath(basePath)}/organisaatio/:organisaatioOid`

export const createSuorittaminenPathWithOrg = (
  basePath: string = "",
  organisaatioOid: Oid
) => `${suorittaminenPath(basePath)}/organisaatio/${organisaatioOid}`

// Suorittamisen hakunäkymä
export const suorittaminenHetuhakuPath = (basePath: string = "") =>
  `${suorittaminenPath(basePath)}/haku`

export const createSuorittaminenHetuhakuPath = suorittaminenHetuhakuPath

// Maksuttomuusnäkymä

export const maksuttomuusPath = (basePath: string = "") =>
  `${basePath}/maksuttomuus`

export const createMaksuttomuusPath = maksuttomuusPath

// Käyttöoikeusnäkymä

export const käyttöoikeusPath = (basePath: string = "") =>
  `${basePath}/kayttooikeudet`

export const createKäyttöoikeusPath = käyttöoikeusPath

// Kuntanäkymien juuri

export const kuntaRootPath = (basePath: string = "") =>
  `${basePath}/kuntailmoitukset`

// Kuntailmoitusnäkymä

export const kuntailmoitusPath = kuntaRootPath

export const createKuntailmoitusPath = kuntailmoitusPath

export const kuntailmoitusPathWithOrg = (basePath: string = "") =>
  `${kuntailmoitusPath(basePath)}/organisaatio/:organisaatioOid`

export const createKuntailmoitusPathWithOrg = (
  basePath: string = "",
  organisaatioOid: Oid
) => `${kuntailmoitusPath(basePath)}/organisaatio/${organisaatioOid}`

// Kuntakäyttäjän hakunäkymä

export const kunnanHetuhakuPath = (basePath: string = "") =>
  `${kuntaRootPath(basePath)}/haku`

export const createKunnanHetuhakuPath = kunnanHetuhakuPath
