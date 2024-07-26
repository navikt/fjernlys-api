package no.nav.fjernlys.functions

import no.nav.fjernlys.dbQueries.RiskAssessmentRepository
import no.nav.fjernlys.dbQueries.RiskCategoryRepository
import no.nav.fjernlys.dbQueries.RiskProbConsRepository
import no.nav.fjernlys.plugins.RiskProbCons
import javax.sql.DataSource
import org.slf4j.LoggerFactory

class UpdateRiskProbConsTable(datasource: DataSource) {

    private val logger = LoggerFactory.getLogger(UpdateRiskProbConsTable::class.java)

    private val riskAssessmentRepository = RiskAssessmentRepository(datasource)
    private val riskCategoryRepository = RiskCategoryRepository(datasource)
    private val riskProbConsRepository = RiskProbConsRepository(datasource)

    fun updateRiskProbConsTable() {
        try {
            val allCategories = riskCategoryRepository.getAllUniqueCategories()
            allCategories.forEach { category ->
                logger.info("Processing category: $category")
                val categorySums = getProbAndConsValues(category)
                updateRiskProbCons(categorySums)
            }
            logger.info("Completed updating risk probability and consequence table.")
        } catch (e: Exception) {
            logger.error("Error updating risk probability and consequence table", e)
        }
    }

    private fun getProbAndConsValues(categoryName: String): RiskProbCons {
        return try {
            val categoryData = riskAssessmentRepository.getRiskAssessmentByCategory(categoryName)
            val totalProbability = categoryData.sumOf { it.probability }
            val totalConsequence = categoryData.sumOf { it.consequence }
            val totalNewProbability = categoryData.sumOf { it.newProbability ?: 0.0 }
            val totalNewConsequence = categoryData.sumOf { it.newConsequence ?: 0.0 }
            val totalRisksForCategory = categoryData.size

            RiskProbCons(
                categoryName = categoryName,
                probability = totalProbability,
                consequence = totalConsequence,
                newProbability = if (categoryData.any { it.newProbability != null }) totalNewProbability else null,
                newConsequence = if (categoryData.any { it.newConsequence != null }) totalNewConsequence else null,
                totalRisksPerCategory = totalRisksForCategory
            )
        } catch (e: Exception) {
            logger.error("Error calculating probabilities and consequences for category: $categoryName", e)
            throw e
        }
    }

    private fun updateRiskProbCons(riskProbConsSums: RiskProbCons) {
        try {
            riskProbConsRepository.updateRiskProbConsTable(
                riskProbConsSums.categoryName,
                riskProbConsSums.probability,
                riskProbConsSums.consequence,
                riskProbConsSums.newProbability ?: 0.0, // Provide default value
                riskProbConsSums.newConsequence ?: 0.0, // Provide default value
                riskProbConsSums.totalRisksPerCategory
            )
            logger.info("Updated risk probability and consequence for category: ${riskProbConsSums.categoryName}")
        } catch (e: Exception) {
            logger.error("Error updating risk probability and consequence for category: ${riskProbConsSums.categoryName}", e)
            throw e
        }
    }
}
