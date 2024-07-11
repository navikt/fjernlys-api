package no.nav.fjernlys


import io.ktor.server.application.*
import no.nav.fjernlys.plugins.configureRouting
import no.nav.fjernlys.plugins.configureSecurity

fun main(args: Array<String>) {

    val naisEnv = NaisEnvironment()

    val dataSource = createDataSource(database = naisEnv.database)
    runMigration(dataSource = dataSource)
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureRouting()
}
