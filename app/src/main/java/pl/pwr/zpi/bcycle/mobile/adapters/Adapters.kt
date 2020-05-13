package pl.pwr.zpi.bcycle.mobile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.row_future_trip.view.*
import kotlinx.android.synthetic.main.row_history_trip.view.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import pl.pwr.zpi.bcycle.mobile.R
import pl.pwr.zpi.bcycle.mobile.models.GroupTrip
import pl.pwr.zpi.bcycle.mobile.models.Trip
import pl.pwr.zpi.bcycle.mobile.models.TripTemplate
import pl.pwr.zpi.bcycle.mobile.utils.round

private const val TYPE_FUTURE = 1
private const val TYPE_HISTORY = 2

class TripAdapter<T>(private val trips: MutableList<T>, private val context: Context) :
    RecyclerView.Adapter<TripAdapter.ViewHolder>() where T:TripTemplate {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        if (viewType == TYPE_FUTURE) return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.row_future_trip, parent, false)
        )
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.row_history_trip, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return trips.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val view = holder.itemView
        if(getItemViewType(position) == TYPE_FUTURE){
            val item = trips[position] as GroupTrip
            view.tv_start_date.text =
                item.startDate.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
            view.tv_end_date.text =
                item.endDate.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
            view.tv_tripname.text = item.name
            view.tv_participantsNum.text = item.participants?.size.toString()
            view.tv_role.text =
                if (item.host?.id == FirebaseAuth.getInstance().uid) context.resources.getString(R.string.you_host) else context.resources.getString(
                    R.string.you_participate)
        }
        else{
            val item = trips[position] as Trip
            view.tv_start.text =
                item.started.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
            view.tv_end.text =
                item.finished.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
            view.tv_duration.text = context.getString(R.string.time_format, (item.time / 60000).div(60), (item.time / 60000).rem(60))
            view.tv_road.text = context.getString(R.string.distance_format,item.distance.div(1000))
            if (item.photos.count() != 0) {
                holder.itemView.iv_photo.load(item.photos[0])
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (trips[position] is Trip) TYPE_HISTORY else TYPE_FUTURE
    }

    fun addAll(list:List<T>){
        trips.addAll(list)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}