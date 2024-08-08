package no.nav.fjernlys.dbQueries

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.fjernlys.plugins.MeasureValueOut
import no.nav.fjernlys.plugins.RiskAssessmentData
import java.util.*
import javax.sql.DataSource

class HistoryRiskMeasureRepository(val dataSource: DataSource) {

    fun getLastEditedRiskMeasure(assessmentId: String): List<MeasureValueOut> {
        val sql = """
        SELECT * 
        FROM risk_measure 
        WHERE risk_assessment_id = :assessmentId

        """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(sql, mapOf("assessmentId" to assessmentId))
                    .map { row ->
                        MeasureValueOut(
                            id = row.string("id"),
                            riskAssessmentId = row.string("risk_assessment_id"),
                            category = row.string("measure_category"),
                            status = row.string("measure_status"),
                        )
                    }
                    .asList
            )
        }
    }

    fun insertLastEntryIntoRiskMeasureHistory(riskMeasure: MeasureValueOut, newAssessmentId: String): Boolean {

        val newMeasureId = UUID.randomUUID().toString()

        val sql = """
            INSERT INTO history_risk_measure (
                id,
                risk_assessment_id,
                category, 
                status
            ) VALUES (?, ?, ?, ?)
        """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            val result = session.run(
                queryOf(
                    sql,
                    newMeasureId,
                    newAssessmentId,
                    riskMeasure.category,
                    riskMeasure.status,
                ).asUpdate
            )
            result > 0
        }
    }

    fun getHistoryRiskMeasureFromAssessmentId(historyAssessmentId: String): List<MeasureValueOut> {
        val sql = """
        SELECT * FROM history_risk_measure WHERE risk_assessment_id = :historyAssessmentId
    """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(sql, mapOf("historyAssessmentId" to historyAssessmentId))
                    .map { row ->
                        MeasureValueOut(
                            id = row.string("id"),
                            riskAssessmentId = row.string("risk_assessment_id"),
                            category = row.string("category"),
                            status = row.string("status"),
                        )
                    }
                    .asList
            )
        }
    }
}