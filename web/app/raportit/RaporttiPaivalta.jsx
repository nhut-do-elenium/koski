import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import {showError} from '../util/location'
import {formatISODate} from '../date/date'
import {generateRandomPassword} from '../util/password'
import {downloadExcel} from './downloadExcel'
import { LyhytKuvaus, PaivaValinta, RaportinLataus, Vinkit } from './raporttiComponents'

export const RaporttiPaivalta = ({
  organisaatioP,
  apiEndpoint,
  shortDescription,
  dateInputHelp,
  example
}) => {
  const paivaAtom = Atom()
  const submitBus = Bacon.Bus()

  const password = generateRandomPassword()

  const downloadExcelP = Bacon.combineWith(
    organisaatioP, paivaAtom,
    (o, p) => o && p && ({oppilaitosOid: o.oid, paiva: formatISODate(p), password, baseUrl: `/koski/api/raportit${apiEndpoint}`})
  )
  const downloadExcelE = submitBus.map(downloadExcelP)
    .flatMapLatest(downloadExcel)

  downloadExcelE.onError(e => { showError(e) })

  const inProgressP = submitBus.awaiting(downloadExcelE.mapError())
  const submitEnabledP = downloadExcelP.map(x => !!x).and(inProgressP.not())

  return (
    <section>
      {shortDescription && <LyhytKuvaus>{shortDescription}</LyhytKuvaus>}

      <PaivaValinta
        paivaAtom={paivaAtom}
        ohje={dateInputHelp}
      />

      <RaportinLataus
        password={password}
        inProgressP={inProgressP}
        submitEnabledP={submitEnabledP}
        submitBus={submitBus}
      />

      {example && <Vinkit>{example}</Vinkit>}
    </section>
  )
}
