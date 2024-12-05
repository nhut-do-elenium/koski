import React, { useState } from 'react'
import { TestIdLayer } from '../../appstate/useTestId'
import { t } from '../../i18n/i18n'
import { TextEdit } from '../controls/TextField'

export type PaikallinenKoulutusFieldsProps = {
  onChange: (values?: PaikallinenKoulutus) => void
}

export type PaikallinenKoulutus = {
  nimi: string
  koodiarvo: string
  kuvaus: string
}

const emptyPaikallinenKoulutus: PaikallinenKoulutus = {
  nimi: '',
  koodiarvo: '',
  kuvaus: ''
}

export const PaikallinenKoulutusFields = (
  props: PaikallinenKoulutusFieldsProps
) => {
  const [koulutus, setKoulutus] = useState<PaikallinenKoulutus>(
    emptyPaikallinenKoulutus
  )

  const update = (field: keyof PaikallinenKoulutus) => (value?: string) => {
    const patched: PaikallinenKoulutus = { ...koulutus, [field]: value }
    setKoulutus(patched)
    props.onChange(
      patched.nimi && patched.koodiarvo && patched.kuvaus ? patched : undefined
    )
  }

  return (
    <TestIdLayer id="paikallinenKoulutus">
      <section className="PaikallinenKoulutus">
        <label>
          {t('Nimi')}
          <TextEdit
            value={koulutus.nimi}
            onChange={update('nimi')}
            testId="nimi"
          />
        </label>
        <label>
          {t('Koodiarvo')}
          <TextEdit
            value={koulutus.koodiarvo}
            onChange={update('koodiarvo')}
            testId="koodiarvo"
          />
        </label>
        <label>
          {t('Kuvaus')}
          <TextEdit
            value={koulutus.kuvaus}
            onChange={update('kuvaus')}
            testId="kuvaus"
          />
        </label>
      </section>
    </TestIdLayer>
  )
}
