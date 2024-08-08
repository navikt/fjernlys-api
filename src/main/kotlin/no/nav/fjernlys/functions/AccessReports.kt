package no.nav.fjernlys.functions

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.fjernlys.dbQueries.*
import no.nav.fjernlys.plugins.*
import java.util.*
import javax.sql.DataSource

class AccessReports(val dataSource: DataSource) {
    private val riskReportRepository = RiskReportRepository(dataSource)
    private val riskAssessmentRepository = RiskAssessmentRepository(dataSource)
    private val riskMeasureRepository = RiskMeasureRepository(dataSource)
    private val HistoryRiskReportRepository = HistoryRiskReportRepository(dataSource)
    private val HistoryRiskAssessmentRepository = HistoryRiskAssessmentRepository(dataSource)
    private val HistoryRiskMeasureRepository = HistoryRiskMeasureRepository(dataSource)


    private fun getAllCurrentReports(reportList: List<RiskReportData>): String {
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
                    reportId = assessment.reportId,
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

    fun getAllHistoryReports(reportId: String): String {
        val historyReportList = HistoryRiskReportRepository.getAllHistoryReports(reportId)
        val result = historyReportList.map { historyReport ->
            val historyRiskAssessmentList =
                HistoryRiskAssessmentRepository.getHistoryRiskAssessmentFromHistoryReportId(historyReport.id)

            val historyRiskValues = historyRiskAssessmentList.map { historyAssessment ->
                val historyRiskMeasureList =
                    HistoryRiskMeasureRepository.getHistoryRiskMeasureFromAssessmentId(historyAssessment.id)

                val historyMeasureValuesOut = historyRiskMeasureList.map { historyMeasure ->
                    MeasureValueOut(
                        id = historyMeasure.id,
                        riskAssessmentId = historyMeasure.riskAssessmentId,
                        category = historyMeasure.category,
                        status = historyMeasure.status,
                    )
                }
                RiskValueOut(
                    id = historyAssessment.id,
                    reportId = historyAssessment.reportId,
                    probability = historyAssessment.probability.toDouble(),
                    consequence = historyAssessment.consequence.toDouble(),
                    dependent = historyAssessment.dependent,
                    riskLevel = historyAssessment.riskLevel,
                    category = historyAssessment.category,
                    measureValues = historyMeasureValuesOut,
                    newConsequence = historyAssessment.newConsequence?.toDouble(),
                    newProbability = historyAssessment.newProbability?.toDouble()
                )
            }
            OutgoingData(
                id = historyReport.id,
                isOwner = historyReport.isOwner,
                ownerIdent = historyReport.ownerIdent,
                serviceName = historyReport.serviceName,
                riskValues = historyRiskValues,
                reportCreated = historyReport.reportCreated,
                reportEdited = historyReport.reportEdited
            )
        }
        return Json.encodeToString(result)
    }


    fun getAllCurrentReportsByService(serviceName: String): String {
        val reportList = riskReportRepository.getAllRiskReportsByService(serviceName)
        return getAllCurrentReports(reportList)
    }

    fun getFullReportById(reportId: String): String {
        val report = riskReportRepository.getRiskReportFromId(reportId)!!
        val riskAssessmentList = riskAssessmentRepository.getRiskAssessmentFromReportId(reportId)

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
                reportId = assessment.reportId.toString(),
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

        val result = OutgoingData(
            id = report.id,
            isOwner = report.isOwner,
            ownerIdent = report.ownerIdent,
            serviceName = report.serviceName,
            riskValues = riskValues,
            reportCreated = report.reportCreated,
            reportEdited = report.reportEdited
        )
        return Json.encodeToString(result)
    }

    fun insertNewReport(incomingData: IncomingData) {
        val currentMoment: Instant = Clock.System.now()
        val date: Instant = currentMoment
        var serviceName = ""

        val reportId = UUID.randomUUID().toString()


        riskReportRepository.insertIntoRiskReport(
            reportId, incomingData.isOwner, incomingData.ownerIdent, incomingData.serviceName, date, date
        )
        serviceName = incomingData.serviceName

        incomingData.riskValues.forEach { riskValue ->
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
                val measureId = UUID.randomUUID().toString()

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

    fun updateReportEdit(editedReport: EditedReport) {
        val currentMoment: Instant = Clock.System.now()
        val editDate: Instant = currentMoment


        riskReportRepository.updateRiskReport(
            editedReport, editDate
        )

        val existingRisks = riskAssessmentRepository.getRiskAssessmentFromReportId(editedReport.id)
        val existingRisksId = existingRisks.map { it.id }.toSet()

        val editedRisks = editedReport.riskValues ?: emptyList()
        val editedRisksIds = editedRisks.map { it.id }.toSet()

        val risksToDelete = existingRisks.filter { it.id !in editedRisksIds }
        risksToDelete.forEach { risk ->
            riskAssessmentRepository.deleteRiskById(risk.id)
            riskMeasureRepository.deleteMeasuresByRiskId(risk.id)
        }

        editedReport.riskValues?.forEach { riskValue ->
            val assessmentId = riskValue.id ?: UUID.randomUUID().toString()
            if (riskValue.id == null) {
                RiskAssessmentRepository(dataSource).insertIntoRiskAssessment(
                    assessmentId,
                    riskValue.reportId,
                    riskValue.probability,
                    riskValue.consequence,
                    riskValue.dependent,
                    riskValue.riskLevel,
                    riskValue.category,
                    riskValue.newProbability,
                    riskValue.newConsequence
                )
                updateMeasureValues(riskValue.measureValues, assessmentId)
            } else {
                riskAssessmentRepository.updateRiskAssessment(riskValue)
                updateMeasureValues(riskValue.measureValues, assessmentId)
            }
        }
        UpdateHistoryTables(dataSource).updateHistoryReport(editedReport.id)
        UpdateRiskLevelData(dataSource).updateRiskLevelByService(editedReport.serviceName)
    }

    fun updateMeasureValues(measureValue: List<MeasureValueOut>?, assessmentId: String) {
        if (measureValue != null) {

            val existingMeasures = riskMeasureRepository.getRiskMeasureFromAssessmentId(assessmentId)
            val existingMeasuresIds = existingMeasures.map { it.id }.toSet()

            val editedMeasuresIds = measureValue.map { it.id }.toSet()

            val measuresToDelete = existingMeasures.filter { it.id !in editedMeasuresIds }
            measuresToDelete.forEach { measure ->
                measure.id?.let { riskMeasureRepository.deleteMeasuresByMeasureId(it) }
            }


            measureValue.forEach { measure ->
                val measureId = measure.id ?: UUID.randomUUID().toString()
                if (measure.id == null) {
                    RiskMeasureRepository(dataSource).insertIntoRiskMeasure(
                        measureId,
                        assessmentId,
                        measure.category,
                        measure.status
                    )
                } else {
                    riskMeasureRepository.updateRiskMeasure(measure)
                }
            }
        }
    }

}
