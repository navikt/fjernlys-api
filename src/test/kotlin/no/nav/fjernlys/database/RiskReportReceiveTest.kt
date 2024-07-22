package no.nav.fjernlys.database

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.fjernlys.dbQueries.RiskAssessmentRepository
import no.nav.fjernlys.dbQueries.RiskMeasureRepository
import no.nav.fjernlys.dbQueries.RiskReportRepository
import no.nav.fjernlys.plugins.OutgoingData
import no.nav.fjernlys.plugins.RiskReportData
import no.nav.fjernlys.plugins.RiskValue
import no.nav.fjernlys.plugins.RiskValueOut
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testcontainers.TestContainersHelper
import java.util.*

class RiskReportReceiveTest {
    val dataSource = TestContainersHelper.postgresContainer.dataSource


    @Serializable
    data class MeasureValue(
        val category: String, val status: String, val started: Boolean
    )

    @Serializable
    data class RiskValue(
        val probability: Double,
        val consequence: Double,
        val dependent: Boolean,
        val riskLevel: String,
        val category: String,
        val measureValues: List<MeasureValue>,
        val newConsequence: Double,
        val newProbability: Double
    )

    @Serializable
    data class IncomingData(
        val ownerData: Boolean,
        val notOwnerData: String,
        val serviceData: String,
        val riskValues: List<no.nav.fjernlys.plugins.RiskValue>
    )

    val jsonString = """
        {
          "id": "1337ef71-5dd4-4933-a9a7-d6bd748465a7",
          "isOwner": true,
          "ownerIdent": "A111111",
          "serviceName": "Dagpenger",
          "riskValues": [
            {
              "id": "e0cba3d7-1870-4d53-b817-ac00e2b01e75",
              "probability": 2,
              "consequence": 2,
              "dependent": false,
              "riskLevel": "Lav",
              "category": "Personvern og informasjonssikkerhet",
              "measureValues": [
                {
                  "id": "f768d3d7-0fc7-433b-b6e9-f17d2ab473a7",
                  "riskAssessmentId": "e0cba3d7-1870-4d53-b817-ac00e2b01e75",
                  "category": "Redusere",
                  "status": "Videreført"
                },
                {
                  "id": "d5cad7ee-1987-4083-af88-a48b8329d2c0",
                  "riskAssessmentId": "e0cba3d7-1870-4d53-b817-ac00e2b01e75",
                  "category": "Godta",
                  "status": "Lukket"
                },
                {
                  "id": "925d507d-6088-45c5-8f81-7a59239ad979",
                  "riskAssessmentId": "e0cba3d7-1870-4d53-b817-ac00e2b01e75",
                  "category": "Eliminere",
                  "status": "Påbegynt"
                }
              ],
              "newConsequence": 5,
              "newProbability": 5
            }
          ],
          "reportCreated": "2024-07-22T13:21:54.583Z",
          "reportEdited": "2024-07-22T13:21:54.583Z"
        }
    """.trimIndent()

    val incomingData = Json.decodeFromString<OutgoingData>(jsonString)

    val currentMoment: Instant = Clock.System.now()
    val date: Instant = currentMoment


    @BeforeEach
    fun `insert data in all tables`() {
        val riskReportRepository = RiskReportRepository(dataSource)
        val riskAssessmentRepository = RiskAssessmentRepository(dataSource)
        val riskMeasureRepository = RiskMeasureRepository(dataSource)

        val reportId = UUID.randomUUID().toString()

        riskReportRepository.insertIntoRiskReport(
            incomingData.id,
            incomingData!!.isOwner,
            incomingData!!.ownerIdent,
            incomingData!!.serviceName,
            incomingData.reportCreated,
            incomingData.reportEdited
        )




        incomingData.riskValues?.forEach { riskValue ->


            riskAssessmentRepository.insertIntoRiskAssessment(
                id = riskValue.id,
                reportId = incomingData.id,
                probability = riskValue.probability,
                consequence = riskValue.consequence,
                dependent = riskValue.dependent,
                riskLevel = riskValue.riskLevel,
                category = riskValue.category,
                newProbability = riskValue.newProbability,
                newConsequence = riskValue.newConsequence
            )

            riskValue.measureValues?.forEach { measureValue ->

                measureValue.id?.let {
                    riskMeasureRepository.insertIntoRiskMeasure(
                        id = it,
                        riskAssessmentId = riskValue.id,
                        measureCategory = measureValue.category,
                        measureStatus = measureValue.status,

                        )
                }
            }
        }
    }


    @Test
    fun `verify RiskReport`() {
        using(sessionOf(dataSource)) { session ->
            val sql = """
                SELECT * FROM risk_report WHERE id = ?
            """.trimIndent()

            val query = queryOf(sql, "1337ef71-5dd4-4933-a9a7-d6bd748465a7").map { row ->
                row.string("id") to row.string("service_name")
            }.asSingle

            val result = session.run(query)
            Assertions.assertNotNull(result)
            Assertions.assertEquals("1337ef71-5dd4-4933-a9a7-d6bd748465a7", result?.first)
            Assertions.assertEquals("Dagpenger", result?.second)
        }
    }

    @Test
    fun `verify RiskAssessment`() {
        using(sessionOf(dataSource)) { session ->
            val sql = """
                SELECT * FROM risk_assessment WHERE id = ?
            """.trimIndent()

            val query = queryOf(sql, "e0cba3d7-1870-4d53-b817-ac00e2b01e75").map { row ->
                row.string("id") to row.double("probability")
            }.asSingle

            val result = session.run(query)
            Assertions.assertNotNull(result)
            Assertions.assertEquals("e0cba3d7-1870-4d53-b817-ac00e2b01e75", result?.first)
            Assertions.assertEquals(2.0, result?.second)
        }
    }

    @Test
    fun `verify RiskMeasure`() {
        using(sessionOf(dataSource)) { session ->
            val sql = """
                SELECT * FROM risk_measure WHERE id = ?
            """.trimIndent()

            val query = queryOf(sql, "f768d3d7-0fc7-433b-b6e9-f17d2ab473a7").map { row ->
                row.string("id") to row.string("measure_status")
            }.asSingle

            val result = session.run(query)
            Assertions.assertNotNull(result)
            Assertions.assertEquals("f768d3d7-0fc7-433b-b6e9-f17d2ab473a7", result?.first)
            Assertions.assertEquals("Videreført", result?.second)
        }
    }

    private fun mapRowToStatusoversikt(rad: Row): Int {
        return 1
    }

    @AfterEach
    fun clear() {
        clearDatabase()
    }

    private fun clearDatabase() {
        using(sessionOf(dataSource)) { session ->
            val tables = listOf(
                "risk_measure", "risk_assessment", "risk_report"
            )

            tables.forEach { table ->
                val sql = "DELETE FROM $table"
                val query = queryOf(sql).asUpdate
                session.run(query)
            }
        }
    }
}