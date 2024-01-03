import React, { useState } from 'react'
import { useApiMethod, useOnApiSuccess } from '../../api-fetch'
import { formatDateRange } from '../../date/date'
import { t } from '../../i18n/i18n'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { invalidateOpiskeluoikeus } from '../../util/koskiApi'
import { getOpiskeluoikeusOid } from '../../util/opiskeluoikeus'
import { RequiresWriteAccess } from '../access/RequiresWriteAccess'
import { Column, ColumnRow } from '../containers/Columns'
import { FlatButton } from '../controls/FlatButton'
import { RaisedButton } from '../controls/RaisedButton'
import { Trans } from '../texts/Trans'
import { TestIdLayer, TestIdRoot, TestIdText } from '../../appstate/useTestId'

export type OpiskeluoikeusEditToolbarProps = {
  opiskeluoikeus: Opiskeluoikeus
  editMode: boolean
  invalidatable: boolean
  onStartEdit: () => void
}

export const OpiskeluoikeusEditToolbar = (
  props: OpiskeluoikeusEditToolbarProps
) => {
  const spans = props.editMode ? [12, 12] : [21, 3]
  const opiskeluoikeusOid = getOpiskeluoikeusOid(props.opiskeluoikeus)

  return (
    <ColumnRow>
      <Column span={{ default: spans[0], phone: 24 }}>
        <TestIdText id="voimassaoloaika">
          <Trans>{'Opiskeluoikeuden voimassaoloaika'}</Trans>
          {': '}
          {'arvioituPäättymispäivä' in props.opiskeluoikeus &&
          !props.opiskeluoikeus.päättymispäivä
            ? `${formatDateRange(
                props.opiskeluoikeus.alkamispäivä,
                props.opiskeluoikeus.arvioituPäättymispäivä
              )} (${t('Arvioitu päättymispäivä')})`
            : formatDateRange(
                props.opiskeluoikeus.alkamispäivä,
                props.opiskeluoikeus.päättymispäivä
              )}
        </TestIdText>
      </Column>
      <Column
        span={{ default: spans[1], phone: 24 }}
        align={{ default: 'right', phone: 'left' }}
      >
        <RequiresWriteAccess opiskeluoikeus={props.opiskeluoikeus}>
          {props.editMode ? (
            props.invalidatable &&
            opiskeluoikeusOid && (
              <MitätöintiButton opiskeluoikeusOid={opiskeluoikeusOid} />
            )
          ) : (
            <RaisedButton fullWidth onClick={props.onStartEdit} testId="edit">
              {'Muokkaa'}
            </RaisedButton>
          )}
        </RequiresWriteAccess>
      </Column>
    </ColumnRow>
  )
}

type MitätöintiButtonProps = {
  opiskeluoikeusOid: string
}

const MitätöintiButton: React.FC<MitätöintiButtonProps> = (props) => {
  const invalidate = useApiMethod(invalidateOpiskeluoikeus)
  const [confirmationVisible, setConfirmationVisible] = useState(false)
  useOnApiSuccess(invalidate, () => {
    window.location.replace('/koski/virkailija')
  })

  return (
    <TestIdLayer id="invalidate">
      {confirmationVisible ? (
        <>
          <RaisedButton
            type="dangerzone"
            onClick={() => invalidate.call(props.opiskeluoikeusOid)}
            testId="confirm"
          >
            {'Vahvista mitätöinti, operaatiota ei voi peruuttaa'}
          </RaisedButton>
          <FlatButton
            onClick={() => setConfirmationVisible(false)}
            testId="cancel"
          >
            {'Peruuta mitätöinti'}
          </FlatButton>
        </>
      ) : (
        <FlatButton
          onClick={() => setConfirmationVisible(true)}
          testId="button"
        >
          {'Mitätöi opiskeluoikeus'}
        </FlatButton>
      )}
    </TestIdLayer>
  )
}
