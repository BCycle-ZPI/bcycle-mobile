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
import kotlinx.android.synthetic.main.row_history_trip.view.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.Trip
import pl.pwr.zpi.bcycle.mobile.utils.background
import pl.pwr.zpi.bcycle.mobile.utils.showToastError
import kotlin.math.round

class DashboardActivity : BCycleNavigationDrawerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        //updateNavigationDrawerHeader(FirebaseAuth.getInstance().currentUser!!)
        setListeners()
        val adapter = HistoryTripAdapter(mutableListOf())
        rv_trips.layoutManager = LinearLayoutManager(this)
        rv_trips.adapter = adapter
        ApiClient.tripApi.getAll().background().subscribe({
            adapter.trips.addAll(it.result)
            adapter.notifyDataSetChanged()
        }, {
            showToastError(R.string.prompt_cannot_data)
        })
    }

    private fun setListeners(){
        bt_newtrip.setOnClickListener{
            //todo
        }
        bt_grouptrips.setOnClickListener{
            //TODO
        }
    }
}

class HistoryTripAdapter(val trips: MutableList<Trip>) :
    RecyclerView.Adapter<HistoryTripAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.row_history_trip, parent, false);
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return trips.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = trips[position]
        //same day
        if (item.started.format(DateTimeFormatter.BASIC_ISO_DATE) == item.finished.format(
                DateTimeFormatter.BASIC_ISO_DATE)) {
            holder.itemView.tv_date.text = item.started.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
            holder.itemView.tv_hours.text = "${item.started.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))} - ${item.finished.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))}"
        }
        else{
            holder.itemView.tv_date.text =
                "${item.started.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))} - ${item.finished.format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                )}"
            holder.itemView.tv_hours.text = "${item.started.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))} - ${item.finished.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))}"
        }
        holder.itemView.tv_duration.text = item.time.div(3600*60).toString() + "h " + item.time.rem(3600*60).toString() + "min"
        holder.itemView.tv_road.text = item.distance.div(1000).round(2).toString() + "km"
        if (item.photos.count() != 0) {
            holder.itemView.iv_photo.load(item.photos[0])
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}
fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}
