import React from 'react'
import Bacon from 'baconjs'
import R from 'ramda'

export const opintoOikeusChange = Bacon.Bus()

const changeOpintoOikeus = (opintoOikeus, change) => opintoOikeusChange.push(R.merge(opintoOikeus, change))

export const OpintoOikeus = React.createClass({
  render() {
    let {opintoOikeus} = this.props
    return (
      <div className="opintooikeus">
        <h4>Opinto-oikeudet</h4>
        <span className="tutkinto">{opintoOikeus.tutkinto.nimi}</span> <span className="oppilaitos">{opintoOikeus.oppilaitosOrganisaatio.nimi}</span>
        { opintoOikeus.tutkinto.rakenne
          ?
            <div className="tutkinto-rakenne">
                <label>Suoritustapa
                    <select className="suoritustapa" value={opintoOikeus.suoritustapa} onChange={(event) => changeOpintoOikeus(opintoOikeus, {'suoritustapa': event.target.value || undefined })}>
                        {withEmptyValue(opintoOikeus.tutkinto.rakenne.suoritustavat).map(s => <option key={s.koodi} value={s.koodi}>{s.nimi}</option>)}
                    </select>
                </label>
                <label>Osaamisala
                    <select className="osaamisala" value={opintoOikeus.osaamisala} onChange={(event) => changeOpintoOikeus(opintoOikeus, {'osaamisala': event.target.value || undefined })}>
                        {withEmptyValue(opintoOikeus.tutkinto.rakenne.osaamisalat).map(o => <option key={o.koodi} value={o.koodi}>{o.nimi}</option>)}
                    </select>
                </label>
              { opintoOikeus.suoritustapa
                ? <Rakenneosa
                    selectedTutkinnonOsa={this.state.selectedTutkinnonOsa}
                    tutkinnonOsaBus={this.state.tutkinnonOsaBus}
                    rakenneosa={opintoOikeus.tutkinto.rakenne.suoritustavat.find(x => x.koodi == opintoOikeus.suoritustapa).rakenne}
                    opintoOikeus={opintoOikeus}
                  />
                : null
              }
            </div>
          : null
        }
      </div>
    )
  },
  getInitialState() {
      return {
        tutkinnonOsaBus: Bacon.Bus(),
        selectedTutkinnonOsa: undefined
      }
  },
  componentDidMount() {
      this.state.tutkinnonOsaBus
          .onValue(tutkinnonOsa => this.setState({selectedTutkinnonOsa: tutkinnonOsa}))
  }
})

const withEmptyValue = (xs) => [{ koodi: '', nimi: 'Valitse...'}].concat(xs)

const Rakenneosa = React.createClass({
  render() {
    let { rakenneosa, opintoOikeus, selectedTutkinnonOsa, tutkinnonOsaBus } = this.props
    return rakenneosa.osat
      ? <RakenneModuuli key={rakenneosa.nimi} opintoOikeus={opintoOikeus} rakenneosa={rakenneosa} selectedTutkinnonOsa={selectedTutkinnonOsa} tutkinnonOsaBus={tutkinnonOsaBus}/>
      : <TutkinnonOsa key={rakenneosa.nimi} opintoOikeus={opintoOikeus} tutkinnonOsa={rakenneosa} selectedTutkinnonOsa={selectedTutkinnonOsa} tutkinnonOsaBus={tutkinnonOsaBus}/>
  }
})

const RakenneModuuli = React.createClass({
  render() {
    const { rakenneosa, opintoOikeus, selectedTutkinnonOsa, tutkinnonOsaBus } = this.props
    return (
      <div className="rakenne-moduuli">
        <span className="name">{rakenneosa.nimi}</span>
        <ul className="osat">
          { rakenneosa.osat
            .filter(osa => { return !osa.osaamisalaKoodi || osa.osaamisalaKoodi == opintoOikeus.osaamisala})
            .map((osa, i) => <li key={i}>
              <Rakenneosa
                rakenneosa={osa}
                opintoOikeus={opintoOikeus}
                selectedTutkinnonOsa={selectedTutkinnonOsa}
                tutkinnonOsaBus={tutkinnonOsaBus}/>
            </li>)
          }
        </ul>
      </div>
    )
  }
})

const TutkinnonOsa = React.createClass({
  render() {
    const {tutkinnonOsa, opintoOikeus, selectedTutkinnonOsa, tutkinnonOsaBus} = this.props
    const selected = selectedTutkinnonOsa && tutkinnonOsa.nimi === selectedTutkinnonOsa.nimi
    const arviointiAsteikko = R.find(asteikko => R.equals(asteikko.koodisto, tutkinnonOsa.arviointiAsteikko))(opintoOikeus.tutkinto.rakenne.arviointiAsteikot)
    const arvosanat = arviointiAsteikko ? arviointiAsteikko.arvosanat.map((arvosana, i) => <li key={arvosana.id}>{arvosana.nimi}</li>) : undefined
    return (
      <div className={ selected ? 'tutkinnon-osa selected' : 'tutkinnon-osa'} onClick={() => tutkinnonOsaBus.push(tutkinnonOsa)}>
        <span className="name">{tutkinnonOsa.nimi}</span>
        { selected && arvosanat ?
          <div className="arvostelu">
            <ul className="arvosanat">
              {arvosanat}
            </ul>
            <button className="button blue">Tallenna arvio</button>
          </div> : null
        }
      </div>
    )
  }
})