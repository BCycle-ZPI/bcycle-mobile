package pl.pwr.zpi.bcycle.mobile.models

data class GroupTripParticipant(
    val user: UserInfo,
    val status: ParticipantStatus
) : java.io.Serializable {
    fun settingStatus(newStatus: ParticipantStatus) = GroupTripParticipant(user, newStatus)
}
