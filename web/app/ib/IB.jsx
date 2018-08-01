import React from 'baret'
import * as R from 'ramda'
import Bacon from 'baconjs'
import {LukionOppiaineEditor} from '../lukio/LukionOppiaineEditor'
import {
  LukionOppiaineetTableHead,
  OmatTiedotLukionOppiaineetTableHead
} from '../lukio/fragments/LukionOppiaineetTableHead'
import {modelData, modelItems, modelLookup} from '../editor/EditorModel'
import {FootnoteDescriptions} from '../components/footnote'
import {UusiIBOppiaineDropdown} from './UusiIBOppiaineDropdown'
import {koodistoValues} from '../uusioppija/koodisto'
import {t} from '../i18n/i18n'
import {OmatTiedotLukionOppiaine} from '../lukio/OmatTiedotLukionOppiaineet'
import {isMobileAtom} from '../util/isMobileAtom'

const ArvosanaFootnote = {title: 'Ennustettu arvosana', hint: '*'}

const IBRyhmät = (oppiaineet, päätasonSuoritusModel, edit) => {
  const ryhmätOppiaineista = Bacon.constant(oppiaineet.map(oppiaine => modelData(oppiaine, 'koulutusmoduuli.ryhmä')))
  const ryhmätKoodistosta = edit ? koodistoValues('aineryhmaib') : Bacon.constant([])
  const ryhmätKaikki = Bacon.combineWith(ryhmätOppiaineista, ryhmätKoodistosta,
    (oppiaineista, koodistosta) => R.pipe(R.uniqBy(R.prop('koodiarvo')), R.sortBy(R.prop('koodiarvo')))(koodistosta.concat(oppiaineista)))

  const oppiaineetAineryhmittäin = R.groupBy(oppiaine => modelData(oppiaine, 'koulutusmoduuli.ryhmä').koodiarvo, oppiaineet)
  const aineryhmät = ryhmätKaikki.map(ryhmät => ryhmät.map(ryhmä => ({ryhmä, aineet: oppiaineetAineryhmittäin[ryhmä.koodiarvo]})))

  const yhteisetIbSuoritukset = ['theoryOfKnowledge', 'creativityActionService', 'extendedEssay'].map(k => modelLookup(päätasonSuoritusModel, k))
  const footnotes = R.any(s => modelData(s, 'arviointi.-1.predicted'), R.concat(oppiaineet, yhteisetIbSuoritukset))
    ? [ArvosanaFootnote]
    : []

  return {aineryhmät, footnotes}
}

export const IBTutkinnonOppiaineetEditor = ({suorituksetModel, additionalEditableKoulutusmoduuliProperties}) => {
  const {edit, suoritus: päätasonSuoritusModel} = suorituksetModel.context
  const oppiaineet = modelItems(suorituksetModel)

  const {aineryhmät, footnotes} = IBRyhmät(oppiaineet, päätasonSuoritusModel, edit)

  return (
    <div>
      <table className='suoritukset oppiaineet'>
        <LukionOppiaineetTableHead />
        <tbody>
        {
          aineryhmät.map(ryhmät => ryhmät.map(r => [
            <tr className='aineryhmä' key={r.ryhmä.koodiarvo}>
              <th colSpan='4'>{t(r.ryhmä.nimi)}</th>
            </tr>,
            r.aineet && r.aineet.map((oppiaine, oppiaineIndex) => {
              const footnote = modelData(oppiaine, 'arviointi.-1.predicted') && ArvosanaFootnote
              return (
                <LukionOppiaineEditor
                  key={oppiaineIndex}
                  oppiaine={oppiaine}
                  footnote={footnote}
                  additionalEditableKoulutusmoduuliProperties={additionalEditableKoulutusmoduuliProperties}
                />
              )
            }),
            <tr className='uusi-oppiaine' key={`uusi-oppiaine-${r.ryhmä.koodiarvo}`}>
              <td colSpan='4'>
                <UusiIBOppiaineDropdown
                  model={päätasonSuoritusModel}
                  aineryhmä={r.ryhmä}
                />
              </td>
            </tr>
          ]))
        }
        </tbody>
      </table>
      {!R.isEmpty(footnotes) && <FootnoteDescriptions data={footnotes}/>}
    </div>
  )
}

export const OmatTiedotIBTutkinnonOppiaineet = ({suorituksetModel}) => {
  const {suoritus: päätasonSuoritusModel} = suorituksetModel.context
  const oppiaineet = modelItems(suorituksetModel)

  const {aineryhmät, footnotes} = IBRyhmät(oppiaineet, päätasonSuoritusModel)

  return (
    <div className='ib-aineryhmat'>
      {
        aineryhmät.map(ryhmät => ryhmät.map(r => [
          <h4 key={r.ryhmä.koodiarvo} className='aineryhma-title'>
            {t(r.ryhmä.nimi)}
          </h4>,
          <table key={`suoritustable-${r.ryhmä.koodiarvo}`} className='omattiedot-suoritukset'>
            <OmatTiedotLukionOppiaineetTableHead />
            <tbody>
            {r.aineet && Bacon.combineWith(isMobileAtom, mobile =>
              r.aineet.map((oppiaine, oppiaineIndex) => {
                const footnote = modelData(oppiaine, 'arviointi.-1.predicted') && ArvosanaFootnote
                return (
                  <OmatTiedotLukionOppiaine
                    key={oppiaineIndex}
                    oppiaine={oppiaine}
                    isMobile={mobile}
                    footnote={footnote}
                  />
                )
              })
            )}
            </tbody>
          </table>
        ]))
      }
      {!R.isEmpty(footnotes) && <FootnoteDescriptions data={footnotes}/>}
    </div>
  )

}
