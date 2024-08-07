package no.nav.fjernlys.functions

import no.nav.fjernlys.dataSource
import no.nav.fjernlys.dbQueries.RiskAssessmentRepository
import no.nav.fjernlys.dbQueries.RiskCategoryRepository
import no.nav.fjernlys.plugins.RiskAssessmentData
import no.nav.fjernlys.plugins.RiskCategoryCounts
import javax.sql.DataSource

class UpdateCategoryTable(datasource: DataSource) {

    private val riskAssessmentRepository = RiskAssessmentRepository(datasource)
    private val riskCategoryRepository = RiskCategoryRepository(datasource)

    fun updateAllCategoriesCount () {
        val allCategories = riskCategoryRepository.getAllUniqueCategories()
        allCategories.forEach { category ->
            val categoryCounts = getCategoryCounts(category)
            updateCategoryTable(categoryCounts)
        }
    }

    private fun getCategoryCounts(categoryName: String): RiskCategoryCounts {
        val categoryData = riskAssessmentRepository.getRiskAssessmentByCategory(categoryName)
        val dependentCount = categoryData.count { it.dependent }
        val notDependentCount = categoryData.count { !it.dependent }
        val totalRisks = categoryData.size

        return RiskCategoryCounts(
            category = categoryName,
            dependent = dependentCount,
            notDependent = notDependentCount,
            totalRisk = totalRisks
        )
    }

    private fun updateCategoryTable(categoryCounts: RiskCategoryCounts) {
        riskCategoryRepository.updateDependentTable(
            categoryCounts.category,
            categoryCounts.dependent,
            categoryCounts.notDependent,
            categoryCounts.totalRisk
        )
    }
}

