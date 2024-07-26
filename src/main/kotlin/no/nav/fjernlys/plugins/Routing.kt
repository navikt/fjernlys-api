package no.nav.fjernlys.plugins

import io.ktor.http.*
import no.nav.fjernlys.appstatus.health
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.fjernlys.dbQueries.*
import no.nav.fjernlys.functions.UpdateCategoryTable
import no.nav.fjernlys.functions.UpdateRiskLevelData
import no.nav.fjernlys.functions.UpdateRiskProbConsTable
import java.util.UUID
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
                reportId,
                incomingData!!.ownerData,
                incomingData!!.notOwnerData,
                incomingData!!.serviceData,
                date,
                date
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

        post("/submit") {
            try {
                val postData = call.receive<IncomingData>()
                println("Received data: $postData")
                incomingData = postData
                call.respond(HttpStatusCode.OK, mapOf("message" to "Data received successfully"))
                test()

//                Update risk_category_table when a new form is submitted
                UpdateCategoryTable(dataSource).updateAllCategoriesCount()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        get("/get/reports") {
            try {
                val getReportService = call.request.queryParameters["service"]
                    ?: throw IllegalArgumentException("Missing parameter: service")

                val riskReport = RiskReportRepository(dataSource)
                val testList: List<RiskReportData> =
                    riskReport.getAllRiskReportsByService(getReportService)

                val jsonTestList = Json.encodeToString(testList)

                call.respond(HttpStatusCode.OK, jsonTestList)

                println(jsonTestList)
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
                val newId = UUID.randomUUID().toString()

                val findNewestReport = historyRiskRepository.getLastEditedRiskReport(getReportId)

                if (findNewestReport != null) {
                    val insertRiskSuccess =
                        historyRiskRepository.insertLastEntryIntoRiskReportHistory(findNewestReport, newId)
                    if (insertRiskSuccess) {
                        call.respond(HttpStatusCode.OK, "Entry successfully inserted into history.")
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "Failed to insert entry into history.")
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, "Report not found")
                }

//  --------------------------- Risk Assessment ---------------------------
                val historyAssessment = HistoryRiskAssessmentRepository(dataSource)
                val findNewestAssessment = historyAssessment.getLastEditedRiskAssessment(getReportId)

//  --------------------------- Measure ------------------------------------
                val historyMeasure = HistoryRiskMeasureRepository(dataSource)

                findNewestAssessment.forEach { assessment ->
                    val newAssessmentId = UUID.randomUUID().toString()
                    val findMeasure = historyMeasure.getLastEditedRiskMeasure(assessment.id)

                    historyAssessment.insertLastEntryIntoRiskAssessmentHistory(assessment, newId, newAssessmentId)

                    // Move the check for measures inside the assessment loop
                    println("HALLLLLLLAAA" + findMeasure)
                    findMeasure.forEach { measure ->
                        historyMeasure.insertLastEntryIntoRiskMeasureHistory(measure, newAssessmentId)
                    }
                }


            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "An error occurred: ${e.message}")
            }
        }
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
        get("/get/risk-category") {
            try {

//                Send by category
//                val riskCategoryName = call.request.queryParameters["category"]
//                    ?: throw IllegalArgumentException("Missing parameter: category")
//                val selectedData = RiskCategoryRepository(dataSource).getDependentByCategoryName(riskCategoryName)

//                Send all
                val sendAll = RiskCategoryRepository(dataSource).getAll()

//                val category = RiskCategoryRepository(dataSource).getAll()



                call.respond(HttpStatusCode.OK, sendAll)

            } catch(e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "An error occurred: ${e.message}")

            }
        }
        get("/get/risk-probability-consequence") {
            try {
                val updateRiskProbCons = UpdateRiskProbConsTable(dataSource)
                val riskProbConsRepository = RiskProbConsRepository(dataSource)
                val halla = updateRiskProbCons.updateRiskProbConsTable()
                println(halla)

                val responseData = riskProbConsRepository.getAllFromProbConsTable()

                // Mocked data for response, ensuring it's in JSON format

                call.respond(HttpStatusCode.OK, responseData)

            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "An error occurred: ${e.message}")
            }
        }
    }
}








