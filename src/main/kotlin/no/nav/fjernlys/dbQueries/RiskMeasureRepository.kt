package no.nav.fjernlys.dbQueries

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.fjernlys.NaisEnvironment
import no.nav.fjernlys.createDataSource
import no.nav.fjernlys.plugins.RiskMeasureData
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

    fun getRiskMeasureFromId(id: String): RiskMeasureData? {
        val sql = """
        SELECT * FROM risk_measure WHERE id = :id
    """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(sql, mapOf("id" to id))
                    .map { row ->
                        RiskMeasureData(
                            id = row.string("id"),
                            riskAssessmentId = row.string("risk_assessment_id"),
                            measureCategory = row.string("measure_category"),
                            measureStatus = row.string("measure_status"),
                        )
                    }
                    .asSingle
            )
        }
    }

    fun getRiskMeasureFromAssessmentId(risk_assessment_id: String): List<RiskMeasureData> {
        val sql = """
        SELECT * FROM risk_measure WHERE risk_assessment_id = :risk_assessment_id
    """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(sql, mapOf("risk_assessment_id" to risk_assessment_id))
                    .map { row ->
                        RiskMeasureData(
                            id = row.string("id"),
                            riskAssessmentId = row.string("risk_assessment_id"),
                            measureCategory = row.string("measure_category"),
                            measureStatus = row.string("measure_status"),
                        )
                    }
                    .asList
            )
        }
    }


}