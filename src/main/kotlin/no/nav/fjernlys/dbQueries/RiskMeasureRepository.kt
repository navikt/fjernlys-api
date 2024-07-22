package no.nav.fjernlys.dbQueries

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.fjernlys.NaisEnvironment
import no.nav.fjernlys.createDataSource
import no.nav.fjernlys.plugins.MeasureValue
import no.nav.fjernlys.plugins.MeasureValueOut
import javax.sql.DataSource

class RiskMeasureRepository(val dataSource: DataSource) {


    fun insertIntoRiskMeasure(
        id: String,
        riskAssessmentId: String,
        measureCategory: String,
        measureStatus: String,
    ) {
        using(sessionOf(dataSource)) { session ->

            val sql = """
            INSERT INTO risk_measure (
                id, risk_assessment_id, measure_category, measure_status
            ) VALUES (?, ?, ?, ?)
        """.trimIndent()

            session.run(
                queryOf(
                    sql,
                    id,
                    riskAssessmentId,
                    measureCategory,
                    measureStatus
                ).asUpdate
            )
        }
    }

    fun getRiskMeasureFromId(id: String): MeasureValueOut? {
        val sql = """
        SELECT * FROM risk_measure WHERE id = :id
    """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(sql, mapOf("id" to id))
                    .map { row ->
                        MeasureValueOut(
                            id = row.string("id"),
                            riskAssessmentId = row.string("risk_assessment_id"),
                            category = row.string("measure_category"),
                            status = row.string("measure_status"),
                        )
                    }
                    .asSingle
            )
        }
    }

    fun getRiskMeasureFromAssessmentId(risk_assessment_id: String): List<MeasureValueOut> {
        val sql = """
        SELECT * FROM risk_measure WHERE risk_assessment_id = :risk_assessment_id
    """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(sql, mapOf("risk_assessment_id" to risk_assessment_id))
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


}