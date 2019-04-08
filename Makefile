env = cloud
cleandist = true
mvn_argline =
mvn_opts =

DOCKER_COMPOSE = docker-compose
DOCKER_COMPOSE_OPTS = --force-recreate --renew-anon-volumes

.PHONY: help
help:
	@echo ""
	@echo "make build	- Build the whole application, ready for running or testing"
	@echo "make front	- Build front end"
	@echo "make test	- Run unit tests"
	@echo "make run	- Run previously built application in local environment"
	@echo "make postgres	- Run local postgres server"
	@echo "make watch	- Watch for changes in webapp files"
	@echo "make clean	- Remove generated build data"
	@echo "make purge	- Remove all local data, including postgresql and elasticsearch databases"
	@echo "make deploy 	- Deploy to CSC's ePouta cloud"
	@echo "make dist version=<version> - Tag and deploy application to artifactory."
	@echo "make deploy env=<env> version=<version>	- Install deployed version to env."

.PHONY: logdir
logdir:
	@mkdir -p log

.PHONY: clean
clean:
	mvn clean
	rm -fr web/target

.PHONY: clean-db
clean-db:
	rm -fr elasticsearch/data
	rm -fr postgresql/data

.PHONY: purge
purge:
	mvn clean
	rm -fr web/target
	rm -fr elasticsearch/data
	rm -fr postgresql/data

### Building the application

.PHONY: build
build: logdir
	mvn compile
	# Built the whole application, ready for running or testing

.PHONY: front
front: logdir
	cd web && npm install

.PHONY: watch
watch:
	cd web && npm run watch

.PHONY: watch-prod
watch-prod:
	NODE_ENV="'production'" make watch

.PHONY: source-to-image
source-to-image: clean build
	echo "TODO" > target/webapp/buildversion.txt
	mvn package -P uberjar -DskipTests

### Running tests

.PHONY: test
test:
	mvn $(mvn_opts) -DargLine="$(mvn_argline)" test

.PHONY: browserstack
browserstack:
	mvn $(mvn_opts) -DargLine="$(mvn_argline)" test -Pbrowserstack

.PHONY: localizationtest
localizationtest:
	mvn $(mvn_opts) -DargLine="$(mvn_argline)" test -Plocalization

.PHONY: testresults
testresults:
	less -R +`grep -n "FAILED" target/surefire-reports/koski-tests.txt|head -1|cut -d ':' -f 1` target/surefire-reports/koski-tests.txt

.PHONY: js-unit-test
js-unit-test:
	cd web && npm run unit-test

.PHONY: js-unit-test-watch
js-unit-test-watch:
	cd web && npm run unit-test-watch

.PHONY: fronttest
fronttest:
	cd web && npm run test

.PHONY: screenshot
screenshot:
	ls -t web/target/screenshots|head -1|xargs -I{} open web/target/screenshots/{}

### Running application and database

.PHONY: run
run:
	mvn exec:java $(JAVA_OPTS) -Dexec.mainClass=fi.oph.koski.jettylauncher.JettyLauncher

docker-dbs:
	${DOCKER_COMPOSE} up ${DOCKER_COMPOSE_OPTS}

postgres:
	postgres --config_file=postgresql/postgresql.conf -D postgresql/data

.PHONY: postgres-clean
postgres-clean:
	rm postgresql/data/postmaster.pid 2> /dev/null||true

.PHONY: elastic
elastic:
	elasticsearch -E path.conf=elasticsearch -E path.data=elasticsearch/data -E path.logs=elasticsearch/log

### Code checks

.PHONY: eslint
eslint: front
	cd web && npm run lint

.PHONY: scalastyle
scalastyle:
	mvn verify -DskipTests -P scalastyle

.PHONY: lint
lint: eslint scalastyle

.PHONY: owasp
owasp:
	mvn dependency-check:check -P owasp

.PHONY: owaspresults
owaspresults:
	open target/dependency-check-report.html

.PHONY: snyk
snyk: # javascript dependency vulnerability check
	mvn generate-resources # to download correct node/npm version via frontend-maven-plugin
	./web/node/node web/node_modules/snyk/dist/cli/index.js test --dev . web

.PHONY: mvndeps
mvndeps:
	mvn dependency:tree|less

.PHONY: scala-console
scala-console:
	./scripts/mvn-scala-console.sh

.PHONY: reset-raportointikanta
reset-raportointikanta:
	./scripts/local-reset-raportointikanta.sh


### Dist and deploy

.PHONY: dist
dist: check-version
	cleandist=$(cleandist) ./scripts/dist.sh $(version)

.PHONY: deploy
deploy: check-version
	./scripts/deploy.sh $(env) $(version)

.PHONY: check-version
check-version:
ifndef version
	@echo "version is not set."
	@echo "Set version with version=<version>"
	@echo "Use version=local for locally installed version"
	exit 1
endif
