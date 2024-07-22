package no.nav.fjernlys.dbQueries

import no.nav.fjernlys.dataSource
import javax.sql.DataSource

class GetRiskLevelData(datasource: DataSource) {

    fun getRiskLevel(): Map<String, Int> {
        val report = RiskReportRepository(dataSource).getAllRiskReports()
        val riskAssessmentRepository = RiskAssessmentRepository(dataSource)

        val riskLevelList = report.flatMap { reportEl ->
            riskAssessmentRepository.getRiskAssessmentFromReportId(reportEl.id)
        }.map { assessment ->
            assessment.riskLevel
        }

        return riskLevelList.groupingBy { it }.eachCount()
    }
}
