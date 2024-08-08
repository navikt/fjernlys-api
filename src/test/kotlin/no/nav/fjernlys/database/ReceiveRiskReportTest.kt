package no.nav.fjernlys.database

import kotlinx.datetime.*
import kotlinx.serialization.json.Json
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.fjernlys.dbQueries.*
import no.nav.fjernlys.functions.AccessReports
import no.nav.fjernlys.plugins.EditedReport
import no.nav.fjernlys.plugins.IncomingData
import no.nav.fjernlys.plugins.RiskReportData
import org.junit.jupiter.api.*
import testcontainers.TestContainersHelper


class ReceiveRiskReportTest {
    val dataSource = TestContainersHelper.postgresContainer.dataSource

    companion object {
        var reportId = ""
        val jsonString = """
        {
          "isOwner": true,
          "ownerIdent": "A111111",
          "serviceName": "Dagpenger",
          "riskValues": [
            {
              "probability": 4.0,
              "consequence": 4.5,
              "dependent": false,
              "riskLevel": "Lav",
              "category": "Personvern og informasjonssikkerhet",
              "measureValues": [
                {
                  "category": "Redusere",
                  "status": "Videreført"
                },
                {
                  "category": "Godta",
                  "status": "Lukket"
                },
                {
                  "category": "Eliminere",
                  "status": "Påbegynt"
                }
              ],
              "newConsequence": 2.0,
              "newProbability": 2.5
            }
          ]
        }
    """.trimIndent()


        @JvmStatic
        @BeforeAll
        fun setUp(): Unit {
            // Initial setup before any test runs
            initialDataInsertion(jsonString)
        }

        @JvmStatic
        @AfterAll
        fun clear(): Unit {
            clearDatabase()
        }
    }


    @Test
    fun `verify RiskReport`() {
        val dataSource = TestContainersHelper.postgresContainer.dataSource
        val result: List<RiskReportData> = RiskReportRepository(dataSource).getAllRiskReports()
        Assertions.assertNotNull(result)
        println(result)
        Assertions.assertEquals(true, result[0].isOwner)
        Assertions.assertEquals("A111111", result[0].ownerIdent)
        Assertions.assertEquals("Dagpenger", result[0].serviceName)

        //Check for approx date registration
        val zone = TimeZone.of("Europe/Berlin")
        val expectedDate = Clock.System.now().toLocalDateTime(zone).run {
            LocalDateTime(year, monthNumber, dayOfMonth, hour, minute, second)
        }.toInstant(zone)

        val reportCreatedDate = result[0].reportCreated.toLocalDateTime(zone).run {
            LocalDateTime(year, monthNumber, dayOfMonth, hour, minute, second)
        }.toInstant(zone)
        Assertions.assertTrue(reportCreatedDate.epochSeconds <= expectedDate.epochSeconds + 1)
        Assertions.assertTrue(reportCreatedDate.epochSeconds >= expectedDate.epochSeconds - 1)

        val reportEditedDate = result[0].reportCreated.toLocalDateTime(zone).run {
            LocalDateTime(year, monthNumber, dayOfMonth, hour, minute, second)
        }.toInstant(zone)
        Assertions.assertTrue(reportEditedDate.epochSeconds <= expectedDate.epochSeconds + 1)
        Assertions.assertTrue(reportEditedDate.epochSeconds >= expectedDate.epochSeconds - 1)


        reportId = result[0].id


        `verify RiskAssessment`(reportId)


    }


    fun `verify RiskAssessment`(reportId: String) {

        val result = RiskAssessmentRepository(dataSource).getRiskAssessmentFromReportId(reportId)
        val assessmentId = result[0].id

        Assertions.assertNotNull(result)
        Assertions.assertEquals(1, result.size)
        Assertions.assertEquals(4.0, result[0].probability)
        Assertions.assertEquals(4.5, result[0].consequence)
        Assertions.assertFalse(result[0].dependent)
        Assertions.assertEquals("Lav", result[0].riskLevel)
        Assertions.assertEquals("Personvern og informasjonssikkerhet", result[0].category)
        Assertions.assertEquals(2.5, result[0].newProbability)
        Assertions.assertEquals(2.0, result[0].newConsequence)

        `verify RiskMeasure`(assessmentId)
    }


    fun `verify RiskMeasure`(assessmentId: String) {

        val result = RiskMeasureRepository(dataSource).getRiskMeasureFromAssessmentId(assessmentId)

        Assertions.assertNotNull(result)
        Assertions.assertEquals(3, result.size)
        Assertions.assertEquals("Redusere", result[0].category)
        Assertions.assertEquals("Videreført", result[0].status)
        Assertions.assertEquals("Godta", result[1].category)
        Assertions.assertEquals("Lukket", result[1].status)
        Assertions.assertEquals("Eliminere", result[2].category)
        Assertions.assertEquals("Påbegynt", result[2].status)
    }


    @Test
    fun `verify HistoryReportInsert`() {
        val dataSource = TestContainersHelper.postgresContainer.dataSource
        val resultString = AccessReports(dataSource).getAllHistoryReports(reportId)

        val result: List<EditedReport> = Json.decodeFromString(resultString)

        Assertions.assertNotNull(result, "Should not be null")
        Assertions.assertFalse(reportId == result[0].id)
        Assertions.assertEquals(true, result[0].isOwner)
        Assertions.assertEquals("A111111", result[0].ownerIdent)
        Assertions.assertEquals("Dagpenger", result[0].serviceName)

        //Check for approx date registration
        val zone = TimeZone.of("Europe/Berlin")
        val expectedDate = Clock.System.now().toLocalDateTime(zone).run {
            LocalDateTime(year, monthNumber, dayOfMonth, hour, minute, second)
        }.toInstant(zone)

        val reportCreatedDate = result[0].reportCreated.toLocalDateTime(zone).run {
            LocalDateTime(year, monthNumber, dayOfMonth, hour, minute, second)
        }.toInstant(zone)
        Assertions.assertTrue(reportCreatedDate.epochSeconds <= expectedDate.epochSeconds + 1)
        Assertions.assertTrue(reportCreatedDate.epochSeconds >= expectedDate.epochSeconds - 1)

        val reportEditedDate = result[0].reportCreated.toLocalDateTime(zone).run {
            LocalDateTime(year, monthNumber, dayOfMonth, hour, minute, second)
        }.toInstant(zone)
        Assertions.assertTrue(reportEditedDate.epochSeconds <= expectedDate.epochSeconds + 1)
        Assertions.assertTrue(reportEditedDate.epochSeconds >= expectedDate.epochSeconds - 1)

        `verify historyRiskAssessment`(result[0].id)

    }

    fun `verify historyRiskAssessment`(reportId: String) {

        val result = HistoryRiskAssessmentRepository(dataSource).getHistoryRiskAssessmentFromHistoryReportId(reportId)
        val assessmentId = result[0].id
        println("histAss: " + assessmentId)

        Assertions.assertNotNull(result)
        Assertions.assertEquals(1, result.size)
        Assertions.assertEquals(4.0, result[0].probability)
        Assertions.assertEquals(4.5, result[0].consequence)
        Assertions.assertFalse(result[0].dependent)
        Assertions.assertEquals("Lav", result[0].riskLevel)
        Assertions.assertEquals("Personvern og informasjonssikkerhet", result[0].category)
        Assertions.assertEquals(2.5, result[0].newProbability)
        Assertions.assertEquals(2.0, result[0].newConsequence)

        `verify historyRiskMeasure`(result[0].id)
    }


    fun `verify historyRiskMeasure`(assessmentId: String) {

        val result = HistoryRiskMeasureRepository(dataSource).getHistoryRiskMeasureFromAssessmentId(assessmentId)

        Assertions.assertNotNull(result)
        Assertions.assertEquals(3, result.size)
        Assertions.assertEquals("Redusere", result[0].category)
        Assertions.assertEquals("Videreført", result[0].status)
        Assertions.assertEquals("Godta", result[1].category)
        Assertions.assertEquals("Lukket", result[1].status)
        Assertions.assertEquals("Eliminere", result[2].category)
        Assertions.assertEquals("Påbegynt", result[2].status)
    }


}

val test = ReceiveRiskReportTest()

fun initialDataInsertion(jsonString: String) {
    val report = Json.decodeFromString<IncomingData>(jsonString)
    AccessReports(test.dataSource).insertNewReport(report)
}

private fun clearDatabase() {
    using(sessionOf(test.dataSource)) { session ->
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


