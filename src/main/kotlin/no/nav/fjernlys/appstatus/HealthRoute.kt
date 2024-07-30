package no.nav.fjernlys.appstatus

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.health() {
    get("isalive") {
        call.respond("OK")
    }
    get("isready") {
        call.respond("OK")
    }
}
