package pl.pwr.zpi.bcycle.mobile.models

data class UserStats(
    val tripCount: Int,
    val groupTripTotalCount: Int,
    val groupTripHostingCount: Int,
    val totalKilometers: Double,
    val totalTimeMinutes: Double
)
