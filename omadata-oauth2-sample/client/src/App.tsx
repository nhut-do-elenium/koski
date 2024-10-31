import React from "react"
import "./App.css"

function App() {
  const [data, setData] = React.useState<object | null>(null)
  const [error, setError] = React.useState<string | null>(null)

  React.useEffect(() => {
    fetch("/api/front-page-dummy")
      .then((res) => {
        if (!res.ok) {
          throw new Error(`API result: ${res.status}:${res.statusText}`)
        }
        setError("SUCCESS")
        return res.json()
      })
      .then((data) => {
        setData(data)
      })
      .catch((error) => {
        console.error(error)
        setData({ result: "ERROR" })
        setError(error.message)
      })
  }, [])

  return (
    <div className="App">
      <header className="App-header">
        <p>OmaDataOAuth2 Sample app</p>
      </header>
      <p>
        <a href={"/api/openid-api-test"}>Test whole authorization code flow</a>
      </p>
      <p>
        <a href={"/api/openid-api-test/invalid-redirect-uri"}>
          Test authorization code flow with invalid redirect_uri
        </a>
      </p>
      <h2>Fetch resource using access token result:</h2>
      <p>{!data ? "Loading..." : JSON.stringify(data)}</p>
      <p>{!error ? "Checking errors..." : error}</p>
    </div>
  )
}

export default App
