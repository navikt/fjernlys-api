package no.nav.fjernlys

import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource

fun createDataSource(database: Database): DataSource {
    return HikariDataSource().apply {
        dataSourceClassName = PGSimpleDataSource::class.qualifiedName
        addDataSourceProperty("serverName", database.host)
        addDataSourceProperty("portNumber", database.port)
        addDataSourceProperty("user", database.username)
        addDataSourceProperty("password", database.password)
        addDataSourceProperty("databaseName", database.name)
        addDataSourceProperty("sslCert", System.getenv("DB_SSLCERT"))
        addDataSourceProperty("sslKey", System.getenv("DB_SSLKEY"))
        addDataSourceProperty("sslMode", System.getenv("DB_SSLMODE"))
        addDataSourceProperty("sslRootCert", System.getenv("DB_SSLROOTCERT"))
        maximumPoolSize = 10
        minimumIdle = 1
        idleTimeout = 100000
        connectionTimeout = 100000
        maxLifetime = 300000
    }/*.also {
        it.metricRegistry = Metrics.appMicrometerRegistry
    }*/
}

fun getFlyway(dataSource: DataSource): Flyway = Flyway.configure().dataSource(dataSource).load()

fun runMigration(dataSource: DataSource) {
    getFlyway(dataSource).migrate()
}
