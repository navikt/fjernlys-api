package no.nav.fjernlys.plugins

import io.ktor.http.*
import no.nav.fjernlys.appstatus.health
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.fjernlys.dbQueries.*
import java.util.UUID
import javax.sql.DataSource
import no.nav.fjernlys.plugins.RiskReportData



fun Application.configureRouting(dataSource: DataSource) {


    var incomingData: IncomingData? = null

    fun test() {
        val currentMoment: Instant = Clock.System.now()
        val date: Instant = currentMoment
        val riskReportRepository = RiskReportRepository(dataSource)
        val riskAssessmentRepository = RiskAssessmentRepository(dataSource)
        val riskMeasureRepository = RiskMeasureRepository(dataSource)

        val reportId = UUID.randomUUID().toString()
        if (incomingData != null) {
            riskReportRepository.insertIntoRiskReport(
                reportId,
                incomingData!!.ownerData,
                incomingData!!.notOwnerData,
                incomingData!!.serviceData,
                date,
                date
            )
        }

        incomingData?.riskValues?.forEach { riskValue ->
            val riskAssessmentId = UUID.randomUUID().toString() // Generate or fetch a meaningful ID

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

            riskValue.measureValues?.forEach { measureValue ->
                val measureId = UUID.randomUUID().toString() // Generate or fetch a meaningful ID

                riskMeasureRepository.insertIntoRiskMeasure(
                    id = measureId,
                    risk_assessment_id = riskAssessmentId,
                    measure_category = measureValue.category,
                    measure_status = measureValue.status,

                    )
            }
        }
    }

    fun getAllReports(service: String): String {
        val riskReportRepository = RiskReportRepository(dataSource)
        val riskAssessmentRepository = RiskAssessmentRepository(dataSource)
        val riskMeasureRepository = RiskMeasureRepository(dataSource)

        val reportList = riskReportRepository.getRiskReportIdFromService(service)

        val result = reportList.map { report ->
            val riskAssessmentList = riskAssessmentRepository.getRiskMeasureFromAssessmentId(report.id)

            val riskValues = riskAssessmentList.map { assessment ->
                val riskMeasureList = riskMeasureRepository.getRiskMeasureFromAssessmentId(assessment.id)

                val measureValuesOut = riskMeasureList.map { measure ->
                    MeasureValueOut(
                        id = measure.id,
                        risk_assessment_id = measure.risk_assessment_id,
                        category = measure.measure_category,
                        status = measure.measure_status
                    )
                }

                RiskValueOut(
                    id = assessment.id,
                    probability = assessment.probability.toDouble(),
                    consequence = assessment.consequence.toDouble(),
                    dependent = assessment.dependent,
                    riskLevel = assessment.risk_level,
                    category = assessment.category,
                    measureValues = measureValuesOut,
                    newConsequence = assessment.new_consequence?.toDouble(),
                    newProbability = assessment.new_probability?.toDouble()
                )
            }

            OutgoingData(
                id = report.id,
                isOwner = report.isOwner,
                ownerIdent = report.ownerIdent,
                serviceName = report.serviceName,
                riskValues = riskValues,
                reportCreated = report.reportCreated,
                reportEdited = report.reportEdited
            )
        }

        return Json.encodeToString(result)
    }


    routing {
        health()

        post("/submit") {
            try {
                val postData = call.receive<IncomingData>()
                println("Received data: $postData")
                incomingData = postData
                call.respond(HttpStatusCode.OK, mapOf("message" to "Data received successfully"))
                test()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        get("/get/reports") {
            try {
                val getReportService = call.request.queryParameters["service"]
                    ?: throw IllegalArgumentException("Missing parameter: service")

                val riskReport = RiskReportRepository(dataSource)
                val testListe: List<RiskReportData> = riskReport.getRiskReportIdFromService(getReportService)

                val jsonTestListe = Json.encodeToString(testListe)

                call.respond(HttpStatusCode.OK, jsonTestListe)

                println(jsonTestListe)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "An error occurred: ${e.message}")
            }
        }

        get("/get/all") {
            try {
                val getReportService = call.request.queryParameters["service"]
                    ?: throw IllegalArgumentException("Missing parameter: service")

                val allReports = getAllReports(getReportService)


                call.respond(HttpStatusCode.OK, allReports)
                println(allReports)


            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "An error occurred: ${e.message}")
            }
        }
        get("/get/history") {
            try {
//  --------------------------- Risk Report ---------------------------
                val getReportId = call.request.queryParameters["id"]
                if (getReportId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Missing or malformed id")
                    return@get
                }
                val historyRiskRepository = HistoryRiskReportRepository(dataSource)
                val findNewestReport = historyRiskRepository.getLastEditedRiskReport(getReportId)

                if (findNewestReport != null) {
                    val insertRiskSuccess = historyRiskRepository.insertLastEntryIntoRiskReportHistory(findNewestReport)
                    if (insertRiskSuccess) {
                        call.respond(HttpStatusCode.OK, "Entry successfully inserted into history.")
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "Failed to insert entry into history.")
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, "Report not found")
                }

//  --------------------------- Risk Assessment ---------------------------
                val historyAssRepository = HistoryRiskAssessmentRepository(dataSource)
                val findNewestAssessment = historyAssRepository.getLastEditedRiskAssessment(getReportId)
                if (findNewestAssessment != null) {
                    val insertAssessmentSuccess = historyAssRepository.insertLastEntryIntoRiskAssessmentHistory(findNewestAssessment)
                    if (insertAssessmentSuccess) {
                        call.respond(HttpStatusCode.OK, "Entry successfully inserted into history.")
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "Failed to insert entry into history.")
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, "Report not found")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "An error occurred: ${e.message}")
            }
        }
    }
}









