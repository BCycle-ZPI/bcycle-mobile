package pl.pwr.zpi.bcycle.mobile.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_participant.view.*
import pl.pwr.zpi.bcycle.mobile.ParticipantsFragment
import pl.pwr.zpi.bcycle.mobile.R
import pl.pwr.zpi.bcycle.mobile.models.GroupTrip
import pl.pwr.zpi.bcycle.mobile.models.GroupTripParticipant
import pl.pwr.zpi.bcycle.mobile.models.ParticipantStatus
import pl.pwr.zpi.bcycle.mobile.models.UserInfo


class ParticipantAdapter(private val trip: GroupTrip, private val context: Context, val canEdit: Boolean, val fragment: ParticipantsFragment):
    RecyclerView.Adapter<ParticipantAdapter.ViewHolder>() {
    private val participants = mutableListOf<GroupTripParticipant>()
    private var buttonsEnabled = true
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_participant, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return participants.size + 1
    }

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val view = holder.itemView
        if (position == 0) {
            // special case for host
            displayParticipantInBox(trip.host!!, view)
            view.descriptionTV.visibility = View.VISIBLE
            view.descriptionTV.setText(R.string.participant_host)
            view.participantButtons.visibility = View.GONE
        } else {
            val listPosition = position - 1
            val participant = participants[listPosition]
            displayParticipantInBox(participant.user, view)
            view.descriptionTV.visibility = showIf(participant.status == ParticipantStatus.PENDING)
            if (participant.status == ParticipantStatus.PENDING) {
                view.descriptionTV.setText(R.string.participant_pending)
            }
            view.participantButtons.visibility = showIf(canEdit)
            view.removeBt.visibility = showIf(participant.status == ParticipantStatus.ACCEPTED)
            view.acceptBt.visibility = showIf(participant.status == ParticipantStatus.PENDING)
            view.rejectBt.visibility = showIf(participant.status == ParticipantStatus.PENDING)
            view.removeBt.isEnabled = buttonsEnabled
            view.acceptBt.isEnabled = buttonsEnabled
            view.rejectBt.isEnabled = buttonsEnabled
            view.removeBt.setOnClickListener { fragment.removeParticipant(listPosition, participant) }
            view.acceptBt.setOnClickListener { fragment.acceptParticipant(listPosition, participant) }
            view.rejectBt.setOnClickListener { fragment.rejectParticipant(listPosition, participant) }
        }
    }

    private fun displayParticipantInBox(participant: UserInfo, view: View) {
        view.nameTV.text = participant.displayName
        storage.getReferenceFromUrl(participant.photoUrl).downloadUrl
            .addOnSuccessListener { Picasso.get().load(it).into(view.avatarIV) }
    }

    fun enableButtons() {
        buttonsEnabled = true
        notifyDataSetChanged()
    }

    fun disableButtons() {
        buttonsEnabled = false
        notifyDataSetChanged()
    }

    fun showIf(rule: Boolean) = if (rule) View.VISIBLE else View.GONE

    fun setWithListContents(list: List<GroupTripParticipant>) {
        participants.clear()
        participants.addAll(list)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

