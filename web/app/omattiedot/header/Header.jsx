import React from 'baret'
import Atom from 'bacon.atom'
import {modelItems, modelLookup} from '../../editor/EditorModel'
import {HeaderInfo} from './HeaderInfo'
import {withFeatureFlag} from '../../components/withFeatureFlag'
import {HeaderButtons} from './HeaderButtons'
import {HeaderName} from './HeaderName'
import {HeaderVirheraportointiSection} from './HeaderVirheraportointiSection'
import {HeaderSuoritusjakoSection} from './HeaderSuoritusjakoSection'

export const FormState = {
  VIRHERAPORTOINTI: 'virheraportointi',
  SUORITUSJAKO: 'suoritusjako',
  NONE: 'none'
}

const VirheraportointiFeature = withFeatureFlag(FEATURE.OMAT_TIEDOT.VIRHERAPORTOINTI, HeaderVirheraportointiSection)
const SuoritusjakoFeature = withFeatureFlag(FEATURE.OMAT_TIEDOT.SUORITUSJAKO, HeaderSuoritusjakoSection)

export const Header = ({oppija}) => {
  const showPalvelussaNäkyvätTiedot = Atom(false)
  const uiMode = Atom(FormState.NONE)

  const henkilö = modelLookup(oppija, 'henkilö')
  const opiskeluoikeudet = modelItems(oppija, 'opiskeluoikeudet')

  return (
    <header className='header'>
      <HeaderInfo showPalvelussaNäkyvätTiedotA={showPalvelussaNäkyvätTiedot}/>

      <div className='header__bottom-row'>
        <HeaderName henkilö={henkilö}/>
        <HeaderButtons uiModeA={uiMode} stateType={FormState}/>
        <VirheraportointiFeature uiModeA={uiMode} henkilö={henkilö} opiskeluoikeudet={opiskeluoikeudet}/>
        <SuoritusjakoFeature uiModeA={uiMode}/>
      </div>
    </header>
  )
}
