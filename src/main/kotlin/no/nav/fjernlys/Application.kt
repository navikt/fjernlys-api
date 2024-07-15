package no.nav.fjernlys


import io.ktor.server.application.*
import no.nav.fjernlys.plugins.configureRouting
import no.nav.fjernlys.plugins.configureSecurity
import no.nav.fjernlys.dbQueries.DbQueryInsert

fun main(args: Array<String>) {

    val naisEnv = NaisEnvironment()

    val dataSource = createDataSource(database = naisEnv.database)
    runMigration(dataSource = dataSource)
    val Test = DbQueryInsert(dataSource)
    Test.insertInfo()
    Test.printAllRecords()

    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureRouting()
}
