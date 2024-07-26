package no.nav.fjernlys.dbQueries

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.fjernlys.plugins.RiskProbCons
import javax.sql.DataSource

class RiskProbConsRepository(val dataSource: DataSource)  {

    fun updateRiskProbConsTable(
        categoryName: String,
        probability: Double,
        consequence: Double,
        newProbability: Double,
        newConsequence: Double,
        totalRisks: Int
    ) {
        using(sessionOf(dataSource)) { session ->
            val sql = """
                INSERT INTO risk_prob_cons_table (category_name, probability, consequence, new_probability, new_consequence, total_risk)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (category_name) DO UPDATE SET
                    probability = EXCLUDED.probability,
                    consequence = EXCLUDED.consequence,
                    new_probability = EXCLUDED.new_probability,
                    new_consequence = EXCLUDED.new_consequence,
                    total_risk = EXCLUDED.total_risk
            """.trimIndent()

            session.run(
                queryOf(
                    sql,
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

    fun getAllFromProbConsTable(): List<RiskProbCons> {
        val sql = """
            SELECT * 
            FROM risk_prob_cons_table
        """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(queryOf(sql).map { row ->
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
}
