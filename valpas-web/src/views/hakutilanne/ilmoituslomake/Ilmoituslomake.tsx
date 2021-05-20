import React, { useState } from "react"
import { ModalButtonGroup } from "../../../components/buttons/ModalButtonGroup"
import { RaisedButton } from "../../../components/buttons/RaisedButton"
import { Modal } from "../../../components/containers/Modal"
import { t, T } from "../../../i18n/i18n"
import { KuntailmoitusPohjatiedot } from "../../../state/apitypes/kuntailmoituspohjatiedot"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../../state/apitypes/oppija"
import { Organisaatio } from "../../../state/apitypes/organisaatiot"
import { Oid } from "../../../state/common"
import { IlmoitusForm } from "./IlmoitusForm"

export type IlmoituslomakeProps = {
  oppijat: OppijaHakutilanteillaSuppeatTiedot[]
  pohjatiedot: KuntailmoitusPohjatiedot
  tekijäorganisaatio: Organisaatio
  onClose: () => void
}

export const Ilmoituslomake = (props: IlmoituslomakeProps) => {
  const [submittedForms, setSubmittedForms] = useState<Oid[]>([])
  const allFormsSubmitted =
    submittedForms.length === props.pohjatiedot.oppijat.length

  return (
    <Modal title={t("ilmoituslomake__otsikko")} onClose={props.onClose}>
      <p>
        <T id="ilmoituslomake__ohje" />
      </p>

      {props.pohjatiedot.oppijat.map((oppija, index) => (
        <IlmoitusForm
          key={oppija.oppijaOid}
          formIndex={index}
          numberOfForms={props.pohjatiedot.oppijat.length}
          oppija={
            props.oppijat.find(
              (o) => o.oppija.henkilö.oid === oppija.oppijaOid
            )!!
          }
          pohjatiedot={oppija}
          kunnat={props.pohjatiedot.kunnat}
          maat={props.pohjatiedot.maat}
          tekijäorganisaatio={props.tekijäorganisaatio}
          kielet={props.pohjatiedot.yhteydenottokielet}
          onSubmit={() =>
            setSubmittedForms([...submittedForms, oppija.oppijaOid])
          }
        />
      ))}

      <ModalButtonGroup>
        {allFormsSubmitted ? (
          <RaisedButton hierarchy="primary" onClick={props.onClose}>
            <T id="ilmoituslomake__valmis" />
          </RaisedButton>
        ) : (
          <RaisedButton hierarchy="secondary" onClick={props.onClose}>
            <T id="ilmoituslomake__sulje" />
          </RaisedButton>
        )}
      </ModalButtonGroup>
    </Modal>
  )
}
