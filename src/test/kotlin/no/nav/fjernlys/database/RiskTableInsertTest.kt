package no.nav.fjernlys.dbQueries

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.fjernlys.NaisEnvironment
import no.nav.fjernlys.createDataSource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DbQueryInsertTest {

    private lateinit var dbQueryInsert: DbQueryInsert
    private val naisEnv = NaisEnvironment()
    private val dataSource = createDataSource(database = naisEnv.database)

    @BeforeEach
    fun setup() {
        dbQueryInsert = DbQueryInsert()
        clearDatabase()
    }

    @AfterEach
    fun teardown() {
        clearDatabase()
    }

    @Test
    fun testInsertInfo() {
        dbQueryInsert.insertInfo()

        using(sessionOf(dataSource)) { session ->
            val sql = "SELECT * FROM risiko_rapport"
            val query = queryOf(sql).map { row -> dbQueryInsert.mapRowToRisikoRapport(row) }.asList
            val results = session.run(query)

            assertEquals(1, results.size)
            val record = results[0]
            assertEquals("123e4567-e89b-12d3-a456-426614174001", record.id)
            assertEquals(true, record.isOwner)
            assertEquals("OWN12345", record.ownerIdent)
            assertEquals("ServiceX", record.serviceName)
            // Additional assertions can be added here as needed
        }
    }



    private fun clearDatabase() {
        using(sessionOf(dataSource)) { session ->
            val sql = "DELETE FROM risiko_rapport"
            val query = queryOf(sql).asUpdate
            session.run(query)
        }
    }
}
