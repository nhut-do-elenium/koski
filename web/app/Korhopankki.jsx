import './polyfills/polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import './style/main.less'
import HetuLogin from './korhopankki/HetuLogin'
import Text from './i18n/Text'

const MockUsers = () =>
  (<table className='mock-users'>
    <thead>
      <tr><th><Text name='Hetu'/></th><th><Text name='Nimi'/></th></tr>
    </thead>
    <tbody>
    {window.mockUsers.map(user => (<tr key={user.hetu + '-' + user.nimi}>
      <td>{user.hetu}</td><td>{user.nimi}</td>
    </tr>))}
    </tbody>
  </table>)

ReactDOM.render((
  <div>
    <HetuLogin/>
    <MockUsers/>
  </div>
), document.getElementById('content'))
