package no.nav.fjernlys.dbQueries

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.fjernlys.plugins.RiskAssessmentData
import java.util.*
import javax.sql.DataSource
import kotliquery.using

class HistoryRiskAssessmentRepository(val dataSource: DataSource) {

    fun getLastEditedRiskAssessment(reportId: String): List<RiskAssessmentData> {
        val sql = """
        SELECT * FROM risk_assessment WHERE report_id = :reportId

        """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(sql, mapOf("reportId" to reportId))
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

    fun insertLastEntryIntoRiskAssessmentHistory(
        riskAssessment: RiskAssessmentData,
        historyReportId: String,
        newAssesmentId: String
    ): Boolean {
        val sql = """
            INSERT INTO history_risk_assessment (
                id,
                history_report_id,
                probability,
                consequence,
                dependent,
                risk_level,
                category,
                new_consequence,
                new_probability
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            val result = session.run(
                queryOf(
                    sql,
                    newAssesmentId,
                    historyReportId,
                    riskAssessment.probability,
                    riskAssessment.consequence,
                    riskAssessment.dependent,
                    riskAssessment.riskLevel,
                    riskAssessment.category,
                    riskAssessment.newConsequence,
                    riskAssessment.newProbability
                ).asUpdate
            )
            result > 0
        }
    }

    fun getHistoryRiskAssessmentFromHistoryReportId(historyReportId: String): List<RiskAssessmentRepository.RiskAssessmentData> {
        val sql = """
        SELECT * FROM history_risk_assessment WHERE history_report_id = :historyReportId
    """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(sql, mapOf("historyReportId" to historyReportId))
                    .map { row ->
                        RiskAssessmentRepository.RiskAssessmentData(
                            id = row.string("id"),
                            reportId = row.string("history_report_id"),
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

}
