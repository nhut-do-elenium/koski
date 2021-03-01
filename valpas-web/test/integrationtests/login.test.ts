import {
  clickElement,
  defaultLogin,
  expectElementEventuallyVisible,
  expectElementNotVisible,
  expectElementVisible,
  getCurrentUrl,
  loginAs,
  reset,
} from "../integrationtests-env/browser"

describe("Login / Logout / kirjautuminen", () => {
  it("Kirjautumattomalle käyttäjälle näytetään kirjautumisruutu, jossa ei näy logout-painiketta", async () => {
    await reset("/virkailija")
    await expectElementVisible("article.page#login-app")
    await expectElementNotVisible(".localraamit__logoutbutton")
  })

  it("Kirjautumisen jälkeen käyttäjä näkee varsinaisen sovelluksen ja logout-painikkeen", async () => {
    await defaultLogin("/virkailija")
    await expectElementVisible("article.page#virkailija-app")
    await expectElementVisible(".localraamit__logoutbutton")
  })

  it("Kirjautuminen ei-Valpas-tunnuksilla näyttää virheen ja logout-painikkeen", async () => {
    await loginAs("/virkailija", "kalle", "kalle")
    await expectElementVisible("article.page#error-view")
    await expectElementVisible(".localraamit__logoutbutton")
  })

  it("Kirjautunut käyttäjä palaa uloskirjautumisen jälkeen login-sivulle", async () => {
    await defaultLogin("/virkailija")
    await expectElementVisible(".localraamit__logoutbutton")
    await clickElement(".localraamit__logoutbutton")
    await expectElementEventuallyVisible("article.page#login-app")
  })

  it("Ei-Valpas-tunnuksilla kirjautunut käyttäjä palaa uloskirjautumisen jälkeen login-sivulle", async () => {
    await loginAs("/virkailija", "kalle", "kalle")
    await expectElementVisible(".localraamit__logoutbutton")
    await clickElement(".localraamit__logoutbutton")
    await expectElementEventuallyVisible("article.page#login-app")
  })

  it("Käyttäjä on kirjautumisen jälkeen osoitteessa, jonne hän alunperin yritti", async () => {
    await loginAs(
      "/virkailija/oppijat/1.2.246.562.24.00000000001",
      "valpas-jkl-normaali",
      "valpas-jkl-normaali"
    )
    expect(await getCurrentUrl()).toEqual(
      "http://localhost:1234/valpas/virkailija/oppijat/1.2.246.562.24.00000000001"
    )
  })
})
