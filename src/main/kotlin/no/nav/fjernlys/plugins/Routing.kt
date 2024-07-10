package no.nav.fjernlys.plugins

import no.nav.fjernlys.appstatus.health
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
      health()
    }
}
