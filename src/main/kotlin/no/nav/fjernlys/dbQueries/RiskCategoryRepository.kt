package no.nav.fjernlys.dbQueries

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.fjernlys.dataSource
import no.nav.fjernlys.plugins.RiskCategoryCounts
import java.util.Locale.Category
import javax.sql.DataSource

class RiskCategoryRepository(dataSource: DataSource) {

    fun updateDependentTable(
        categoryName: String,
        dependentRisk: Int,
        notDependentRisk: Int,
        totalRisk: Int,
    ) {
        using(sessionOf(dataSource)) { session ->
            val sql = """
                UPDATE risk_category_table
                SET dependent_risk = ?, not_dependent_risk = ?, total_risk = ?
                WHERE category_name = ?
            """.trimIndent()

            session.run(
                queryOf(
                    sql,
                    dependentRisk,
                    notDependentRisk,
                    totalRisk,
                    categoryName
                ).asUpdate
            )
        }
    }


    fun getAllUniqueCategories(): List<String> {
        val sql = """
            SELECT DISTINCT category_name
            FROM risk_category_table
        """.trimIndent()

        return using(sessionOf(dataSource)) {session ->
            session.run(queryOf(sql).map { row ->
                row.string(("category_name"))
            }.asList)
        }
    }

    fun getAll(): List<RiskCategoryCounts> {
        val sql = """
            SELECT * 
            FROM risk_category_table
        """.trimIndent()

        return using(sessionOf(dataSource)) {session ->
            session.run(queryOf(sql).map {row ->
                RiskCategoryCounts(
                    category = row.string("category_name"),
                    dependent = row.int("dependent_risk"),
                    notDependent = row.int("not_dependent_risk"),
                    totalRisk = row.int("total_risk")
                )
            }.asList)
        }
    }

    fun getDependentByCategoryName(categoryName: String): List<RiskCategoryCounts> {
        val sql="""
            SELECT * 
            FROM risk_category_table
            WHERE category_name = :categoryName
        """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(queryOf(sql, mapOf("categoryName" to categoryName)).map { row ->
                RiskCategoryCounts(
                    category = row.string("category_name"),
                    dependent = row.int("dependent_risk"),
                    notDependent = row.int("not_dependent_risk"),
                    totalRisk = row.int("total_risk")
                )
            }.asList)
        }
    }






}