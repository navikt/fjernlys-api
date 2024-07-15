package no.nav.fjernlys.database

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.fjernlys.NaisEnvironment
import no.nav.fjernlys.createDataSource
import no.nav.fjernlys.dbQueries.DbQueryInsert
import org.junit.jupiter.api.Test
import testcontainers.TestContainersHelper

class RiskReportDbTest {
    @Test
    fun `harTabell`() {
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