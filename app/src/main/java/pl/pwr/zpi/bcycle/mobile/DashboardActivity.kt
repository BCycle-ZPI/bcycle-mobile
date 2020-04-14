package pl.pwr.zpi.bcycle.mobile

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.row_history_trip.view.*
import pl.pwr.zpi.bcycle.mobile.models.Trip
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.TemporalUnit
import java.util.*

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        val calendar = Calendar.getInstance()
        val tripList =
            mutableListOf(Trip(1.0f, calendar.time, calendar.time,""), Trip(2.9f, calendar.time, calendar.time, ""))
        rv_trips.layoutManager = LinearLayoutManager(this)
        rv_trips.adapter = HistoryTripAdapter(tripList)
    }
}

class HistoryTripAdapter(val trips:List<Trip>) : RecyclerView.Adapter<HistoryTripAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryTripAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_history_trip, parent, false);
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return trips.size
    }

    override fun onBindViewHolder(holder: HistoryTripAdapter.ViewHolder, position: Int) {
        val item = trips[position]
        holder.itemView.tv_date.text = item.started.toString()
        holder.itemView.tv_duration.text = "2h12min"/// = Duration.between(item.finished.toLocalDate(), item.started.toLocalDate()).toString()
        holder.itemView.tv_hours.text = item.finished.toString()
        holder.itemView.tv_road.text = item.distance.toString()
        //holder.itemView.iv_photo.load(item.mapImagrUrl)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}
