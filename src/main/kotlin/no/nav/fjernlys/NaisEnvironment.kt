package no.nav.fjernlys

import java.net.URL

class NaisEnvironment(
    val database: Database = Database(),
    val security: Security = Security(),

    cluster: String = getEnvVar("NAIS_CLUSTER_NAME")
) {
    companion object {
        enum class Environment {
            `PROD-GCP`, `DEV-GCP`, LOKAL
        }

        fun hentMiljø(cluster: String) =
            Environment.entries.find { it.name.lowercase() == cluster }
                ?: throw IllegalStateException("Ukjent miljø $cluster")
    }

    val miljø = hentMiljø(cluster)
}

class Database(
    val host: String = getEnvVar("NAIS_DATABASE_FJERNLYS_API_DB_HOST"),
    val port: String = getEnvVar("NAIS_DATABASE_FJERNLYS_API_DB_PORT"),
    val username: String = getEnvVar("NAIS_DATABASE_FJERNLYS_API_DB_USERNAME"),
    val password: String = getEnvVar("NAIS_DATABASE_FJERNLYS_API_DB_PASSWORD"),
    val name: String = getEnvVar("NAIS_DATABASE_FJERNLYS_API_DB_DATABASE")
)

class Security(
    val azureConfig: AzureConfig = AzureConfig(),
    val adGrupper: ADGrupper = ADGrupper()
) {
    companion object {
        const val NAV_IDENT_CLAIM = "NAVident"
        const val GROUPS_CLAIM = "groups"
        const val NAME_CLAIM = "name"
        const val OBJECT_ID_CLAIM = "oid"
    }
}

class AzureConfig(
    val clientId: String = getEnvVar("AZURE_APP_CLIENT_ID"),
    val clientSecret: String = getEnvVar("AZURE_APP_CLIENT_SECRET"),
    val jwksUri: URL = URL(getEnvVar("AZURE_OPENID_CONFIG_JWKS_URI")),
    val issuer: String = getEnvVar("AZURE_OPENID_CONFIG_ISSUER"),
    val tokenEndpoint: String = getEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
    val privateJwk: String = getEnvVar("AZURE_APP_JWK"),
    val graphDatabaseUrl: String = getEnvVar("AZURE_GRAPH_URL", "https://graph.microsoft.com/beta")
) {


    override fun toString() =
        "AzureConfig(audience='$clientId', jwksUri=$jwksUri, issuer='$issuer', tokenEndpoint='$tokenEndpoint')"
}

class ADGrupper(
    val adminGroup: String = getEnvVar("FJERNLYS_ADMINGROUP_GROUP_ID"),

)




/*
    private fun securityConfigs() =
        mapOf(
            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SSL",
            SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG to "",
            SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG to "JKS",
            SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to "PKCS12",
            SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to truststoreLocation,
            SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to credstorePassword,
            SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to keystoreLocation,
            SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to credstorePassword,
            SslConfigs.SSL_KEY_PASSWORD_CONFIG to credstorePassword
        )*/








fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable $varName")
