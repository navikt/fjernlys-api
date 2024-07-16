package no.nav.fjernlys.plugins

import io.ktor.http.*
import no.nav.fjernlys.appstatus.health
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun Application.configureRouting() {

    @Serializable
    data class MeasureValue(
        val category: String,
        val status: String,
        val started: Boolean
    )

    @Serializable
    data class RiskValue(
        val probability: Double,
        val consequence: Double,
        val dependent: Boolean,
        val riskLevel: String,
        val category: String,
        val measureValues: List<MeasureValue>?,
        val newConsequence: String?,
        val newProbability: String?
    )

    @Serializable
    data class IncomingData(
        val ownerData: Boolean,
        val notOwnerData: String,
        val serviceData: String,
        val riskValues: List<RiskValue>
    )


    routing {
        health()

        post("/submit") {
            val postData = call.receive<IncomingData>()
            println("Received data: $postData")
            println("TESTE: ${postData.serviceData}")
            println("Measure:  ${postData.riskValues[0].measureValues?.get(0)?.category}")
            call.respond(HttpStatusCode.OK, mapOf("message" to "Data received successfully"))
        }
    }
}
