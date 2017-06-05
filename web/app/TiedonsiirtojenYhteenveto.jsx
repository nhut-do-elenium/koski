import React from 'react'
import Http from './http'
import {tiedonsiirrotContentP} from './Tiedonsiirrot.jsx'
import Link from './Link.jsx'
import SortingTableHeader from './SortingTableHeader.jsx'
import {t} from './i18n'
import Text from './Text.jsx'
import {ISO2FinnishDateTime} from './date'

const yhteenvetoP = (queryString) => Http.cachedGet('/koski/api/tiedonsiirrot/yhteenveto' + queryString, { willHandleErrors: true})

export const tiedonsiirtojenYhteenvetoContentP = (queryString) => tiedonsiirrotContentP('/koski/tiedonsiirrot/yhteenveto', yhteenvetoP(queryString).map((rivit) =>
  ({
    content: (<div className="tiedonsiirto-yhteenveto">
                <Text name="Yhteenveto siirretyistä oppilaitoksittain"/>
                <table>
                  <thead>
                  <tr>
                    <SortingTableHeader field="oppilaitos" titleKey='Oppilaitos' default="asc"/>
                    <SortingTableHeader field="aika" titleKey='Viimeisin siirto'/>
                    <th className="siirretyt"><Text name="Siirrettyjen lukumäärä"/></th>
                    <th className="virheelliset"><Text name="Virheellisten lukumäärä"/></th>
                    <th className="opiskeluoikeudet"><Text name="Onnistuneiden lukumäärä"/></th>
                    <th className="lähdejärjestelmä"><Text name="Lähdejärjestelmä"/></th>
                    <th className="lähdejärjestelmä"><Text name="Käyttäjä"/></th>
                  </tr>
                  </thead>
                  <tbody>
                 { rivit.map((rivi, i) => {
                     return (<tr key={i}>
                       <td className="oppilaitos"><Link href={'/koski/tiedonsiirrot?oppilaitos=' + rivi.oppilaitos.oid}>{t(rivi.oppilaitos.nimi)}</Link></td>
                       <td className="aika">{ISO2FinnishDateTime(rivi.viimeisin)}</td>
                       <td className="siirretyt">{rivi.siirretyt}</td>
                       <td className="virheelliset">{ rivi.virheelliset ? <Link href={'/koski/tiedonsiirrot/virheet?oppilaitos=' + rivi.oppilaitos.oid}>{rivi.virheelliset}</Link> : '0'}</td>
                       <td className="opiskeluoikeudet">{rivi.onnistuneet}</td>
                       <td className="lähdejärjestelmä">{rivi.lähdejärjestelmä ? t(rivi.lähdejärjestelmä.nimi) : ''}</td>
                       <td className="käyttäjä">{rivi.käyttäjä.käyttäjätunnus || rivi.käyttäjä.oid}</td>
                     </tr>)
                    })
                  }
                  </tbody>
                </table>
              </div>),
      title: 'Tiedonsiirrot'
  })
))