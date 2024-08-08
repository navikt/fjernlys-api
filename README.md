# Fjernlys-API

Applikasjonen er skrevet i **kotlin**, med **ktor** for API-kall, og en **postgres**-database

## Komme igang med Fjernlys-API

### Lokal postgres med docker

Vi bruker `colima` som container runtime.

1. Hent postgres image: `docker pull postgres`
2. Start containeren: `docker run -p 5432:5432 -e POSTGRES_PASSWORD=test postgres`

### Klargjøre testmiljø

JUnit-testene kjører på en lokal test-container. Derfor må du klargjøre dette miljøet først ved å skrive inn dette:
`colima start ; sudo rm -rf /var/run/docker.sock && sudo ln -s /Users/$(whoami)/.colima/docker.sock /var/run/docker.sock`

PS: Må gjøres hver gang maskinen blir restartet

### Filstruktur

#### Main

* Appstatus:
    * HealthRoute: IsAlive og IsReady
* dbQueries:
    * HistoryRiskXxxx: Klasser med SQL-spørringer til versjonshistorikktabeller
    * RiskXxx: Klasser med SQL-spørringer til vanlige rapport-tabeller
* functions:
    * AccessReports: Funksjoner for å legge til i, hente fra eller redigere rapporter, også historiske
    * UpdateHistoryTables: Funksjoner for oppdatering av historiske tabeller (versjonshistorikk)
    * UpdateRiskLevelData: Funksjoner for oppdatering av risikonivåtabellen
* plugins:
    * PostData: Inneholder alle dataklasser
    * Routing: Api routes
    * Security: Ikke aktiv per nå

#### Test

* Appstatus:
    * HealthRouteTest: IsAlive og IsReady test
* database:
    * ReceiveRiskreport: Tester for å se at inkommende data faktisk lagres, og at versjonshistorikk oppdateres
