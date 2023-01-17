import React from 'react'
import { useLayout } from '../../util/useDepth'
import { common, CommonPropsWithChildren } from '../CommonProps'
import { Column, ColumnRow, COLUMN_COUNT } from '../containers/Columns'
import { OSASUORITUSTABLE_DEPTH_KEY } from './OsasuoritusTable'

const LABEL_WIDTH_COLUMNS = 4

export type OsasuoritusPropertyProps = CommonPropsWithChildren<{
  label: string
}>

export const OsasuoritusProperty: React.FC<OsasuoritusPropertyProps> = (
  props
) => {
  const [indentation, LayoutProvider] = useLayout(OSASUORITUSTABLE_DEPTH_KEY)
  return (
    <ColumnRow {...common(props, ['OsasuoritusProperty'])} indent={indentation}>
      <OsasuoritusPropertyLabel>{props.label}</OsasuoritusPropertyLabel>
      <LayoutProvider indent={LABEL_WIDTH_COLUMNS}>
        {props.children}
      </LayoutProvider>
    </ColumnRow>
  )
}

export type OsasuoritusSubpropertyProps = CommonPropsWithChildren<{
  label: string
  rowNumber?: number
}>

export const OsasuoritusSubproperty: React.FC<OsasuoritusSubpropertyProps> = (
  props
) => {
  return (
    <>
      <OsasuoritusPropertyLabel key="subprop-label" row={props.rowNumber}>
        {props.label}
      </OsasuoritusPropertyLabel>
      <OsasuoritusPropertyValue key="subprop-value" row={props.rowNumber}>
        {props.children}
      </OsasuoritusPropertyValue>
    </>
  )
}

export type OsasuoritusPropertyLabel = CommonPropsWithChildren<{
  row?: number
}>

export const OsasuoritusPropertyLabel: React.FC<OsasuoritusPropertyLabel> = (
  props
) => {
  const [indentation] = useLayout(OSASUORITUSTABLE_DEPTH_KEY)
  return (
    <Column
      row={props.row || 0}
      start={indentation}
      span={LABEL_WIDTH_COLUMNS}
      {...common(props, ['OsasuoritusPropertyLabel'])}
    >
      {props.children}
    </Column>
  )
}

export type OsasuoritusPropertyValueProps = CommonPropsWithChildren<{
  row?: number
}>

export const OsasuoritusPropertyValue: React.FC<
  OsasuoritusPropertyValueProps
> = (props) => {
  const [indentation] = useLayout(OSASUORITUSTABLE_DEPTH_KEY)
  const span = COLUMN_COUNT - indentation - LABEL_WIDTH_COLUMNS

  return (
    <Column
      row={props.row || 0}
      start={indentation + LABEL_WIDTH_COLUMNS}
      span={span}
      {...common(props, ['OsasuoritusPropertyValue'])}
    >
      {props.children}
    </Column>
  )
}
