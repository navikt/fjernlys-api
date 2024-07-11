package no.nav.fjernlys.database

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.fjernlys.NaisEnvironment
import no.nav.fjernlys.createDataSource
import no.nav.fjernlys.runMigration
import org.junit.Test

class RiskReportDbTest {
    @Test
    fun `harTabell`(){
        val naisEnv = NaisEnvironment()
        val dataSource = createDataSource(database = naisEnv.database)
        using(sessionOf(dataSource)) { session ->
            val sql = """
            SELECT
                *
            FROM 
               risiko_rapport
        """.trimIndent()

        val query = queryOf(statement = sql).map(this::mapRowToStatusoversikt).asList
        session.run(query)
        }

    }
    private fun mapRowToStatusoversikt(rad: Row): Int {
        return 1
    }
}