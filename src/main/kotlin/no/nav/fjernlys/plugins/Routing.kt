package no.nav.fjernlys.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.fjernlys.appstatus.health
import no.nav.fjernlys.dbQueries.RiskAssessmentRepository
import no.nav.fjernlys.dbQueries.RiskMeasureRepository
import no.nav.fjernlys.dbQueries.RiskReportRepository
import no.nav.fjernlys.functions.AccessReports
import no.nav.fjernlys.functions.UpdateHistoryTables
import no.nav.fjernlys.functions.UpdateRiskLevelData
import java.util.*
import javax.sql.DataSource


fun Application.configureRouting(dataSource: DataSource) {


    var incomingData: IncomingData? = null

    fun test() {
        val currentMoment: Instant = Clock.System.now()
        val date: Instant = currentMoment
        val riskReportRepository = RiskReportRepository(dataSource)
        val riskAssessmentRepository = RiskAssessmentRepository(dataSource)
        val riskMeasureRepository = RiskMeasureRepository(dataSource)
        var serviceName = ""

        val reportId = UUID.randomUUID().toString()

        if (incomingData != null) {
            riskReportRepository.insertIntoRiskReport(
                reportId, incomingData!!.ownerData, incomingData!!.notOwnerData, incomingData!!.serviceData, date, date
            )
            serviceName = incomingData!!.serviceData
        }

        incomingData?.riskValues?.forEach { riskValue ->
            val riskAssessmentId = UUID.randomUUID().toString() // Generate or fetch a meaningful ID

            riskAssessmentRepository.insertIntoRiskAssessment(
                id = riskAssessmentId,
                reportId = reportId,
                probability = riskValue.probability,
                consequence = riskValue.consequence,
                dependent = riskValue.dependent,
                riskLevel = riskValue.riskLevel,
                category = riskValue.category,
                newProbability = riskValue.newProbability,
                newConsequence = riskValue.newConsequence
            )

            riskValue.measureValues?.forEach { measureValue ->
                val measureId = UUID.randomUUID().toString() // Generate or fetch a meaningful ID

                riskMeasureRepository.insertIntoRiskMeasure(
                    id = measureId,
                    riskAssessmentId = riskAssessmentId,
                    measureCategory = measureValue.category,
                    measureStatus = measureValue.status,

                    )
            }
        }
        UpdateHistoryTables(dataSource).updateHistoryReport(reportId)
        UpdateRiskLevelData(dataSource).updateRiskLevelByService(serviceName)
    }

    fun getAllReports(service: String): String {
        val riskReportRepository = RiskReportRepository(dataSource)
        val riskAssessmentRepository = RiskAssessmentRepository(dataSource)
        val riskMeasureRepository = RiskMeasureRepository(dataSource)

        val reportList = riskReportRepository.getAllRiskReportsByService(service)

        val result = reportList.map { report ->
            val riskAssessmentList = riskAssessmentRepository.getRiskAssessmentFromReportId(report.id)

            val riskValues = riskAssessmentList.map { assessment ->
                val riskMeasureList = riskMeasureRepository.getRiskMeasureFromAssessmentId(assessment.id)

                val measureValuesOut = riskMeasureList.map { measure ->
                    MeasureValueOut(
                        id = measure.id,
                        riskAssessmentId = measure.riskAssessmentId,
                        category = measure.category,
                        status = measure.status,
                    )
                }

                RiskValueOut(
                    id = assessment.id,
                    probability = assessment.probability.toDouble(),
                    consequence = assessment.consequence.toDouble(),
                    dependent = assessment.dependent,
                    riskLevel = assessment.riskLevel,
                    category = assessment.category,
                    measureValues = measureValuesOut,
                    newConsequence = assessment.newConsequence?.toDouble(),
                    newProbability = assessment.newProbability?.toDouble()
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

//---------- API call for submitting a form ----------
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
//---------- API call for editing or ???? ----------
        get("/get/reports") {
            try {
                val service = call.request.queryParameters["service"]
                val id = call.request.queryParameters["id"]

                if (service != null) {

                    val riskReport = RiskReportRepository(dataSource)
                    val reportList: List<RiskReportData> = riskReport.getAllRiskReportsByService(service)
                    val jsonReportList = Json.encodeToString(reportList)

                    call.respond(HttpStatusCode.OK, jsonReportList)
                } else if (id != null) {

                    val accessReports = AccessReports(dataSource)
                    val report = accessReports.getFullReportById(id)

                    call.respond(HttpStatusCode.OK, report)

                } else {
                    throw IllegalArgumentException("Missing parameter: service or id")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "An error occurred: ${e.message}")
            }
        }

//---------- API call for dashboard table ----------
        get("/get/all") {
            try {
                val getReportService = call.request.queryParameters["service"]
                    ?: throw IllegalArgumentException("Missing parameter: service")

                val allReports = AccessReports(dataSource).getAllCurrentReportsByService(getReportService)
                call.respond(HttpStatusCode.OK, allReports)

            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "An error occurred: ${e.message}")
            }
        }
//---------- API call for history table ----------
        get("/get/history") {
            try {
                val getReportId =
                    call.request.queryParameters["id"] ?: throw IllegalArgumentException("Missing parameter: id")

                val result = AccessReports(dataSource).getAllHistoryReports(getReportId)
                call.respond(HttpStatusCode.OK, result)


            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "An error occurred: ${e.message}")
            }
        }


//---------- API call for doughnut charts ----------
        get("/get/risk-levels") {
            try {
                val riskLevelServiceName = call.request.queryParameters["service"]
                    ?: throw IllegalArgumentException("Missing parameter: service")


                val riskLevels: RiskLevelData =
                    UpdateRiskLevelData(dataSource).getRiskLevelValuesByService(riskLevelServiceName)

                call.respond(HttpStatusCode.OK, riskLevels)


            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "An error occurred: ${e.message}")
            }
        }
    }
}








