package testcontainers

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.fjernlys.runMigration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy

//TestContainer
class PostgrestContainerHelper(
    network: Network = Network.newNetwork(),
    log: Logger = LoggerFactory.getLogger(PostgrestContainerHelper::class.java)
) {
    private val postgresNetworkAlias = "postgrescontainer"
    private val databaseName = "fjernlys-container-db"
    private var mitegationIsDone = false
    val postgresContainer: PostgreSQLContainer<*> =
        PostgreSQLContainer("postgres:15")
            .withLogConsumer(
                Slf4jLogConsumer(log).withPrefix(postgresNetworkAlias).withSeparateOutputStreams()
            )
            .withNetwork(network)
            .withNetworkAliases(postgresNetworkAlias)
            .withDatabaseName(databaseName)
            .withCreateContainerCmdModifier { cmd -> cmd.withName("$postgresNetworkAlias-${System.currentTimeMillis()}") }
            .waitingFor(HostPortWaitStrategy()).apply {
                start()
            }

    val dataSource = newDataSource()

    fun newDataSource() =
        HikariDataSource(HikariConfig().apply {
            jdbcUrl = postgresContainer.jdbcUrl
            username = postgresContainer.username
            password = postgresContainer.password
        }).also {
            if (!mitegationIsDone) {
                runMigration(it)
                mitegationIsDone = true
            }
        }
}
