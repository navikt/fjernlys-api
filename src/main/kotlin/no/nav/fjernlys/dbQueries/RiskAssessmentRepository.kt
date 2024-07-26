package no.nav.fjernlys.dbQueries

import kotlinx.serialization.Serializable
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.fjernlys.plugins.MeasureValue
import no.nav.fjernlys.plugins.RiskAssessmentData
import javax.sql.DataSource

class RiskAssessmentRepository(val dataSource: DataSource) {


    fun insertIntoRiskAssessment(
        id: String,
        reportId: String,
        probability: Number,
        consequence: Number,
        dependent: Boolean,
        riskLevel: String,
        category: String,
        newProbability: Double?,
        newConsequence: Double?
    ) {

        using(sessionOf(dataSource)) { session ->

            val sql = """
                INSERT INTO risk_assessment (
                    id, report_id, probability, consequence, dependent, risk_level, category, new_probability, new_consequence
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            session.run(
                queryOf(
                    sql,
                    id,
                    reportId,
                    probability,
                    consequence,
                    dependent,
                    riskLevel,
                    category,
                    newProbability,
                    newConsequence
                ).asUpdate
            )

        }
    }

    fun getRiskAssessmentFromId(id: String): RiskAssessmentData? {
        val sql = """
        SELECT * FROM risk_assessment WHERE id = :id
    """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(sql, mapOf("id" to id))
                    .map { row ->
                        RiskAssessmentData(
                            id = row.string("id"),
                            reportId = row.string("report_id"),
                            probability = row.double("probability"),
                            consequence = row.double("consequence"),
                            dependent = row.boolean("dependent"),
                            riskLevel = row.string("risk_level"),
                            category = row.string("category"),
                            newProbability = row.double("new_probability"),
                            newConsequence = row.double("new_consequence")
                        )
                    }
                    .asSingle
            )
        }
    }

    fun getRiskAssessmentFromReportId(report_id: String): List<RiskAssessmentData> {
        val sql = """
        SELECT * FROM risk_assessment WHERE report_id = :report_id
    """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(sql, mapOf("report_id" to report_id))
                    .map { row ->
                        RiskAssessmentData(
                            id = row.string("id"),
                            reportId = row.string("report_id"),
                            probability = row.double("probability"),
                            consequence = row.double("consequence"),
                            dependent = row.boolean("dependent"),
                            riskLevel = row.string("risk_level"),
                            category = row.string("category"),
                            newProbability = row.double("new_probability"),
                            newConsequence = row.double("new_consequence")
                        )
                    }
                .asList
            )
        }
    }

//    ----------------------------------------------------------


    fun getRiskAssessmentByCategory(category: String): List<RiskAssessmentData> {
        val sql = """
        SELECT *
        FROM risk_assessment
        WHERE category = :category
    """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(queryOf(sql, mapOf("category" to category)).map { row ->
                RiskAssessmentData(
                    id = row.string("id"),
                    reportId = row.string("report_id"),
                    probability = row.double("probability"),
                    consequence = row.double("consequence"),
                    dependent = row.boolean("dependent"),
                    riskLevel = row.string("risk_level"),
                    category = row.string("category"),
                    newProbability = row.double("new_probability"),
                    newConsequence = row.double("new_consequence")
                )
            }.asList)
        }
    }


    @Serializable
    data class CategoryDependentData(
        val categoryName: String,
        val dependent: Int,
        val notDependent: Int,
        val totalRisksPerCategory: Int,
    )



    fun mapRowToRiskAssessment(row: Row): RiskAssessmentData {
        // Convert a single row into a RisikoRapport object
        return RiskAssessmentData(
            id = row.string("id"),
            reportId = row.string("report_id"),
            probability = row.double("probability"),
            consequence = row.double("consequence"),
            dependent = row.boolean("dependent"),
            riskLevel = row.string("risk_level"),
            category = row.string("category"),
            newProbability = row.double("new_probability"),
            newConsequence = row.double("new_consequence")
        )
    }
}


