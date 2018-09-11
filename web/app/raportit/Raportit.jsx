import React from 'baret'
import {t} from '../i18n/i18n'
import Text from '../i18n/Text'
import Bacon from 'baconjs'
import OrganisaatioPicker from '../virkailija/OrganisaatioPicker'
import Atom from 'bacon.atom'
import DateInput from '../date/DateInput'
import {showError} from '../util/location'
import {formatISODate} from '../date/date'
import {appendQueryParams} from '../util/location'
import {generateRandomPassword} from '../util/password'

export const raportitContentP = () => {
  const oppilaitosAtom = Atom()
  const alkuAtom = Atom()
  const loppuAtom = Atom()
  const submitBus = Bacon.Bus()

  const password = generateRandomPassword()

  const downloadExcelP = Bacon.combineWith(
    oppilaitosAtom, alkuAtom, loppuAtom,
    (o, a, l) => o && a && l && (l.valueOf() >= a.valueOf()) && {oppilaitosOid: o.oid, alku: formatISODate(a), loppu: formatISODate(l), password}
  )
  const downloadExcelE = submitBus.map(downloadExcelP)
    .flatMapLatest(downloadExcel)

  downloadExcelE.onError(e => { showError(e) })

  const inProgressP = submitBus.awaiting(downloadExcelE.mapError())
  const submitEnabledP = downloadExcelP.map(x => !!x).and(inProgressP.not())
  const buttonTextP = inProgressP.map((inProgress) => <Text name={!inProgress ? 'Lataa Excel-tiedosto' : 'Ladataan...'}/>)

  return Bacon.constant({
    content: (<div className='content-area'>
      <form className='raportit main-content'>
        <h2><Text name='Opiskelijavuositiedot'/></h2>
        <p><Text name='Opiskelijavuositiedot-description'/></p>
        <Oppilaitos oppilaitosAtom={oppilaitosAtom} />
        <div className='aloituspaiva'>
          <label><Text name="Aikajakso"/></label>
          <div className='date-range'>
            <DateInput value={alkuAtom.get()} valueCallback={(value) => alkuAtom.set(value)} validityCallback={(valid) => !valid && alkuAtom.set(undefined)} />
            {' — '}
            <DateInput value={loppuAtom.get()} valueCallback={(value) => loppuAtom.set(value)} validityCallback={(valid) => !valid && loppuAtom.set(undefined)} />
          </div>
        </div>
        <div className='password'><Text name="Excel-tiedosto on suojattu salasanalla"/> <code>{password}</code></div>
        <button className='koski-button' disabled={submitEnabledP.not()} onClick={e => { e.preventDefault(); submitBus.push(); return false }}>{buttonTextP}</button>
      </form>
    </div>),
    title: 'Raportit'
  })
}

const Oppilaitos = ({oppilaitosAtom}) => {
  const selectableOrgTypes = ['OPPILAITOS']
  return (<label className='oppilaitos'><Text name="Oppilaitos"/>
    {
      oppilaitosAtom.map(oppilaitos => (
        <OrganisaatioPicker
          preselectSingleOption={true}
          selectedOrg={{ oid: oppilaitos && oppilaitos.oid, nimi: oppilaitos && oppilaitos.nimi && t(oppilaitos.nimi) }}
          onSelectionChanged={org => oppilaitosAtom.set({oid: org && org.oid, nimi: org && org.nimi})}
          shouldShowOrg={org => !org.organisaatiotyypit.some(tyyppi => tyyppi === 'TOIMIPISTE')}
          canSelectOrg={(org) => org.organisaatiotyypit.some(ot => selectableOrgTypes.includes(ot))}
          clearText="tyhjennä"
          noSelectionText="Valitse..."
        />
      ))
    }
  </label>)
}

const downloadExcel = (rapsa) => {
  let iframe = document.getElementById('raportti-iframe')
  if (iframe) {
    iframe.parentNode.removeChild(iframe)
  }
  iframe = document.createElement('iframe')
  iframe.id = 'raportti-iframe'
  iframe.style.display = 'none'
  document.body.appendChild(iframe)

  const resultBus = Bacon.Bus()
  let downloadTimer

  iframe.addEventListener('load', () => {
    var response
    try {
      response = { text: iframe.contentDocument.body.textContent, httpStatus: 400 }
    } catch (err) {
      response = { text: 'Tuntematon virhe', httpStatus: 500 }
    }
    window.clearInterval(downloadTimer)
    resultBus.error(response)
    resultBus.end()
  })

  const downloadToken = 'raportti' + new Date().getTime()
  const url = appendQueryParams('/koski/api/raportit/opiskelijavuositiedot', {...rapsa, downloadToken})
  iframe.src = url

  // detect when download has started by polling a cookie set by the backend.
  // based on https://stackoverflow.com/questions/1106377/detect-when-browser-receives-file-download
  let attempts = 120
  downloadTimer = window.setInterval(() => {
    if (document.cookie.indexOf('koskiDownloadToken=' + downloadToken) >= 0) {
      window.clearInterval(downloadTimer)
      resultBus.push(true)
      resultBus.end()
    } else {
      if (--attempts < 0) {
        window.clearInterval(downloadTimer)
        resultBus.error({text: 'Timeout', httpStatus: 400})
        resultBus.end()
      }
    }
  }, 1000)

  return resultBus
}
