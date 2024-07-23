package no.nav.fjernlys.dbQueries

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.fjernlys.dataSource
import no.nav.fjernlys.plugins.RiskLevelData
import javax.sql.DataSource

class RiskLevelsRepository(dataSource: DataSource) {


    fun updateRiskLevel(
        serviceName: String,
        high: Int,
        moderate: Int,
        low: Int,
    ) {
        using(sessionOf(dataSource)) { session ->

            val sql = """
            UPDATE risk_level_table
            SET high = ?, moderate = ?, low = ?
            WHERE service_name = ?
        """.trimIndent()

            session.run(
                queryOf(
                    sql, high, moderate, low, serviceName
                ).asUpdate
            )
        }
    }


    fun getRiskLevelByService(serviceName: String): RiskLevelData {
        val sql = """
        SELECT * FROM risk_level_table WHERE service_name = :serviceName
    """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(queryOf(sql, mapOf("serviceName" to serviceName)).map { row ->
                RiskLevelData(
                    serviceName = row.string("service_name"),
                    high = row.int("high"),
                    moderate = row.int("moderate"),
                    low = row.int("low"),
                )
            }.asSingle)!!
        }
    }

    fun getAllRiskLevels(): List<RiskLevelData> {
        val sql = """
        SELECT * FROM risk_level_table
    """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(queryOf(sql).map { row ->
                RiskLevelData(
                    serviceName = row.string("service_name"),
                    high = row.int("high"),
                    moderate = row.int("moderate"),
                    low = row.int("low"),
                )
            }.asList)
        }
    }

    fun getAllRiskLevelsExceptTotal(): List<RiskLevelData> {
        val serviceName = "All"
        val sql = """
        SELECT * FROM risk_level_table WHERE service_name != :serviceName
    """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(queryOf(sql, mapOf("serviceName" to serviceName)).map { row ->
                RiskLevelData(
                    serviceName = row.string("service_name"),
                    high = row.int("high"),
                    moderate = row.int("moderate"),
                    low = row.int("low")
                )
            }.asList)
        }
    }


}