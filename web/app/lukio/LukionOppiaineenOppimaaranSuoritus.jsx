import React from 'baret'
import Bacon from 'baconjs'
import {modelErrorMessages} from '../editor/EditorModel'
import {LukionOppiaineEditor} from './LukionOppiaineEditor'
import {LukionOppiaineetTableHead, OmatTiedotLukionOppiaineetTableHead} from './fragments/LukionOppiaineetTableHead'
import {isMobileAtom} from '../util/isMobileAtom'
import {OmatTiedotLukionOppiaine} from './OmatTiedotLukionOppiaineet'
import {paikallisiaKursseja} from './LukionOppiaineetEditor'
import {FootnoteDescriptions} from '../components/footnote'

export const LukionOppiaineenOppimaaranSuoritus = ({model}) => (
  <section>
    <table className="suoritukset oppiaineet">
      <LukionOppiaineetTableHead />
      <tbody>
      <LukionOppiaineEditor oppiaine={model} allowOppiaineRemoval={false} />
      {
        modelErrorMessages(model).map((error, i) =>
          <tr key={'error-' + i} className='error'><td colSpan='42' className='error'>{error}</td></tr>
        )
      }
      </tbody>
    </table>
    {paikallisiaKursseja(model) && <FootnoteDescriptions data={[{title: 'Paikallinen kurssi tai oppiaine', hint: '*'}]}/>}
  </section>
)

export const OmatTiedotLukionOppiaineenOppimaaranSuoritus = ({model}) => (
  <section>
    <table className='omattiedot-suoritukset'>
      <OmatTiedotLukionOppiaineetTableHead />
      <tbody>
        {Bacon.combineWith(isMobileAtom, mobile =>
          <OmatTiedotLukionOppiaine oppiaine={model} isMobile={mobile}/>
        )}
      </tbody>
    </table>
    {paikallisiaKursseja(model) && <FootnoteDescriptions data={[{title: 'Paikallinen kurssi tai oppiaine', hint: '*'}]}/>}
  </section>
)
