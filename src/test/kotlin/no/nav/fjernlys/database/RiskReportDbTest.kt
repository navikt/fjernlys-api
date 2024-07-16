package no.nav.fjernlys.database

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.junit.jupiter.api.Test
import testcontainers.TestContainersHelper

class RiskReportDbTest {
    @Test
    fun `harTabell Report`() {
        val dataSource = TestContainersHelper.postgresContainer.dataSource
        using(sessionOf(dataSource)) { session ->
            val sql = """
            SELECT
                *
            FROM 
               risk_report  
        """.trimIndent()

            val query = queryOf(statement = sql).map(this::mapRowToStatusoversikt).asList
            print(session.run(query))
        }

    }

    @Test
    fun `harTabell Assessment`() {
        val dataSource = TestContainersHelper.postgresContainer.dataSource
        using(sessionOf(dataSource)) { session ->
            val sql = """
            SELECT
                *
            FROM 
               risk_assessment  
        """.trimIndent()

            val query = queryOf(statement = sql).map(this::mapRowToStatusoversikt).asList
            print(session.run(query))
        }

    }

    @Test
    fun `harTabell Measure`() {
        val dataSource = TestContainersHelper.postgresContainer.dataSource
        using(sessionOf(dataSource)) { session ->
            val sql = """
            SELECT
                *
            FROM 
               risk_measure  
        """.trimIndent()

            val query = queryOf(statement = sql).map(this::mapRowToStatusoversikt).asList
            print(session.run(query))
        }

    }

    private fun mapRowToStatusoversikt(rad: Row): Int {
        return 1
    }
}