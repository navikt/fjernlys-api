package no.nav.fjernlys.appstatus


import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class HealthRouteTest {
    @Test
    fun `isAlive Test`() = testApplication {
        client.get("/internal/isalive").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("OK", bodyAsText())
        }
    }
    @Test
    fun `isReady Test`() = testApplication {
        client.get("/internal/isready").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("OK", bodyAsText())
        }
    }
}
