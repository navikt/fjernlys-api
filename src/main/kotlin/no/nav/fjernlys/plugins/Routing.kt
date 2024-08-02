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
import no.nav.fjernlys.dbQueries.*
import no.nav.fjernlys.functions.UpdateCategoryTable
import no.nav.fjernlys.functions.UpdateRiskLevelData
import no.nav.fjernlys.functions.UpdateRiskProbConsTable
import java.util.UUID
import java.util.*
import javax.sql.DataSource


fun Application.configureRouting(dataSource: DataSource) {


    routing {
        health()

//---------- API call for submitting a form ----------
        post("/submit") {
            try {
                val newReport = call.receive<IncomingData>()
                println("Received data: $newReport")
                call.respond(HttpStatusCode.OK, mapOf("message" to "Data received successfully"))
                AccessReports(dataSource).insertNewReport(newReport)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

//---------- API call for editing a form ----------
        post("/submit/edit") {
            try {
                val editedReport = call.receive<EditedReport>()
                val isAnyFieldMissing = editedReport.riskValues?.any {
                    it.measureValues.isNullOrEmpty() || it.measureValues.any { measureValue ->
                        measureValue.category.isBlank() || measureValue.status.isBlank()
                    } || it.newConsequence == null || it.newProbability == null
                } == true

                if (isAnyFieldMissing) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Missing required fields"))
                    return@post
                }


                call.respond(HttpStatusCode.OK, mapOf("message" to "Data received successfully"))
                println(editedReport)
                AccessReports(dataSource).updateReportEdit(editedReport)

//                Update risk_category_table when a new form is submitted
                UpdateCategoryTable(dataSource).updateAllCategoriesCount()

            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("message" to "An error occurred while processing your request")
                )
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


        get("/get/risk-category") {
            try {

//                Send by category
//                val riskCategoryName = call.request.queryParameters["category"]
//                    ?: throw IllegalArgumentException("Missing parameter: category")
//                val selectedData = RiskCategoryRepository(dataSource).getDependentByCategoryName(riskCategoryName)

//                Update new table
                UpdateCategoryTable(dataSource).updateAllCategoriesCount()

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

                // Henter Liste Prob Cons values fra risk_prob_cons_table
                updateRiskProbCons.updateRiskProbConsTable()
                val responseData = updateRiskProbCons.calculateRiskProbConsValues()
                call.respond(HttpStatusCode.OK, responseData)

            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "An error occurred: ${e.message}")
            }
        }

        get()
    }
}








