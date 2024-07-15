package no.nav.fjernlys


import io.ktor.server.application.*
import no.nav.fjernlys.plugins.configureRouting
import no.nav.fjernlys.plugins.configureSecurity
import no.nav.fjernlys.dbQueries.RiskAssessmentRepository

fun main(args: Array<String>) {

    val naisEnv = NaisEnvironment()

    val dataSource = createDataSource(database = naisEnv.database)
    runMigration(dataSource = dataSource)
//    val register = RiskAssessmentRepository()
//    register.insertIntoRiskAssessment("123e4567-e89b-12d3-a456-426614174001","RAPPORT1", 2.5, 3.5, false, "Moderat", "Personvern", 1.5, 2.0)

    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureRouting()
}
