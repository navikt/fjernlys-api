package no.nav.fjernlys


import io.ktor.server.application.*
import no.nav.fjernlys.plugins.configureRouting
import no.nav.fjernlys.plugins.configureSecurity

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureRouting()
}
