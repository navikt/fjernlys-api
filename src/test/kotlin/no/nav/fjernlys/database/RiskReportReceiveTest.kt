package no.nav.fjernlys.database

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.fjernlys.dbQueries.RiskAssessmentRepository
import no.nav.fjernlys.dbQueries.RiskMeasureRepository
import no.nav.fjernlys.dbQueries.RiskReportRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testcontainers.TestContainersHelper
import java.time.LocalDateTime

class RiskReportReceiveTest {
    val dataSource = TestContainersHelper.postgresContainer.dataSource


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
        val measureValues: List<MeasureValue>,
        val newConsequence: Double,
        val newProbability: Double
    )

    @Serializable
    data class IncomingData(
        val ownerData: Boolean,
        val notOwnerData: String,
        val serviceData: String,
        val riskValues: List<RiskValue>
    )

    val jsonString = """
        {
            "ownerData": false,
            "notOwnerData": "A111111",
            "serviceData": "Alderpensjon",
            "riskValues": [
                {
                    "probability": 2.5,
                    "consequence": 2.5,
                    "dependent": false,
                    "riskLevel": "Moderat",
                    "category": "Helse, miljø og sikkerhet",
                    "measureValues": [
                        {
                            "category": "Redusere",
                            "status": "Ikke påbegynt",
                            "started": false
                        }
                    ],
                    "newConsequence": 1,
                    "newProbability": 1.5
                },
                {
                    "probability": 3.5,
                    "consequence": 3.5,
                    "dependent": false,
                    "riskLevel": "Moderat",
                    "category": "Personvern og informasjonssikkerhet",
                    "measureValues": [
                        {
                            "category": "Redusere",
                            "status": "Påbegynt",
                            "started": false
                        }
                    ],
                    "newConsequence": 2.5,
                    "newProbability": 2
                }
            ]
        }
    """.trimIndent()

    val incomingData = Json.decodeFromString<IncomingData>(jsonString)

    val sortedRiskValues = incomingData.riskValues
    val date = LocalDateTime.now()


    @BeforeEach
    fun `insert data in all tables`() {
        val riskReportRepository = RiskReportRepository(dataSource)
        val riskAssessmentRepository = RiskAssessmentRepository(dataSource)
        val riskMeasureRepository = RiskMeasureRepository(dataSource)

        val reportId = "id1" // Generate or fetch a meaningful ID
        riskReportRepository.insertIntoRiskReport(
            reportId,
            incomingData.ownerData,
            incomingData.notOwnerData,
            incomingData.serviceData,
            date,
            date
        )

        incomingData.riskValues.forEachIndexed { index, riskValue ->
            val riskAssessmentId = "risk${index + 1}" // Generate or fetch a meaningful ID

            riskAssessmentRepository.insertIntoRiskAssessment(
                id = riskAssessmentId,
                report_id = reportId,
                probability = riskValue.probability,
                consequence = riskValue.consequence,
                dependent = riskValue.dependent,
                risk_level = riskValue.riskLevel,
                category = riskValue.category,
                new_probability = riskValue.newProbability,
                new_consequence = riskValue.newConsequence
            )

            riskValue.measureValues.forEachIndexed { measureIndex, measureValue ->
                val measureId = "measure${index + 1}_${measureIndex + 1}" // Generate or fetch a meaningful ID

                riskMeasureRepository.insertIntoRiskMeasure(
                    id = measureId,
                    risk_assessment_id = riskAssessmentId,
                    measure_category = measureValue.category,
                    measure_status = measureValue.status,
                    measure_started = measureValue.started
                )
            }
        }
    }


    @Test
    fun `verify RiskReport`() {
        using(sessionOf(dataSource)) { session ->
            val sql = """
                SELECT * FROM risk_report WHERE id = ?
            """.trimIndent()

            val query = queryOf(sql, "id1").map { row ->
                row.string("id") to row.string("service_name")
            }.asSingle

            val result = session.run(query)
            Assertions.assertNotNull(result)
            Assertions.assertEquals("id1", result?.first)
            Assertions.assertEquals("Alderpensjon", result?.second)
        }
    }

    @Test
    fun `verify RiskAssessment`() {
        using(sessionOf(dataSource)) { session ->
            val sql = """
                SELECT * FROM risk_assessment WHERE id = ?
            """.trimIndent()

            val query = queryOf(sql, "risk1").map { row ->
                row.string("id") to row.double("probability")
            }.asSingle

            val result = session.run(query)
            Assertions.assertNotNull(result)
            Assertions.assertEquals("risk1", result?.first)
            Assertions.assertEquals(2.5, result?.second)
        }
    }

    @Test
    fun `verify RiskMeasure`() {
        using(sessionOf(dataSource)) { session ->
            val sql = """
                SELECT * FROM risk_measure WHERE id = ?
            """.trimIndent()

            val query = queryOf(sql, "measure1_1").map { row ->
                row.string("id") to row.string("measure_status")
            }.asSingle

            val result = session.run(query)
            Assertions.assertNotNull(result)
            Assertions.assertEquals("measure1_1", result?.first)
            Assertions.assertEquals("Ikke påbegynt", result?.second)
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
                "risk_measure",
                "risk_assessment",
                "risk_report"
            )

            tables.forEach { table ->
                val sql = "DELETE FROM $table"
                val query = queryOf(sql).asUpdate
                session.run(query)
            }
        }
    }
}