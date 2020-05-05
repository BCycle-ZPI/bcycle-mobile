package pl.pwr.zpi.bcycle.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.row_history_trip.view.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.GroupTrip
import pl.pwr.zpi.bcycle.mobile.models.Trip
import pl.pwr.zpi.bcycle.mobile.models.UserInfo
import pl.pwr.zpi.bcycle.mobile.utils.background
import pl.pwr.zpi.bcycle.mobile.utils.showToast
import pl.pwr.zpi.bcycle.mobile.utils.showToastError
import java.time.ZonedDateTime
import kotlin.math.round

class DashboardActivity : BCycleNavigationDrawerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        setListeners()
        arrangeRecyclerViews()
    }

    private fun arrangeRecyclerViews() {
        val adapter = HistoryTripAdapter(mutableListOf<Trip>(), R.layout.row_history_trip)
        rv_trips.layoutManager = LinearLayoutManager(this)
        rv_trips.adapter = adapter
        ApiClient.tripApi.getAll().background().subscribe({
            adapter.trips.addAll(it.result)
            adapter.notifyDataSetChanged()
            showToast("loaded")

        }, {
            showToastError(R.string.prompt_cannot_data)
        })

        rv_future_trips.layoutManager = LinearLayoutManager(this)
        val myTrips = mutableListOf<GroupTrip>()
        myTrips.add(
            GroupTrip(
                1,
                "2",
                "2",
                UserInfo("aa", "aa", "aa<", "A"),
                "A",
                org.threeten.bp.ZonedDateTime.now(),
                org.threeten.bp.ZonedDateTime.now(),
                mutableListOf(),
                null
            )
        )
        myTrips.add(
            GroupTrip(
                1,
                "2",
                "2",
                UserInfo("aa", "aa", "aa<", "A"),
                "A",
                org.threeten.bp.ZonedDateTime.now(),
                org.threeten.bp.ZonedDateTime.now(),
                mutableListOf(),
                null
            )
        )
        val adapterFutureTrips =
            HistoryTripAdapter.FutureTripAdapter(myTrips, R.layout.row_future_trip)
        rv_future_trips.adapter = adapterFutureTrips
       /* ApiClient.groupTripApi.getAll().background().subscribe({
            adapterFutureTrips.futureTrips.addAll(it.result)
            adapterFutureTrips.notifyDataSetChanged()
            showToast(it.result.size)
        }, {
            showToast(it.message!!)
        })*/
    }

    private fun setListeners() {
        bt_newtrip.setOnClickListener {
            //todo
        }
        bt_grouptrips.setOnClickListener {
            //TODO
        }
    }
}

open class HistoryTripAdapter<T>(val trips: MutableList<T>, val row_xml_id:Int) :
    RecyclerView.Adapter<HistoryTripAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(row_xml_id, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return trips.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = trips[position] as Trip
        //same day
        if (item.started.format(DateTimeFormatter.BASIC_ISO_DATE) == item.finished.format(
                DateTimeFormatter.BASIC_ISO_DATE
            )
        ) {
            holder.itemView.tv_date.text =
                item.started.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
            holder.itemView.tv_hours.text =
                "${item.started.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))} - ${item.finished.format(
                    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                )}"
        }
        //when trip is planned for at least 2 days
        else {
            holder.itemView.tv_date.text =
                "${item.started.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))} - ${item.finished.format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                )}"
            holder.itemView.tv_hours.text =
                "${item.started.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))} - ${item.finished.format(
                    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                )}"
        }
        holder.itemView.tv_duration.text =
            item.time.div(3600 * 60).toString() + "h " + item.time.rem(3600 * 60).toString() + "min"
        holder.itemView.tv_road.text = item.distance.div(1000).round(2).toString() + "km"
        if (item.photos.count() != 0) {
            holder.itemView.iv_photo.load(item.photos[0])
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    class FutureTripAdapter<T>(val futureTrips: MutableList<T>, val trip_row_xml_id:Int) :
        HistoryTripAdapter<T>(futureTrips, trip_row_xml_id) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {}
    }
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}
