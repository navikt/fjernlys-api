package no.nav.fjernlys.dbQueries

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import javax.sql.DataSource
import kotlinx.serialization.Serializable
import no.nav.fjernlys.plugins.RiskReportData

class RiskReportRepository(val dataSource: DataSource) {



    fun insertIntoRiskReport(
        id: String,
        is_owner: Boolean,
        owner_ident: String,
        service_name: String,
        report_created: Instant,
        report_edited: Instant,
    ) {
        using(sessionOf(dataSource)) { session ->

            val sql = """
            INSERT INTO risk_report (
                id, is_owner, owner_ident, service_name, report_created, report_edited
            ) VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()

            session.run(
                queryOf(
                    sql,
                    id,
                    is_owner,
                    owner_ident,
                    service_name,
                    report_created.toJavaInstant(),
                    report_edited.toJavaInstant()
                ).asUpdate
            )
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
                            isOwner = row.boolean("is_owner"),
                            ownerIdent = row.string("owner_ident"),
                            serviceName = row.string("service_name"),
                            reportCreated = Instant.fromEpochMilliseconds(row.sqlTimestamp("report_created").time),
                            reportEdited = Instant.fromEpochMilliseconds(row.sqlTimestamp("report_edited").time),
                        )
                    }
                    .asSingle
            )
        }
    }

    fun getAllRiskReportIds(): List<String> {
        val sql = """
        SELECT id FROM risk_report
    """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(queryOf(sql).map { row ->
                row.string("id")  // Directly map to string
            }.asList)
        }
    }

    fun getRiskReportIdFromService(service_name: String): List<RiskReportData> {
        val sql = """
        SELECT * 
        FROM risk_report 
        WHERE service_name = :service_name 
        ORDER BY report_created DESC;
    """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(queryOf(sql, mapOf("service_name" to service_name)).map { row ->
                RiskReportData(
                    id = row.string("id"),
                    isOwner = row.boolean("is_owner"),
                    ownerIdent = row.string("owner_ident"),
                    serviceName = row.string("service_name"),
                    reportCreated = Instant.fromEpochMilliseconds(row.sqlTimestamp("report_created").time),
                    reportEdited = Instant.fromEpochMilliseconds(row.sqlTimestamp("report_edited").time)
                )
            }.asList)
        }
    }
}
