package no.nav.fjernlys.functions

import no.nav.fjernlys.dataSource
import no.nav.fjernlys.dbQueries.RiskAssessmentRepository
import no.nav.fjernlys.dbQueries.RiskLevelsRepository
import no.nav.fjernlys.dbQueries.RiskReportRepository
import no.nav.fjernlys.plugins.RiskLevelCounts
import no.nav.fjernlys.plugins.RiskLevelData
import javax.sql.DataSource

class UpdateRiskLevelData(datasource: DataSource) {

    fun getRiskLevelByService(serviceName: String): RiskLevelCounts {
        val report = RiskReportRepository(dataSource).getAllRiskReportsByService(serviceName)
        val riskAssessmentRepository = RiskAssessmentRepository(dataSource)
        println("HERE IS THE REPORT " + report.toString())
        
        val riskLevelList = report.flatMap { reportEl ->
            riskAssessmentRepository.getRiskAssessmentFromReportId(reportEl.id)
        }.map { assessment ->
            assessment.riskLevel
        }
        println("HERE IS THE RISKLEVELLIST" + riskLevelList.toString())

        val riskLevelCount = riskLevelList.groupingBy { it }.eachCount()

        return RiskLevelCounts(
            high = riskLevelCount["Høy"]?.toInt() ?: 0,
            moderate = riskLevelCount["Moderat"]?.toInt() ?: 0,
            low = riskLevelCount["Lav"]?.toInt() ?: 0
        )
    }

    fun updateRiskLevelByService(serviceName: String) {
        val serviceRiskLevels = getRiskLevelByService(serviceName)
        RiskLevelsRepository(dataSource).updateRiskLevel(
            serviceName,
            serviceRiskLevels.high,
            serviceRiskLevels.moderate,
            serviceRiskLevels.low
        )
        updateTotalRiskValues()

    }

    fun getAllRiskLevelsExceptTotal(): RiskLevelCounts {
        val allRiskLevels = RiskLevelsRepository(dataSource).getAllRiskLevelsExceptTotal()
        val riskLevelCounts = RiskLevelCounts()
        allRiskLevels.forEach { riskLevelData ->
            riskLevelCounts.high += riskLevelData.high
            riskLevelCounts.moderate += riskLevelData.moderate
            riskLevelCounts.low += riskLevelData.low
        }
        return riskLevelCounts
    }

    fun updateTotalRiskValues() {
        val allRiskValuesExceptTotal: RiskLevelCounts = getAllRiskLevelsExceptTotal()

        RiskLevelsRepository(dataSource).updateRiskLevel(
            "All", allRiskValuesExceptTotal.high, allRiskValuesExceptTotal.moderate, allRiskValuesExceptTotal.low
        )
    }

    fun getRiskLevelValuesByService(serviceName: String): RiskLevelData {
        val riskLevels = RiskLevelsRepository(dataSource).getRiskLevelByService(serviceName)

        return riskLevels
    }
}
