package no.nav.fjernlys.plugins
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


@Serializable
data class MeasureValue(
    val category: String,
    val status: String,
)

@Serializable
data class MeasureValueOut(
    val id: String?,
    val risk_assessment_id: String?,
    val category: String,
    val status: String,
)

@Serializable
data class RiskValueOut(
    val id: String,
    val probability: Double,
    val consequence: Double,
    val dependent: Boolean,
    val riskLevel: String,
    val category: String,
    val measureValues: List<MeasureValueOut>?,
    val newConsequence: Double? = null,
    val newProbability: Double? = null
)

@Serializable
data class RiskValue(
    val probability: Double,
    val consequence: Double,
    val dependent: Boolean,
    val riskLevel: String,
    val category: String,
    val measureValues: List<MeasureValue>? = listOf(),
    val newConsequence: Double? = null,
    val newProbability: Double? = null
)

@Serializable
data class IncomingData(
    val ownerData: Boolean,
    val notOwnerData: String,
    val serviceData: String,
    val riskValues: List<RiskValue>
)

@Serializable
data class OutgoingData(
    val id: String,
    val is_owner: Boolean,
    val owner_ident: String,
    val service_name: String,
    val risk_values: List<RiskValueOut>?,
    val report_created: Instant,
    val report_edited: Instant
)

@Serializable
data class RiskReportData(
    val id: String,
    val is_owner: Boolean,
    val owner_ident: String,
    val service_name: String,
    val report_created: Instant,
    val report_edited: Instant
)