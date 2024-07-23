package no.nav.fjernlys.functions

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.fjernlys.dbQueries.*
import no.nav.fjernlys.plugins.MeasureValueOut
import no.nav.fjernlys.plugins.OutgoingData
import no.nav.fjernlys.plugins.RiskReportData
import no.nav.fjernlys.plugins.RiskValueOut
import javax.sql.DataSource

class AccessReports(dataSource: DataSource) {
    private val riskReportRepository = RiskReportRepository(no.nav.fjernlys.dataSource)
    private val riskAssessmentRepository = RiskAssessmentRepository(no.nav.fjernlys.dataSource)
    private val riskMeasureRepository = RiskMeasureRepository(no.nav.fjernlys.dataSource)
    private val HistoryRiskReportRepository = HistoryRiskReportRepository(no.nav.fjernlys.dataSource)
    private val HistoryRiskAssessmentRepository = HistoryRiskAssessmentRepository(no.nav.fjernlys.dataSource)
    private val HistoryRiskMeasureRepository = HistoryRiskMeasureRepository(no.nav.fjernlys.dataSource)

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

}
