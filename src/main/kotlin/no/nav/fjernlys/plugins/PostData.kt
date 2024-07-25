package no.nav.fjernlys.plugins

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.Integers

@Serializable
data class MeasureValue(
    val category: String,
    val status: String,
)

@Serializable
data class MeasureValueOut(
    val id: String?,
    val riskAssessmentId: String?,
    val category: String,
    val status: String,
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
    val reportEdited: Instant,
)

@Serializable
data class RiskAssessmentData(
    val id: String,
    val reportId: String,
    val probability: Double,
    val consequence: Double,
    val dependent: Boolean,
    val riskLevel: String,
    val category: String,
    val newConsequence: Double?,
    val newProbability: Double?,
)

@Serializable
data class RiskLevelData(
    val serviceName: String,
    val high: Int,
    val moderate: Int,
    val low: Int
)

@Serializable
data class RiskLevelCounts(
    var high: Int = 0,
    var moderate: Int = 0,
    var low: Int = 0
)
