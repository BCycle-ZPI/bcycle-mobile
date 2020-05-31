package pl.pwr.zpi.bcycle.mobile.models

data class GroupTripPoint(
    val latitude: Double,
    val longitude: Double,
    val ordinal: Int?
) :TripPointTemplate(), java.io.Serializable {
    override val lat: Double
        get() = latitude

    override val lng: Double
        get() = longitude
}
