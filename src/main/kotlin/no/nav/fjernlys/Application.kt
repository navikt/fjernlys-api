package no.nav.fjernlys

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.fjernlys.plugins.configureRouting
import no.nav.fjernlys.plugins.configureSecurity
import io.ktor.server.plugins.cors.routing.CORS
import kotlinx.serialization.json.Json

//import io.ktor.features.StatusPages

val naisEnv = NaisEnvironment()
val dataSource = createDataSource(database = naisEnv.database)
fun main(args: Array<String>) {

    runMigration(dataSource = dataSource)
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    configureSecurity()
    install(ContentNegotiation) {
        json()
    }
//    install(StatusPages) {
//        exception<Throwable> { cause ->
//            call.respond(HttpStatusCode.InternalServerError, cause.localizedMessage)
//        }
//    }
    configureRouting(dataSource = dataSource)
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true
        anyHost()
    }
}
