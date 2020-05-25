package pl.pwr.zpi.bcycle.mobile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.yarolegovich.lovelydialog.LovelyInfoDialog
import com.yarolegovich.lovelydialog.LovelyStandardDialog
import kotlinx.android.synthetic.main.activity_future_trip_info.*
import org.threeten.bp.ZonedDateTime
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.GroupTrip
import pl.pwr.zpi.bcycle.mobile.ui.dialogs.InviteDialogFragment
import pl.pwr.zpi.bcycle.mobile.utils.background
import pl.pwr.zpi.bcycle.mobile.utils.showToast
import pl.pwr.zpi.bcycle.mobile.utils.showToastError

class FutureTripInfoActivity : BCycleBaseActivity(), OnMyMapReadyCallback {
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
            setFields(trip)
        }, {
            showToastError(R.string.prompt_unable_to_load_tripinfo)
        })
    }

    private fun setListeners() {
        descBT.setOnClickListener {
            LovelyInfoDialog(this)
                .setTopColorRes(R.color.colorAccent)
                .setIcon(R.drawable.bike_icon)
                .setMessage(trip.description)
                .show()
        }
        starttripBT.setOnClickListener {
            handleStartTrip()
        }
        inviteFAB.setOnClickListener {
            val fragment =
                InviteDialogFragment()
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
        val markers = trip.route
        val participantsNames = mutableListOf<String>()
        trip.participants!!.forEach { it->participantsNames.add(it.user.displayName) }
        val adapter = ArrayAdapter(this, R.layout.listview_row, participantsNames)
        participantsLV.adapter = adapter
        displayTripMarkers(markers, map)
        animateTo(markers[markers.size/2].latitude,markers[markers.size/2].longitude, map)
        if (trip.startDate > ZonedDateTime.now()) {
            inviteFAB.visibility = View.INVISIBLE
        }
    }

    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
    }
}
