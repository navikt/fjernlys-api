package no.nav.fjernlys.database

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.junit.jupiter.api.Test
import testcontainers.TestContainersHelper

class RiskReportDbTest {
    private val dataSource = TestContainersHelper.postgresContainer.dataSource

    @Test
    fun `migration has been run`(){
        using(sessionOf(dataSource)) { session ->
            val sql = """
            SELECT
                *
            FROM 
               risiko_rapport
        """.trimIndent()

        val query = queryOf(statement = sql).map(this::mapRowToStatusoversikt).asList
        print(session.run(query))
        }

    }
    private fun mapRowToStatusoversikt(row: Row): Int {
        return 1
    }
}