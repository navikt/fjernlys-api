package no.nav.fjernlys.dbQueries

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.fjernlys.NaisEnvironment
import no.nav.fjernlys.createDataSource
import javax.sql.DataSource

class RiskMeasureRepository(val dataSource: DataSource) {


    fun insertIntoRiskAssessment(
        id: String,
        risk_assessment_id: String,
        measure_category: String,
        measure_status: String,
        measure_started: Boolean
    ) {
        using(sessionOf(dataSource)) { session ->

            val sql = """
            INSERT INTO risk_assessment (
                id, riskAssessmentId, category, status, startedMeasure
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

            session.run(
                queryOf(
                    sql,
                    id,
                    risk_assessment_id,
                    measure_category,
                    measure_status,
                    measure_started,
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
                            risk_assessment_id = row.string("risk_assessment_id"),
                            measure_category = row.string("measure_category"),
                            measure_status = row.string("measure_status"),
                            measure_started = row.boolean("measure_started"),
                        )
                    }
                    .asSingle
            )
        }
    }

    data class RiskMeasureData(
        val id: String,
        val risk_assessment_id: String,
        val measure_category: String,
        val measure_status: String,
        val measure_started: Boolean
    )

    fun mapRowToRiskMeasure(row: Row): RiskMeasureData {
        // Convert a single row into a RisikoRapport object
        return RiskMeasureData(
            id = row.string("id"),
            risk_assessment_id = row.string("risk_assessment_id"),
            measure_category = row.string("measure_category"),
            measure_status = row.string("measure_status"),
            measure_started = row.boolean("measure_started"),
        )
    }


}