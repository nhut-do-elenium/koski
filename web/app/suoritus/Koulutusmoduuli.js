import {modelData, modelLookup, oneOfPrototypes} from '../editor/EditorModel'

export const isPaikallinen = (m) => m && m.value.classes.includes('paikallinenkoulutusmoduuli')
export const isKieliAine = (m) => m && m.value.classes.includes('kieliaine')
export const isUusi = (oppiaine) => {
  return !modelData(oppiaine, 'tunniste').koodiarvo
}
export const koulutusModuuliprototypes = (suoritus) => oneOfPrototypes(modelLookup(suoritus, 'koulutusmoduuli'))