package no.nav.fjernlys.plugins

import io.ktor.http.*
import no.nav.fjernlys.appstatus.health
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
      health()

        post("/submit") {
            val postData = call.receive<PostData>()
            println("Received data: ${postData.name}")
            call.respond(HttpStatusCode.OK, mapOf("message" to "Data received successfully"))
        }
    }
}
