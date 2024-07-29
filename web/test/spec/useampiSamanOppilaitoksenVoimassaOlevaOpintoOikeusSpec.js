describe('Useampi voimassa oleva opinto oikeus samassa oppilaitoksessa', function () {
  var addOppija = AddOppijaPage()
  var opinnot = OpinnotPage()
  var page = KoskiPage()

  before(
    Authentication().login(),
    resetFixtures,
    page.openPage,
    page.oppijaHaku.searchAndSelect('130320-899Y'),
    prepareForNewOppija('kalle', '230872-7258'),
    addOppija.enterValidDataMuuAmmatillinen(),
    addOppija.selectKoulutusmoduuli('Paikallinen koulutus'),
    addOppija.enterPaikallinenKoulutusmoduuliData(),
    addOppija.submitAndExpectSuccess(
      'Tyhjä, Tero (230872-7258)',
      'Varaston täyttäminen'
    )
  )

  describe('perusopetuksessa', function () {
    before(
      opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
      addOppija.selectOppilaitos('Aalto-yliopisto'),
      addOppija.selectOpiskeluoikeudenTyyppi('Perusopetus'),
      addOppija.submitModal,
      opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
      addOppija.selectOppilaitos('Aalto-yliopisto'),
      addOppija.selectOpiskeluoikeudenTyyppi('Perusopetus'),
      addOppija.submitModal
    )
    it('ei ole sallittu', function () {
      expect(page.getErrorMessage()).to.equal(
        'Opiskeluoikeutta ei voida lisätä, koska oppijalla on jo vastaava opiskeluoikeus.'
      )
    })
  })
  describe('ammatillisessa koulutuksessa', function () {
    before(
      opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
      addOppija.selectOppilaitos('Stadin ammatti- ja aikuisopisto'),
      addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'),
      addOppija.selectSuoritustyyppi('Ammatillinen tutkinto'),
      addOppija.selectTutkinto('Autoalan perustutkinto'),
      addOppija.selectSuoritustapa('Ammatillinen perustutkinto'),
      addOppija.selectAloituspäivä('1.1.2018'),
      addOppija.selectOpintojenRahoitus('Valtionosuusrahoitteinen koulutus'),
      addOppija.selectMaksuttomuus(0),
      addOppija.submitModal,
      opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
      addOppija.selectOppilaitos('Stadin ammatti- ja aikuisopisto'),
      addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'),
      addOppija.selectSuoritustyyppi('Ammatillinen tutkinto'),
      addOppija.selectTutkinto('Autoalan perustutkinto'),
      addOppija.selectSuoritustapa('Ammatillinen perustutkinto'),
      addOppija.selectAloituspäivä('1.1.2018'),
      addOppija.selectOpintojenRahoitus('Valtionosuusrahoitteinen koulutus'),
      addOppija.selectMaksuttomuus(0),
      addOppija.submitModal
    )
    it('ei ole sallittu', function () {
      expect(page.getErrorMessage()).to.equal(
        'Opiskeluoikeutta ei voida lisätä, koska oppijalla on jo vastaava opiskeluoikeus.'
      )
    })
    describe('kun suoritustyyppi eroaa', function () {
      before(
        opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
        addOppija.selectOppilaitos('Stadin ammatti- ja aikuisopisto'),
        addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'),
        addOppija.selectSuoritustyyppi('Ammatillinen tutkinto'),
        addOppija.selectSuoritustapa('Ammatillinen perustutkinto'),
        addOppija.selectTutkinto('Autoalan työnjohdon erikoisammattitutkinto'),
        addOppija.selectAloituspäivä('1.1.2018'),
        addOppija.selectOpintojenRahoitus('Valtionosuusrahoitteinen koulutus'),
        addOppija.selectMaksuttomuus(0),
        addOppija.submitModal
      )
      it('on sallittu', function () {
        expect(opinnot.opiskeluoikeudet.opiskeluoikeuksienMäärä()).to.equal(3)
      })
    })
  })
  describe('muussa ammatillisessa koulutuksessa', function () {
    before(
      opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
      addOppija.selectOppilaitos('Stadin ammatti- ja aikuisopisto'),
      addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'),
      addOppija.selectSuoritustyyppi('Muun ammatillisen koulutuksen suoritus'),
      addOppija.selectKoulutusmoduuli(
        'Ammatilliseen tehtävään valmistava koulutus'
      ),
      addOppija.enterAmmatilliseenTehtäväänvalmistava(
        'Ansio- ja liikennelentäjä'
      ),
      addOppija.selectOpintojenRahoitus('Valtionosuusrahoitteinen koulutus'),
      addOppija.selectMaksuttomuus(0),
      addOppija.submitModal,

      opinnot.opiskeluoikeudet.lisääOpiskeluoikeus,
      addOppija.selectOppilaitos('Stadin ammatti- ja aikuisopisto'),
      addOppija.selectOpiskeluoikeudenTyyppi('Ammatillinen koulutus'),
      addOppija.selectSuoritustyyppi('Muun ammatillisen koulutuksen suoritus'),
      addOppija.selectKoulutusmoduuli(
        'Ammatilliseen tehtävään valmistava koulutus'
      ),
      addOppija.enterAmmatilliseenTehtäväänvalmistava(
        'Ansio- ja liikennelentäjä'
      ),
      addOppija.selectOpintojenRahoitus('Valtionosuusrahoitteinen koulutus'),
      addOppija.selectMaksuttomuus(0),
      addOppija.submitModal
    )
    it('on sallittu', function () {
      expect(opinnot.opiskeluoikeudet.opiskeluoikeuksienMäärä()).to.equal(5)
    })
  })
})
