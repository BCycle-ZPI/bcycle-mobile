package pl.pwr.zpi.bcycle.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.row_history_trip.view.*
import org.threeten.bp.format.DateTimeFormatter
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.Trip
import pl.pwr.zpi.bcycle.mobile.utils.background
import pl.pwr.zpi.bcycle.mobile.utils.showToastError

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
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
        if (item.started.format(DateTimeFormatter.ISO_LOCAL_DATE) == item.finished.format(
                DateTimeFormatter.ISO_LOCAL_DATE)) {
            holder.itemView.tv_date.text = item.started.format(DateTimeFormatter.ISO_LOCAL_DATE)
            holder.itemView.tv_hours.text = item.started.hour.toString() + "-" + item.finished.hour.toString()
        }
        else{
            holder.itemView.tv_date.text = item.started.format(DateTimeFormatter.ISO_LOCAL_DATE) + '-' + item.finished.format(DateTimeFormatter.ISO_LOCAL_DATE)
            holder.itemView.tv_hours.text = ""
        }
        holder.itemView.tv_duration.text = item.time.toString()
        holder.itemView.tv_road.text = item.distance.toString()
        if (item.photos.count() != 0) {
            holder.itemView.iv_photo.load(item.photos[0])
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}
