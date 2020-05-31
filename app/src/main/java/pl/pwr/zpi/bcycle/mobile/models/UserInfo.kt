package pl.pwr.zpi.bcycle.mobile.models

data class UserInfo(
    val id: String,
    val displayName: String,
    val email: String,
    val photoUrl: String
) : java.io.Serializable
