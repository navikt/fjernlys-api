package no.nav.fjernlys.functions

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import no.nav.fjernlys.dbQueries.HistoryRiskAssessmentRepository
import no.nav.fjernlys.dbQueries.HistoryRiskMeasureRepository
import no.nav.fjernlys.dbQueries.HistoryRiskReportRepository
import java.util.*
import javax.sql.DataSource

class UpdateHistoryTables(val dataSource: DataSource) {

    fun updateHistoryReport(reportId: String) {

//  --------------------------- Risk Report ---------------------------
        val historyRiskRepository = HistoryRiskReportRepository(dataSource)
        val newId = UUID.randomUUID().toString()

        val findNewestReport = historyRiskRepository.getLastEditedRiskReport(reportId)
        println("newestReport: " + findNewestReport)

        historyRiskRepository.insertLastEntryIntoRiskReportHistory(findNewestReport, newId)

//  --------------------------- Risk Assessment ---------------------------
        val historyAssessment = HistoryRiskAssessmentRepository(dataSource)
        val findNewestAssessment = historyAssessment.getLastEditedRiskAssessment(reportId)

        println("newestAss: " + findNewestAssessment)

//  --------------------------- Measure ------------------------------------
        val historyMeasure = HistoryRiskMeasureRepository(dataSource)

        findNewestAssessment.forEach { assessment ->
            val newAssessmentId = UUID.randomUUID().toString()
            val findMeasure = historyMeasure.getLastEditedRiskMeasure(assessment.id)

            println("newestMeas" + findMeasure)

            historyAssessment.insertLastEntryIntoRiskAssessmentHistory(assessment, newId, newAssessmentId)

            findMeasure.forEach { measure ->
                historyMeasure.insertLastEntryIntoRiskMeasureHistory(measure, newAssessmentId)
            }
        }
    }
}