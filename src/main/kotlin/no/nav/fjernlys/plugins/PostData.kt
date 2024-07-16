package no.nav.fjernlys.plugins
import kotlinx.serialization.Serializable


@Serializable
data class PostData(val name: String)


//@Serializable
//data class PostData(
//    val ownerData: Boolean,
//    val notOwnerData: String,
//    val serviceData: String,
//    val riskValues: List<RiskValue>
//)
//
//@Serializable
//data class RiskValue(
//    val probability: Int,
//    val consequence: Int,
//    val dependent: Boolean,
//    val riskLevel: String,
//    val category: String,
//    val measureValues: List<MeasureValue>,
//    val newConsequence: String,
//    val newProbability: String
//)
//
//@Serializable
//data class MeasureValue(
//    val category: String,
//    val status: String,
//    val started: Boolean
//)