package no.nav.fjernlys.dbQueries

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.fjernlys.plugins.RiskAssessmentData
import java.util.*
import javax.sql.DataSource
import kotliquery.using

class HistoryRiskAssessmentRepository(val dataSource: DataSource) {

    fun getLastEditedRiskAssessment(id: String): RiskAssessmentData? {
        val sql = """
            SELECT * 
            FROM risk_assessment 
            WHERE report_id = :id
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

    fun insertLastEntryIntoRiskAssessmentHistory(riskAssessment: RiskAssessmentData): Boolean {
        val newId = UUID.randomUUID().toString()
        val sql = """
            INSERT INTO history_risk_assessment
            (id, report_id, probability, consequence, dependent, risk_level, category, new_probability, new_consequence)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            val result = session.run(
                queryOf(
                    sql,
                    newId,
                    riskAssessment.reportId,
                    riskAssessment.probability,
                    riskAssessment.consequence,
                    riskAssessment.dependent,
                    riskAssessment.riskLevel,
                    riskAssessment.category,
                    riskAssessment.newProbability,
                    riskAssessment.newConsequence
                ).asUpdate
            )
            result > 0
        }
    }
}