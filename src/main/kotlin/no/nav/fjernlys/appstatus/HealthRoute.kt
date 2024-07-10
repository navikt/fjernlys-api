package no.nav.fjernlys.appstatus

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.health() {
    get("internal/isalive") {
        call.respond("OK")
    }
    get("internal/isready") {
        call.respond("OK")
    }
}
