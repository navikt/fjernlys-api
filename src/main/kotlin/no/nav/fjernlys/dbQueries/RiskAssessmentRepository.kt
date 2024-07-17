package no.nav.fjernlys.dbQueries

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.fjernlys.dbQueries.RiskMeasureRepository.RiskMeasureData
import javax.sql.DataSource

class RiskAssessmentRepository(val dataSource: DataSource) {


    fun insertIntoRiskAssessment(
        id: String,
        report_id: String,
        probability: Number,
        consequence: Number,
        dependent: Boolean,
        risk_level: String,
        category: String,
        new_probability: Double?,
        new_consequence: Double?
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
                    report_id,
                    probability,
                    consequence,
                    dependent,
                    risk_level,
                    category,
                    new_probability,
                    new_consequence
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
                            report_id = row.string("report_id"),
                            probability = row.double("probability"),
                            consequence = row.double("consequence"),
                            dependent = row.boolean("dependent"),
                            risk_level = row.string("risk_level"),
                            category = row.string("category"),
                            new_probability = row.double("new_probability"),
                            new_consequence = row.double("new_consequence")
                        )
                    }
                    .asSingle
            )
        }
    }

    fun getRiskMeasureFromAssessmentId(report_id: String): List<RiskAssessmentData> {
        val sql = """
        SELECT * FROM risk_assessment WHERE report_id = :report_id
    """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(sql, mapOf("report_id" to report_id))
                    .map { row ->
                        RiskAssessmentData(
                            id = row.string("id"),
                            report_id = row.string("report_id"),
                            probability = row.double("probability"),
                            consequence = row.double("consequence"),
                            dependent = row.boolean("dependent"),
                            risk_level = row.string("risk_level"),
                            category = row.string("category"),
                            new_consequence = row.double("new_consequence"),
                            new_probability = row.double("new_probability")
                        )
                    }
                    .asList
            )
        }
    }

    data class RiskAssessmentData(
        val id: String,
        val report_id: String,
        val probability: Number,
        val consequence: Number,
        val dependent: Boolean,
        val risk_level: String,
        val category: String,
        val new_probability: Number?,
        val new_consequence: Number?
    )

    fun mapRowToRiskAssessment(row: Row): RiskAssessmentData {
        // Convert a single row into a RisikoRapport object
        return RiskAssessmentData(
            id = row.string("id"),
            report_id = row.string("report_id"),
            probability = row.double("probability"),
            consequence = row.double("consequence"),
            dependent = row.boolean("dependent"),
            risk_level = row.string("risk_level"),
            category = row.string("category"),
            new_probability = row.double("new_probability"),
            new_consequence = row.double("new_consequence")
        )
    }


}


