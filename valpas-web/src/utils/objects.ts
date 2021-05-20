export const isEntry = (arr: string[]): arr is [string, string] =>
  arr.length === 2

export const fromEntries = <T>(entries: [string, T][]): Record<string, T> =>
  entries.reduce(
    (obj, entry) => ({
      ...obj,
      [entry[0]]: entry[1],
    }),
    {}
  )

export const isEmptyObject = <T extends object>(obj: T): boolean =>
  Object.entries(obj).length === 0

export const removeFalsyValues = <T extends object>(obj: T): Partial<T> => {
  const partial: Partial<T> = {}
  for (const key in obj) {
    if (obj[key]) {
      partial[key] = obj[key]
    }
  }
  return partial
}
