package no.nav.fjernlys.dbQueries

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import javax.sql.DataSource
import no.nav.fjernlys.plugins.RiskReportData
import java.util.UUID

class HistoryRiskReportRepository(val dataSource: DataSource) {

    fun getLastEditedRiskReport(id: String): RiskReportData {
        val sql = """
            SELECT * 
            FROM risk_report 
            WHERE id = :id
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
                            reportEdited = Instant.fromEpochMilliseconds(row.sqlTimestamp("report_edited").time)
                        )
                    }
                    .asSingle
            )!!
        }
    }

    fun getAllHistoryReports(reportId: String): List<RiskReportData> {
        val sql = """
            SELECT * 
            FROM history_risk_report 
            WHERE report_id = :reportId
            ORDER BY report_edited DESC;
        """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(sql, mapOf("reportId" to reportId))
                    .map { row ->
                        RiskReportData(
                            id = row.string("id"),
                            isOwner = row.boolean("is_owner"),
                            ownerIdent = row.string("owner_ident"),
                            serviceName = row.string("service_name"),
                            reportCreated = Instant.fromEpochMilliseconds(row.sqlTimestamp("report_created").time),
                            reportEdited = Instant.fromEpochMilliseconds(row.sqlTimestamp("report_edited").time)
                        )
                    }
                    .asList
            )
        }
    }

    fun insertLastEntryIntoRiskReportHistory(reportData: RiskReportData, newId: String) {
        val sql = """
            INSERT INTO history_risk_report
            (id, report_id, is_owner, owner_ident, service_name, report_created, report_edited)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            val result = session.run(
                queryOf(
                    sql,
                    newId,
                    reportData.id,
                    reportData.isOwner,
                    reportData.ownerIdent,
                    reportData.serviceName,
                    reportData.reportCreated.toJavaInstant(),
                    reportData.reportEdited.toJavaInstant()
                ).asUpdate
            )

        }
    }
}