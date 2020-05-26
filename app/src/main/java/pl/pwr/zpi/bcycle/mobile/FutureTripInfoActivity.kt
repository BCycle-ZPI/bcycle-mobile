package pl.pwr.zpi.bcycle.mobile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.yarolegovich.lovelydialog.LovelyStandardDialog
import kotlinx.android.synthetic.main.activity_future_trip_info.*
import org.threeten.bp.ZonedDateTime
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.GroupTrip
import pl.pwr.zpi.bcycle.mobile.models.GroupTripParticipant
import pl.pwr.zpi.bcycle.mobile.ui.dialogs.InviteDialogFragment
import pl.pwr.zpi.bcycle.mobile.utils.background
import pl.pwr.zpi.bcycle.mobile.utils.dateToFriendlyString
import pl.pwr.zpi.bcycle.mobile.utils.showToastError

class FutureTripInfoActivity : BCycleBaseActivity(), OnMyMapReadyCallback, OnPhotosWindowClosedCallback {
    private var isEditable: Boolean = true
    private lateinit var map: GoogleMap
    private lateinit var trip: GroupTrip
    private lateinit var mapFragment: SupportMapFragment


    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_future_trip_info)
        getMaps()
        setListeners()
        val tripID = intent!!.extras.getInt(KEY_TRIP_ID)
        ApiClient.groupTripApi.get(tripID).background().subscribe({
            trip = it.result
            participantsBt.visibility = View.VISIBLE
            setFields(trip)
        }, {
            showToastError(R.string.prompt_unable_to_load_tripinfo)
        })
    }

    private fun setListeners() {
        photosBt.setOnClickListener {
            fragmentContainer.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction().replace(
                R.id.fragmentContainer, PhotosFragment(
                    this, trip.photos, this
                )
            ).commit()
        }

        participantsBt.setOnClickListener {
            fragmentContainer.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction().replace(
                R.id.fragmentContainer, ParticipantsFragment(
                    this, trip, isEditable, this
                )
            ).commit()
        }

        starttripBt.setOnClickListener {
            handleStartTrip()
        }

        inviteFAB.setOnClickListener {
            val fragment = InviteDialogFragment()
            fragment.arguments = InviteDialogFragment.prepareInviteDialog(trip)
            fragment.show(supportFragmentManager, DIALOG_INVITE)
        }
    }

    private fun handleStartTrip() {
        if (trip.aboutToStart()) {
            startTrip(trip.id)
        } else {
            val now = ZonedDateTime.now()
            val dialogTitle = if (trip.startDate < now) R.string.start_trip_past_date else R.string.start_trip_future_date
            LovelyStandardDialog(this)
            .setTopColorRes(R.color.colorAccent)
            .setTitle(dialogTitle)
            .setIcon(R.drawable.bike_icon)
            .setPositiveButton(R.string.yes) { startTrip(trip.id) }
            .setPositiveButtonColorRes(R.color.green)
            .setNegativeButton(R.string.no) {}
            .setNegativeButtonColorRes(R.color.red)
            .show()
        }
    }

    private fun getMaps() {
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.myMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setFields(trip: GroupTrip) {
        tripnameTV.text = trip.name
        startTV.text = dateToFriendlyString(trip.startDate)
        endTV.text = dateToFriendlyString(trip.endDate)
        hostTV.text = trip.host!!.displayName
        participantsTV.text = trip.formatParticipantCount(this)
        val markers = trip.route
        displayTripMarkers(markers, map)
        animateTo(markers[markers.size/2].latitude,markers[markers.size/2].longitude, map)
        isEditable = trip.startDate > ZonedDateTime.now()
        inviteFAB.visibility = if (isEditable) View.VISIBLE else View.INVISIBLE
        photosBt.visibility = if (trip.photos.isNotEmpty()) View.VISIBLE else View.GONE
    }

    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
    }

    override fun onPhotosWindowClosed() {
        fragmentContainer.visibility = View.GONE
    }

    fun onParticipantsWindowClosed(participants: List<GroupTripParticipant>) {
        trip.participants = participants
        participantsTV.text = trip.formatParticipantCount(this)
    }
}
