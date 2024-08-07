package no.nav.fjernlys.dbQueries

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.fjernlys.plugins.RiskAssessment
import no.nav.fjernlys.plugins.RiskProbCons
import no.nav.fjernlys.plugins.RiskProbCons1
import javax.sql.DataSource

class RiskProbConsRepository(val dataSource: DataSource) {

    fun updateRiskProbConsTable(
        serviceName: String,
        categoryName: String,
        probability: Double,
        consequence: Double,
        newProbability: Double?,
        newConsequence: Double?,
        totalRisks: Int
    ) {
        using(sessionOf(dataSource)) { session ->
            val sql = """
                INSERT INTO risk_prob_cons_table (service_name, category_name, probability, consequence, new_probability, new_consequence, total_risk)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (service_name, category_name) DO UPDATE SET
                    service_name = EXCLUDED.service_name,
                    probability = EXCLUDED.probability,
                    consequence = EXCLUDED.consequence,
                    new_probability = EXCLUDED.new_probability,
                    new_consequence = EXCLUDED.new_consequence,
                    total_risk = EXCLUDED.total_risk
            """.trimIndent()

            session.run(
                queryOf(
                    sql,
                    serviceName,
                    categoryName,
                    probability,
                    consequence,
                    newProbability,
                    newConsequence,
                    totalRisks
                ).asUpdate
            )
        }
    }

    fun getRiskAssessmentByServiceAndCategory(serviceName: String, categoryName: String): List<RiskAssessment> {
        val sql = """
            SELECT * 
            FROM risk_assessment ra
            JOIN risk_report rr ON ra.report_id = rr.id
            WHERE rr.service_name = ? AND ra.category = ?
        """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(queryOf(sql, serviceName, categoryName).map { row ->
                RiskAssessment(
                    id = row.string("id"),
                    reportId = row.string("report_id"),
                    probability = row.double("probability"),
                    consequence = row.double("consequence"),
                    dependent = row.boolean("dependent"),
                    riskLevel = row.string("risk_level"),
                    category = row.string("category"),
                    newConsequence = row.doubleOrNull("new_consequence"),
                    newProbability = row.doubleOrNull("new_probability")
                )
            }.asList)
        }
    }

    fun getAllFromProbConsTable(): List<RiskProbCons1> {
        val sql = """
            SELECT *
            FROM risk_prob_cons_table
        """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(queryOf(sql).map { row ->
                RiskProbCons1(
                    serviceName = row.string("service_name"),
                    categoryName = row.string("category_name"),
                    probability = row.double("probability"),
                    consequence = row.double("consequence"),
                    newProbability = row.double("new_probability"),
                    newConsequence = row.double("new_consequence"),
                    totalRisksPerCategory = row.int("total_risk")
                )
            }.asList)
        }
    }

    fun getAllUniqueCategories(): List<String> {
        val sql = """
            SELECT DISTINCT category_name
            FROM risk_prob_cons_table
        """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(queryOf(sql).map { row ->
                row.string("category_name")
            }.asList)
        }
    }

    fun getAllUniqueServices(): List<String> {
        val sql = """
            SELECT DISTINCT service_name
            FROM risk_prob_cons_table
        """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(queryOf(sql).map { row ->
                row.string("service_name")
            }.asList)
        }
    }

    fun updateRiskProbConsForAllCategories(serviceName: String) {
        val categories = getAllUniqueCategories()

        categories.forEach { category ->
            val riskAssessments = getRiskAssessmentByServiceAndCategory(serviceName, category)

            if (riskAssessments.isNotEmpty()) {
                // Aggregate the data
                val totalRisks = riskAssessments.size
                val avgProbability = riskAssessments.map { it.probability }.average()
                val avgConsequence = riskAssessments.map { it.consequence }.average()
                val avgNewProbability = riskAssessments.mapNotNull { it.newProbability }.averageOrNull()
                val avgNewConsequence = riskAssessments.mapNotNull { it.newConsequence }.averageOrNull()

                // Update the risk_prob_cons_table
                updateRiskProbConsTable(
                    serviceName = serviceName,
                    categoryName = category,
                    probability = avgProbability,
                    consequence = avgConsequence,
                    newProbability = avgNewProbability,
                    newConsequence = avgNewConsequence,
                    totalRisks = totalRisks
                )
            }
        }
    }

    fun getDataByService(serviceName: String): List<RiskProbCons> {
        val sql = """
            SELECT *
            FROM risk_prob_cons_table
            WHERE service_name = :serviceName
        """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(queryOf(sql, mapOf("serviceName" to serviceName)).map { row ->
                RiskProbCons(
                    categoryName = row.string("category_name"),
                    probability = row.double("probability"),
                    consequence = row.double("consequence"),
                    newProbability = row.double("new_probability"),
                    newConsequence = row.double("new_consequence"),
                    totalRisksPerCategory = row.int("total_risk")
                )
            }.asList)
        }
    }

    // Extension function to calculate the average of a nullable list of Doubles
    fun List<Double?>.averageOrNull(): Double? {
        return this.filterNotNull().takeIf { it.isNotEmpty() }?.average()
    }
}
