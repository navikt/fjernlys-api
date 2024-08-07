package no.nav.fjernlys.functions

import no.nav.fjernlys.dbQueries.RiskAssessmentRepository
import no.nav.fjernlys.dbQueries.RiskCategoryRepository
import no.nav.fjernlys.dbQueries.RiskProbConsRepository
import no.nav.fjernlys.plugins.RiskProbCons
import no.nav.fjernlys.plugins.RiskProbCons1
import no.nav.fjernlys.plugins.RiskProbConsCalculatedValues
import javax.sql.DataSource
import org.slf4j.LoggerFactory

class UpdateRiskProbConsTable(datasource: DataSource) {

    private val logger = LoggerFactory.getLogger(UpdateRiskProbConsTable::class.java)

    private val riskAssessmentRepository = RiskAssessmentRepository(datasource)
    private val riskCategoryRepository = RiskCategoryRepository(datasource)
    private val riskProbConsRepository = RiskProbConsRepository(datasource)

    fun updateRiskProbConsTable() {
        try {
            val allServices = riskProbConsRepository.getAllUniqueServices()
            allServices.forEach { service ->
                logger.info("Processing service: $service")
                riskProbConsRepository.updateRiskProbConsForAllCategories(service)
            }
            logger.info("Completed updating risk probability and consequence table.")
        } catch (e: Exception) {
            logger.error("Error updating risk probability and consequence table", e)
        }
    }

//    fun calculateRiskProbConsValues(): List<RiskProbConsCalculatedValues> {
//        val allCategories = riskProbConsRepository.getAllFromProbConsTable()
//        val processedValues = allCategories.map { category ->
//            val catName = category.categoryName
//            val prob = category.probability / category.totalRisksPerCategory
//            val cons = category.consequence / category.totalRisksPerCategory
//            val totalRisk = category.totalRisksPerCategory
//
//            RiskProbConsCalculatedValues(
//                categoryName = catName,
//                prob = prob,
//                cons = cons,
//                totalRisks = totalRisk
//            )
//        }
//        return processedValues
//    }
}