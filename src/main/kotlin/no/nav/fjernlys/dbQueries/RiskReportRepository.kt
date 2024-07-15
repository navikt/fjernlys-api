package no.nav.fjernlys.dbQueries

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.fjernlys.NaisEnvironment
import no.nav.fjernlys.createDataSource
import javax.sql.DataSource
import java.time.LocalDateTime

class RiskReportRepository(val dataSource: DataSource) {


    fun insertIntoRiskReport(
        id: String,
        is_owner: Boolean,
        owner_ident: String,
        service_name: String,
        report_created: LocalDateTime,
        report_edited: LocalDateTime,
    ) {
        using(sessionOf(dataSource)) { session ->

            val sql = """
            INSERT INTO risk_report (
                id, is_owner, owner_ident, service_name, report_created, report_edited
            ) VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()

            session.run(queryOf(sql, id, is_owner, owner_ident, service_name, report_created, report_edited).asUpdate)
        }
    }

    fun getRiskReportFromId(id: String): RiskReportData? {
        val sql = """
        SELECT * FROM risk_report WHERE id = :id
    """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(sql, mapOf("id" to id))
                    .map { row ->
                        RiskReportData(
                            id = row.string("id"),
                            is_owner = row.boolean("isOwner"),
                            owner_ident = row.string("ownerIdent"),
                            service_name = row.string("serviceName"),
                            report_created = row.localDateTime("reportCreated"),
                            report_edited = row.localDateTime("reportEdited"),
                        )
                    }
                    .asSingle
            )
        }
    }

    data class RiskReportData(
        val id: String,
        val is_owner: Boolean,
        val owner_ident: String,
        val service_name: String,
        val report_created: LocalDateTime,
        val report_edited: LocalDateTime
    )

    fun mapRowToRiskReport(row: Row): RiskReportData {
        // Convert a single row into a RisikoRapport object
        return RiskReportData(
            id = row.string("id"),
            is_owner = row.boolean("isOwner"),
            owner_ident = row.string("ownerIdent"),
            service_name = row.string("serviceName"),
            report_created = row.localDateTime("reportCreated"),
            report_edited = row.localDateTime("reportEdited"),
        )
    }


}