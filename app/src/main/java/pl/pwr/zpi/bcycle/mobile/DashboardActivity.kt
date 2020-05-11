package pl.pwr.zpi.bcycle.mobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.row_future_trip.view.*
import kotlinx.android.synthetic.main.row_history_trip.view.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.GroupTrip
import pl.pwr.zpi.bcycle.mobile.models.Trip
import pl.pwr.zpi.bcycle.mobile.utils.background
import pl.pwr.zpi.bcycle.mobile.utils.showToastError
import kotlin.math.round

class DashboardActivity : BCycleNavigationDrawerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        setListeners()
        arrangeRecyclerViews()
    }

    private fun arrangeRecyclerViews() {
        val adapter = HistoryTripAdapter(mutableListOf<Trip>())
        rv_trips.layoutManager = LinearLayoutManager(this)
        rv_trips.adapter = adapter
        ApiClient.tripApi.getAll().background().subscribe({
            adapter.trips.addAll(it.result)
            adapter.notifyDataSetChanged()
        }, {
            showToastError(R.string.prompt_cannot_data)
        })

        rv_future_trips.layoutManager = LinearLayoutManager(this)
        val adapterFutureTrips =
            HistoryTripAdapter.FutureTripAdapter(this, mutableListOf<GroupTrip>())
        rv_future_trips.adapter = adapterFutureTrips
        ApiClient.groupTripApi.getAll().background().subscribe({
            adapterFutureTrips.futureTrips.addAll(it.result)
            adapterFutureTrips.notifyDataSetChanged()
        }, {
            showToastError(R.string.prompt_cannot_data)
        })
    }

    private fun setListeners() {
        bt_newtrip.setOnClickListener {
            startActivity(Intent(this, TripCreationActivity::class.java))
        }
    }
}

open class HistoryTripAdapter<T>(val trips: MutableList<T>) :
    RecyclerView.Adapter<HistoryTripAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_history_trip,
                parent,
                false
            )
        )

    override fun getItemCount(): Int {
        return trips.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = trips[position] as Trip
        holder.itemView.tv_start.text =
            "${item.started.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))}"
        holder.itemView.tv_end.text =
            "${item.finished.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))}"
        holder.itemView.tv_duration.text =
            (item.time / 60000).div(60).toString() + "h " + (item.time / 60000).rem(60).toString() + "min"
        holder.itemView.tv_road.text = item.distance.div(1000).round(2).toString() + "km"
        if (item.photos.count() != 0) {
            holder.itemView.iv_photo.load(item.photos[0])
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class FutureTripAdapter<T>(val context: Context, val futureTrips: MutableList<T>) :
        RecyclerView.Adapter<HistoryTripAdapter.ViewHolder>() {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = futureTrips[position] as GroupTrip
            val view = holder.itemView
            view.tv_start_date.text =
                item.startDate.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))

            view.tv_end_date.text =
                item.endDate.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
            view.tv_tripname.text = item.name
            view.tv_participantsNum.text = item.participants?.size.toString()
            view.tv_role.text =
                if (item.host.id == FirebaseAuth.getInstance().uid) context.resources.getString(R.string.you_host) else context.resources.getString(
                    R.string.you_participate
                )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_future_trip,
                parent,
                false
            )
        )

        override fun getItemCount(): Int = futureTrips.size
    }
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}
