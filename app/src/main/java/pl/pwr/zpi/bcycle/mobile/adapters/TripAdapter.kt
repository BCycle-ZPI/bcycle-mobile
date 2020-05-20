package pl.pwr.zpi.bcycle.mobile.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.row_future_trip.view.*
import kotlinx.android.synthetic.main.row_history_trip.view.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import pl.pwr.zpi.bcycle.mobile.FutureTripInfoActivity
import pl.pwr.zpi.bcycle.mobile.HistoryTripInfoActivity
import pl.pwr.zpi.bcycle.mobile.KEY_TRIP_ID
import pl.pwr.zpi.bcycle.mobile.R
import pl.pwr.zpi.bcycle.mobile.models.GroupTrip
import pl.pwr.zpi.bcycle.mobile.models.Trip
import pl.pwr.zpi.bcycle.mobile.models.TripTemplate

private const val TYPE_FUTURE = 1
private const val TYPE_HISTORY = 2

class TripAdapter<T>(private val trips: MutableList<T>, private val context: Context) :
    RecyclerView.Adapter<TripAdapter.ViewHolder>() where T : TripTemplate {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = if (viewType == TYPE_FUTURE) {
            LayoutInflater.from(parent.context).inflate(R.layout.row_future_trip, parent, false)
        } else {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_history_trip, parent, false)

        }
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return trips.size
    }

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val view = holder.itemView
        if (getItemViewType(position) == TYPE_FUTURE) {
            val item = trips[position] as GroupTrip
            setListener(view, item.id!!,TYPE_FUTURE)
            view.startdateTV.text =
                item.startDate.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
            view.enddateTV.text =
                item.endDate.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
            view.tripnameTV.text = item.name
            view.participNumTV.text = item.participants?.size.toString()
            view.roleTV.text =
                if (item.host?.id == FirebaseAuth.getInstance().uid) context.resources.getString(R.string.you_host) else context.resources.getString(
                    R.string.you_participate
                )
        } else {
            val item = trips[position] as Trip
            setListener(view, item.id!!, TYPE_HISTORY)//todo wtf
            view.startTV.text =
                item.started.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
            view.endTV.text =
                item.finished.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
            view.durationTV.text = context.getString(
                R.string.time_format,
                (item.time / 60000).div(60),
                (item.time / 60000).rem(60)
            )
            view.roadTV.text = context.getString(R.string.distance_format, item.distance.div(1000))
            if (item.photos.count() != 0) {
                Picasso.get().load(item.photos[0]).transform(RoundedCornersTransformation(5,5)).into(holder.itemView.photoIV)
            }
        }
    }

    private fun setListener(v: View, id:Int, viewType:Int) {
        v.setOnClickListener { openTripInfo(viewType, id) }
    }

    private fun openTripInfo(type:Int, id:Int) {
        val intent:Intent?
        if(type== TYPE_FUTURE) {
            intent = Intent(
                context,
                FutureTripInfoActivity::class.java
            )
            intent.putExtra(KEY_TRIP_ID, id)

        }
        else{
            intent = Intent(
                context,
                HistoryTripInfoActivity::class.java
            )
            intent.putExtra(KEY_TRIP_ID, id)

        }
        context.startActivity(
           intent
        )

    }

    override fun getItemViewType(position: Int): Int {
        return if (trips[position] is Trip) TYPE_HISTORY else TYPE_FUTURE
    }

    fun addAll(list: List<T>) {
        trips.addAll(list)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

