package no.nav.fjernlys.plugins
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


//@Serializable
//data class MeasureValue(
//    val category: String,
//    val status: String,
//)

@Serializable
data class MeasureValueOut(
    val id: String?,
    val riskAssessmentId: String?,
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
    val isOwner: Boolean,
    val ownerIdent: String,
    val serviceName: String,
    val riskValues: List<RiskValueOut>?,
    val reportCreated: Instant,
    val reportEdited: Instant
)

@Serializable
data class RiskReportData(
    val id: String,
    val isOwner: Boolean,
    val ownerIdent: String,
    val serviceName: String,
    val reportCreated: Instant,
    val reportEdited: Instant
)

data class RiskMeasureData(
    val id: String,
    val riskAssessmentId: String?,
    val measureCategory: String,
    val measureStatus: String,

    )

@Serializable
data class MeasureValue(
    val id: String,
    val riskAssessmentId: String,
    val category: String,
    val status: String
)
