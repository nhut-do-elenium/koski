import React from 'react'
import {modelData, modelTitle} from './EditorModel.js'
import {Editor} from './Editor.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {KoulutusmoduuliEditor} from './KoulutusmoduuliEditor.jsx'
import {PäivämääräväliEditor} from './PaivamaaravaliEditor.jsx'
import Text from '../Text.jsx'
import {ObjectEditor} from './ObjectEditor.jsx'

export const NäytönSuorituspaikkaEditor = React.createClass({
  render() {
    let {model} = this.props
    return <span>{modelTitle(model, 'kuvaus')}</span>
  }
})
NäytönSuorituspaikkaEditor.readOnly = true

export const NäytönArvioitsijaEditor = React.createClass({
  render() {
    let {model} = this.props
    return <span>{modelTitle(model, 'nimi')} { modelData(model, 'ntm') ? <span>{' ('}<Text name='näyttötutkintomestari'/>{')'}</span> : ''}</span>
  }
})
NäytönArvioitsijaEditor.readOnly = true
NäytönArvioitsijaEditor.canShowInline = () => true

const OppisopimusEditor = React.createClass({
  render() {
    let {model} = this.props
    return (<div className="oppisopimuksellinenjarjestamismuoto">
      <div>{ modelTitle(model, 'tunniste')}</div>
      <PropertiesEditor
        model = {model}
        propertyFilter={p => !['tunniste'].includes(p.key)}
      />
    </div>)
  }
})

const TutkinnonOsanLisätietoEditor = React.createClass({
  render() {
    let {model} = this.props
    return (<div className="ammatillisentutkinnonosanlisatieto">
      <Editor model={ model } path="tunniste"/>
      <div className="kuvaus">
        <Editor model={ model } path="kuvaus"/>
      </div>
    </div>)
  }
})

export const TyössäoppimisjaksoEditor = React.createClass({
  render() {
    let {model} = this.props
    return (
      <div className="tyossaoppimisjakso">
        <PäivämääräväliEditor model={model}/> { modelTitle(model, 'paikkakunta')}{', '}{ modelTitle(model, 'maa')}
        <PropertiesEditor
          model = {model}
          propertyFilter={p => !['alku', 'loppu', 'paikkakunta', 'maa'].includes(p.key)}
        />
      </div>
    )
  }
})
TyössäoppimisjaksoEditor.readOnly = true
TyössäoppimisjaksoEditor.validateModel = PäivämääräväliEditor.validateModel

const SisältäväOpiskeluoikeusEditor = ({model}) => {
  if (model.context.edit) return <ObjectEditor model={model}/>
  return <span><span className="id"><Editor model={model} path="id"/></span><span className="oppilaitos">{'('}<Editor model={model} path="oppilaitos"/>{')'}</span></span>
}

export const editorMapping = {
  'ammatillisentutkinnonosa': KoulutusmoduuliEditor,
  'naytonsuorituspaikka': NäytönSuorituspaikkaEditor,
  'naytonarvioitsija': NäytönArvioitsijaEditor,
  'naytonsuoritusaika': PäivämääräväliEditor,
  'tyossaoppimisjakso': TyössäoppimisjaksoEditor,
  'oppisopimuksellinenjarjestamismuoto': OppisopimusEditor,
  'ammatillisentutkinnonosanlisatieto': TutkinnonOsanLisätietoEditor,
  'sisaltavaopiskeluoikeus': SisältäväOpiskeluoikeusEditor
}