package no.nav.fjernlys.dbQueries

import kotliquery.queryOf
import kotliquery.Row
import kotliquery.sessionOf
import kotliquery.using
import no.nav.fjernlys.NaisEnvironment
import no.nav.fjernlys.createDataSource
import javax.sql.DataSource

class DbQueryInsert(val dataSource: DataSource) {

    fun insertInfo() {
        using(sessionOf(dataSource)) { session ->

            val sql = """
                INSERT INTO risiko_rapport (
                    id,
                    isOwner,
                    ownerIdent,
                    serviceName,
                    opprettet,
                    endret
                ) VALUES (
                    '123e4567-e89b-12d3-a456-426614174001',  -- UUID for id
                    true,                                   -- isOwner
                    'OWN12345',                             -- ownerIdent
                    'ServiceX',                             -- serviceName
                    current_timestamp,                      -- opprettet
                    current_timestamp                       -- endret
                )
            """.trimIndent()

            val query = queryOf(statement = sql).asUpdate
            session.run(query)
        }
    }

    fun printAllRecords() {
        val naisEnv = NaisEnvironment()
        val dataSource = createDataSource(database = naisEnv.database)
        using(sessionOf(dataSource)) { session ->

            val sql = "SELECT * FROM risiko_rapport"

            // Map each row to RisikoRapport object and collect them into a list
            val query = queryOf(sql).map { row -> mapRowToRisikoRapport(row) }.asList
            val results = session.run(query)

            // Print each record in the results list
            results.forEach { record ->
                println(record)
            }
        }
    }

    fun mapRowToRisikoRapport(row: Row): RisikoRapport {
        // Convert a single row into a RisikoRapport object
        return RisikoRapport(
            id = row.string("id"),
            isOwner = row.boolean("isOwner"),
            ownerIdent = row.string("ownerIdent"),
            serviceName = row.string("serviceName"),
            opprettet = row.localDateTime("opprettet"),
            endret = row.localDateTime("endret")
        )
    }

    // Data class representing a record in the risiko_rapport table
    data class RisikoRapport(
        val id: String,
        val isOwner: Boolean,
        val ownerIdent: String,
        val serviceName: String,
        val opprettet: java.time.LocalDateTime,
        val endret: java.time.LocalDateTime
    )
}
