import { useMemo } from 'react'
import { isSuccess, useApiOnce } from '../api-fetch'
import { useSchema } from '../appstate/constraints'
import { Peruste } from '../appstate/peruste'
import { todayISODate } from '../date/date'
import { OrganisaatioHierarkia } from '../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { isSuorituskielellinen } from '../types/fi/oph/koski/schema/Suorituskielellinen'
import { OpiskeluoikeusClass } from '../types/fi/oph/koski/typemodel/OpiskeluoikeusClass'
import * as C from '../util/constraints'
import { fetchOpiskeluoikeusClassMapping } from '../util/koskiApi'
import { createOpiskeluoikeus } from './opintooikeus/createOpiskeluoikeus'
import {
  opiskeluoikeustyyppiToClassNames,
  opiskeluoikeudenLisätiedotClass,
  asObject
} from './UusiOpiskeluoikeusForm'

export type UusiOpiskeluoikeusDialogState = {
  hankintakoulutus: DialogField<Hankintakoulutus>
  oppilaitos: DialogField<OrganisaatioHierarkia>
  opiskeluoikeus: DialogField<Koodistokoodiviite<'opiskeluoikeudentyyppi'>>
  päätasonSuoritus: DialogField<Koodistokoodiviite<'suorituksentyyppi'>>
  peruste: DialogField<Peruste>
  suorituskieli: DialogField<Koodistokoodiviite<'kieli'>>
  aloituspäivä: DialogField<string>
  tila: DialogField<Koodistokoodiviite<'koskiopiskeluoikeudentila'>>
  maksuton: DialogField<boolean | null>
  opintojenRahoitus: DialogField<Koodistokoodiviite<'opintojenrahoitus'>>
  tuvaJärjestämislupa: DialogField<Koodistokoodiviite<'tuvajarjestamislupa'>>
  jotpaAsianumero: DialogField<Koodistokoodiviite<'jotpaasianumero'>>
  opintokokonaisuus: DialogField<Koodistokoodiviite<'opintokokonaisuudet'>>
  tpoOppimäärä: DialogField<Koodistokoodiviite<'taiteenperusopetusoppimaara'>>
  tpoTaiteenala: DialogField<Koodistokoodiviite<'taiteenperusopetustaiteenala'>>
  tpoToteutustapa: DialogField<
    Koodistokoodiviite<'taiteenperusopetuskoulutuksentoteutustapa'>
  >
  varhaiskasvatuksenJärjestämistapa: DialogField<
    Koodistokoodiviite<'vardajarjestamismuoto'>
  >
  osaamismerkki: DialogField<Koodistokoodiviite<'osaamismerkit'>>
  ooMapping?: OpiskeluoikeusClass[]
  result?: Opiskeluoikeus
}

export type Hankintakoulutus = 'esiopetus' | 'tpo' | undefined
export const useUusiOpiskeluoikeusDialogState =
  (): UusiOpiskeluoikeusDialogState => {
    const ooMappingCall = useApiOnce(fetchOpiskeluoikeusClassMapping)
    const ooMapping = isSuccess(ooMappingCall) ? ooMappingCall.data : undefined
    const suoritusMapping = useMemo(
      () => (ooMapping || []).flatMap((oo) => oo.suoritukset),
      [ooMapping]
    )

    // Oppilaitos
    const hankintakoulutus = useDialogField<Hankintakoulutus>(true)
    const oppilaitos = useDialogField<OrganisaatioHierarkia>(true)
    const oppilaitosValittu = oppilaitos.value !== undefined

    // Opiskeluoikeus
    const opiskeluoikeus =
      useDialogField<Koodistokoodiviite<'opiskeluoikeudentyyppi'>>(
        oppilaitosValittu
      )
    const opiskeluoikeusValittu = opiskeluoikeus.value !== undefined
    const opiskeluoikeusClass = opiskeluoikeustyyppiToClassNames(
      ooMapping,
      opiskeluoikeus.value?.koodiarvo
    )
    const opiskeluoikeudenLisätiedot = useSchema(
      opiskeluoikeudenLisätiedotClass(opiskeluoikeusClass)
    )

    // Päätason suoritus
    const päätasonSuoritus = useDialogField<
      Koodistokoodiviite<'suorituksentyyppi'>
    >(opiskeluoikeusValittu)
    const päätasonSuoritusClass = suoritusMapping.find(
      (s) => s.tyyppi === päätasonSuoritus.value?.koodiarvo
    )?.className
    const päätasonSuoritusValittu = päätasonSuoritus.value !== undefined

    // Peruste
    const peruste = useDialogField<Peruste>(päätasonSuoritusValittu)

    // Suorituskieli
    const suorituskieli = useDialogField<Koodistokoodiviite<'kieli'>>(
      isSuorituskielellinen(asObject(päätasonSuoritusClass))
    )

    // Aloituspäivä
    const aloituspäivä = useDialogField<string>(
      opiskeluoikeusValittu,
      todayISODate
    )

    // Opiskeluoikeuden tila
    const tila = useDialogField<
      Koodistokoodiviite<'koskiopiskeluoikeudentila'>
    >(opiskeluoikeusValittu)

    // Opiskelun maksuttomuus
    const maksuttomuustiedollinen = C.hasProp(
      opiskeluoikeudenLisätiedot,
      'maksuttomuus'
    )
    const maksuton = useDialogField<boolean | null>(
      maksuttomuustiedollinen && päätasonSuoritus.value
        ? ![
            // Päätason suoritukset, joille maksuttomuusvalintaa ei näytetä, vaikka se opiskeluoikeuden tiedoista löytyykin:
            'vstjotpakoulutus',
            'vstosaamismerkki',
            'vstvapaatavoitteinenkoulutus'
          ].includes(päätasonSuoritus.value?.koodiarvo)
        : false
    )

    // Opintojen rahoitus
    const opintojenRahoitus = useDialogField<
      Koodistokoodiviite<'opintojenrahoitus'>
    >(päätasonSuoritusValittu)

    // Tuva-järjestämislupa
    const tuvaJärjestämislupa =
      useDialogField<Koodistokoodiviite<'tuvajarjestamislupa'>>(true)

    // Opintokokonaisuus (vst jotpa, vst vapaatavoitteinen, sekä muu kuin säännelty koulutus)
    const opintokokonaisuus =
      useDialogField<Koodistokoodiviite<'opintokokonaisuudet'>>(true)

    // Jotpa-asianumerollinen
    const jotpaAsianumero = useDialogField<
      Koodistokoodiviite<'jotpaasianumero'>
    >(C.hasProp(opiskeluoikeudenLisätiedot, 'jotpaAsianumero'))

    // Taiteen perusopetuksen oppimäärä, taiteenala ja koulutuksen toteutustapa
    const tpoOppimäärä =
      useDialogField<Koodistokoodiviite<'taiteenperusopetusoppimaara'>>(true)
    const tpoTaiteenala =
      useDialogField<Koodistokoodiviite<'taiteenperusopetustaiteenala'>>(true)
    const tpoToteutustapa =
      useDialogField<
        Koodistokoodiviite<'taiteenperusopetuskoulutuksentoteutustapa'>
      >(true)

    // Varhaiskasvatuksen järjestämistapa
    const varhaiskasvatuksenJärjestämistapa = useDialogField<
      Koodistokoodiviite<'vardajarjestamismuoto'>
    >(hankintakoulutus.value === 'esiopetus')

    // Vapaan sivistystyön koulutuksen osaamismerkki
    const osaamismerkki =
      useDialogField<Koodistokoodiviite<'osaamismerkit'>>(true)

    // Validi opiskeluoikeus
    const result = useMemo(
      () =>
        oppilaitos.value &&
        opiskeluoikeus.value &&
        päätasonSuoritus.value &&
        aloituspäivä.value &&
        tila.value
          ? createOpiskeluoikeus(
              oppilaitos.value,
              opiskeluoikeus.value,
              päätasonSuoritus.value,
              peruste.value,
              aloituspäivä.value,
              tila.value,
              suorituskieli.value,
              maksuton.value,
              opintojenRahoitus.value,
              tuvaJärjestämislupa.value,
              opintokokonaisuus.value,
              jotpaAsianumero.value,
              tpoOppimäärä.value,
              tpoTaiteenala.value,
              tpoToteutustapa.value,
              varhaiskasvatuksenJärjestämistapa.value,
              osaamismerkki.value
            )
          : undefined,
      [
        aloituspäivä.value,
        jotpaAsianumero.value,
        maksuton.value,
        opintojenRahoitus.value,
        opintokokonaisuus.value,
        opiskeluoikeus.value,
        oppilaitos.value,
        osaamismerkki.value,
        peruste.value,
        päätasonSuoritus.value,
        suorituskieli.value,
        tila.value,
        tpoOppimäärä.value,
        tpoTaiteenala.value,
        tpoToteutustapa.value,
        tuvaJärjestämislupa.value,
        varhaiskasvatuksenJärjestämistapa.value
      ]
    )

    return {
      hankintakoulutus,
      oppilaitos,
      opiskeluoikeus,
      päätasonSuoritus,
      peruste,
      suorituskieli,
      aloituspäivä,
      tila,
      maksuton,
      opintojenRahoitus,
      tuvaJärjestämislupa,
      jotpaAsianumero,
      opintokokonaisuus,
      tpoOppimäärä,
      tpoTaiteenala,
      tpoToteutustapa,
      varhaiskasvatuksenJärjestämistapa,
      osaamismerkki,
      ooMapping,
      result
    }
  }

import { useEffect, useState } from 'react'

export type DialogField<T> = {
  value?: T
  set: (t?: T) => void
  visible: boolean
}

export const useDialogField = <T>(
  isVisible: boolean,
  defaultValue?: () => T
): DialogField<T> => {
  const [value, set] = useState<T | undefined>(defaultValue)
  const [visible, setVisible] = useState<boolean>(false)

  useEffect(() => {
    setVisible(isVisible)
    if (!isVisible) {
      set(defaultValue)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isVisible])

  return { value, set, visible }
}
