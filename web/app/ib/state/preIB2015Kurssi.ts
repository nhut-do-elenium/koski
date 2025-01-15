import { useMemo } from 'react'
import {
  DialogField,
  useDialogField
} from '../../components-v2/createdialog/DialogField'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusKursseissa } from '../../types/fi/oph/koski/schema/LaajuusKursseissa'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { PreIBKurssinSuoritus2015 } from '../../types/fi/oph/koski/schema/PreIBKurssinSuoritus2015'
import {
  createPreIBKurssinSuoritus2015,
  PreIB2015KurssiOppiaineenTunniste,
  PreIB2015OsasuoritusTunniste,
  PreIBKurssi2015Props
} from '../oppiaineet/preIBKurssi2015'
import { uusiPaikallinenKey } from './options'

export const UusiPaikallinenLukionKurssiKey = uusiPaikallinenKey('lukio')
export const UusiIBKurssiKey = uusiPaikallinenKey('ib')

export type PreIB2015OsasuoritusState = {
  tunniste: DialogField<PreIB2015OsasuoritusTunniste>
  uusiTyyppi: DialogField<UusiOsasuoritustyyppi>
  lukiokurssinTyyppi: DialogField<Koodistokoodiviite<'lukionkurssintyyppi'>>
  kuvaus: DialogField<LocalizedString>
  pakollinen: DialogField<boolean>
  laajuus: DialogField<LaajuusKursseissa>
  isPaikallinen: boolean
  result: PreIBKurssinSuoritus2015 | null
}

export type UusiOsasuoritustyyppi = 'lukio' | 'ib'

export const usePreIB2015OsasuoritusState = (
  oppiaineenTunniste: PreIB2015KurssiOppiaineenTunniste
): PreIB2015OsasuoritusState => {
  const tunniste = useDialogField<PreIB2015OsasuoritusTunniste>(true)
  const uusiTyyppi = useDialogField<UusiOsasuoritustyyppi>(false)

  const isLukioValtakunnallinen = !uusiTyyppi.value
  const isLukioPaikallinen = uusiTyyppi.value === 'lukio'
  const isIBKurssi = uusiTyyppi.value === 'ib'

  const lukiokurssinTyyppi = useDialogField<
    Koodistokoodiviite<'lukionkurssintyyppi'>
  >(isLukioValtakunnallinen || isLukioPaikallinen)

  const pakollinen = useDialogField<boolean>(isIBKurssi)

  const kuvaus = useDialogField<LocalizedString>(
    isLukioPaikallinen || isIBKurssi
  )

  const laajuus = useDialogField<LaajuusKursseissa>(
    isLukioPaikallinen || isIBKurssi,
    () => LaajuusKursseissa({ arvo: 1 })
  )

  const result = useMemo(
    () =>
      createPreIBKurssinSuoritus2015({
        oppiaineenTunniste,
        tunniste: tunniste.value,
        lukiokurssinTyyppi: lukiokurssinTyyppi.value,
        kuvaus: kuvaus.value,
        pakollinen: pakollinen.value,
        laajuus: laajuus.value
      }),
    [
      oppiaineenTunniste,
      tunniste.value,
      lukiokurssinTyyppi.value,
      kuvaus.value,
      pakollinen.value,
      laajuus.value
    ]
  )

  return {
    tunniste,
    uusiTyyppi,
    lukiokurssinTyyppi,
    kuvaus,
    pakollinen,
    laajuus,
    isPaikallinen: isLukioPaikallinen || isIBKurssi,
    result
  }
}
