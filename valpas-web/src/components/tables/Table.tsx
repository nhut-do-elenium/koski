import bem from "bem-ts"
import React from "react"
import { joinClassNames } from "../../utils/classnames"
import "./Table.less"

const b = bem("table")

export type TableCellSize = "xsmall" | "small" | "large"

export type TableProps = React.HTMLAttributes<HTMLTableElement>

export const Table = ({ children, className, ...rest }: TableProps) => (
  <table {...rest} className={joinClassNames(b(), className)}>
    {children}
  </table>
)

export const TableHeader = ({
  className,
  ...rest
}: React.HTMLAttributes<HTMLTableSectionElement>) => (
  <thead {...rest} className={joinClassNames(b("head"), className)} />
)

export const TableBody = ({
  className,
  ...rest
}: React.HTMLAttributes<HTMLTableSectionElement>) => (
  <tbody {...rest} className={joinClassNames(b("body"), className)} />
)

export const Row = ({
  className,
  ...rest
}: React.HTMLAttributes<HTMLTableRowElement>) => (
  <tr {...rest} className={joinClassNames(b("row"), className)} />
)

export type DataProps = React.HTMLAttributes<HTMLTableDataCellElement> & {
  icon?: React.ReactNode
  size?: TableCellSize
  indicatorSpace?: boolean
}

export const Data = ({
  className,
  children,
  icon,
  size,
  indicatorSpace,
  ...rest
}: DataProps) => (
  <td
    {...rest}
    className={cellClassNames("td", { size, indicatorSpace, className })}
  >
    {icon && <div className={b("icon")}>{icon}</div>}
    {children}
  </td>
)

export type HeaderCellProps = React.HTMLAttributes<HTMLTableHeaderCellElement> & {
  size?: TableCellSize
  indicatorSpace?: boolean
}

export const HeaderCell = ({
  className,
  size,
  indicatorSpace,
  ...rest
}: HeaderCellProps) => (
  <th
    {...rest}
    className={cellClassNames("th", { size, indicatorSpace, className })}
  />
)

const cellClassNames = (
  element: string,
  props: {
    size?: TableCellSize
    indicatorSpace?: boolean
    className?: string
  }
) =>
  joinClassNames(
    b(element, [props.size, props.indicatorSpace ? "indicator" : undefined]),
    props.className
  )
