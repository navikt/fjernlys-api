package no.nav.fjernlys.dbQueries

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import javax.sql.DataSource
import kotlinx.serialization.Serializable

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
                            is_owner = row.boolean("isOwner"),
                            owner_ident = row.string("ownerIdent"),
                            service_name = row.string("serviceName"),
                            report_created = Instant.fromEpochMilliseconds(row.sqlTimestamp("report_created").time),
                            report_edited = Instant.fromEpochMilliseconds(row.sqlTimestamp("report_edited").time),
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
        SELECT * FROM risk_report WHERE service_name = :service_name
    """.trimIndent()

        return using(sessionOf(dataSource)) { session ->
            session.run(queryOf(sql, mapOf("service_name" to service_name)).map { row ->
                RiskReportData(
                    id = row.string("id"),
                    is_owner = row.boolean("is_owner"),
                    owner_ident = row.string("owner_ident"),
                    service_name = row.string("service_name"),
                    report_created = Instant.fromEpochMilliseconds(row.sqlTimestamp("report_created").time),
                    report_edited = Instant.fromEpochMilliseconds(row.sqlTimestamp("report_edited").time),
                )// Directly map to string
            }.asList)
        }
    }

    @Serializable
    data class RiskReportData(
        val id: String,
        val is_owner: Boolean,
        val owner_ident: String,
        val service_name: String,
        val report_created: Instant,
        val report_edited: Instant
    )


    data class RiskReportId(
        val id: String
    )


}