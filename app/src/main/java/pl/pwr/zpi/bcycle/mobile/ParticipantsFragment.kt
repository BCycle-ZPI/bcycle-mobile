package pl.pwr.zpi.bcycle.mobile

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_participants.*
import pl.pwr.zpi.bcycle.mobile.adapters.ParticipantAdapter
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.GroupTrip
import pl.pwr.zpi.bcycle.mobile.models.GroupTripParticipant
import pl.pwr.zpi.bcycle.mobile.models.ParticipantStatus
import pl.pwr.zpi.bcycle.mobile.utils.background
import java.lang.RuntimeException

class ParticipantsFragment(private val con: Context, private val trip: GroupTrip, private val isEditable: Boolean, private val parentActivity: FutureTripInfoActivity) : Fragment() {
    private var participants: MutableList<GroupTripParticipant> = mutableListOf()
    private var isHost: Boolean = false
    private lateinit var adapter: ParticipantAdapter

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_participants, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        participants = trip.participants!!.filter {
            it.status == ParticipantStatus.ACCEPTED || it.status == ParticipantStatus.PENDING
        }.toMutableList()
        isHost = auth.currentUser?.uid == trip.host!!.id
        participantsoffBT.setOnClickListener {
            parentActivity.onParticipantsWindowClosed(participants)
            parentFragmentManager.beginTransaction().remove(this).commit()
        }
        arrangeRecyclerViews()
    }

    private fun arrangeRecyclerViews() {
        adapter = ParticipantAdapter(
            trip,
            con,
            isEditable && isHost,
            this
        )
        adapter.setWithListContents(participants)
        participantsRV.layoutManager = LinearLayoutManager(activity!!.applicationContext)
        participantsRV.adapter = adapter
    }

    @SuppressLint("CheckResult")
    fun acceptParticipant(
        listPosition: Int,
        participant: GroupTripParticipant
    ) {
        startOperation(listPosition, participant)
        ApiClient.groupTripApi.acceptParticipant(trip.id!!, participant.user.id).background().subscribe({
            val newParticipant = participant.settingStatus(ParticipantStatus.ACCEPTED)
            participants[listPosition] = newParticipant
            finishOperation()
        }, this::showError)
    }

    @SuppressLint("CheckResult")
    fun rejectParticipant(
        listPosition: Int,
        participant: GroupTripParticipant
    ) {
        startOperation(listPosition, participant)
        ApiClient.groupTripApi.rejectParticipant(trip.id!!, participant.user.id).background().subscribe({
            participants.removeAt(listPosition)
            finishOperation()
        }, this::showError)
    }

    @SuppressLint("CheckResult")
    fun removeParticipant(
        listPosition: Int,
        participant: GroupTripParticipant
    ) {
        startOperation(listPosition, participant)
        ApiClient.groupTripApi.removeParticipant(trip.id!!, participant.user.id).background().subscribe({
            participants.removeAt(listPosition)
            finishOperation()
        }, this::showError)
    }

    private fun startOperation(listPosition: Int, participant: GroupTripParticipant) {
        if (participants[listPosition] != participant) {
            throw RuntimeException("Inconsistent participant list state")
        }
        participantsSpinner.visibility = View.VISIBLE
        adapter.disableButtons()
    }

    private fun finishOperation() {
        adapter.setWithListContents(participants)
        participantsSpinner.visibility = View.GONE
        adapter.enableButtons()
    }

    private fun showError(err: Throwable) {
        finishOperation()
        AlertDialog.Builder(con).setTitle(R.string.participants_cannot_modify)
            .setMessage(err.localizedMessage)
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .show()
    }
}
