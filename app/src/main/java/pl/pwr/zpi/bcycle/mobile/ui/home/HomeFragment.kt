package pl.pwr.zpi.bcycle.mobile.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.yarolegovich.lovelydialog.LovelyChoiceDialog
import kotlinx.android.synthetic.main.fragment_home.*
import pl.pwr.zpi.bcycle.mobile.BCycleBaseActivity
import pl.pwr.zpi.bcycle.mobile.R
import pl.pwr.zpi.bcycle.mobile.TripCreationActivity
import pl.pwr.zpi.bcycle.mobile.adapters.TYPE_FUTURE
import pl.pwr.zpi.bcycle.mobile.adapters.TripAdapter
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.GroupTrip
import pl.pwr.zpi.bcycle.mobile.models.TripTemplate
import pl.pwr.zpi.bcycle.mobile.utils.background
import pl.pwr.zpi.bcycle.mobile.utils.showToast
import java.util.concurrent.atomic.AtomicBoolean

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var adapter: TripAdapter<TripTemplate>
    private var allGroupTrips: List<GroupTrip>? = null
    private val waitingToStart = AtomicBoolean(false)

    private val baseActivity
        get() = activity as BCycleBaseActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setListeners()
        arrangeRecyclerViews()
    }

    override fun onResume() {
        super.onResume()
        if (this::adapter.isInitialized && !homeRefreshLayout.isRefreshing) refreshTripList()
    }

    private fun arrangeRecyclerViews() {
        adapter = TripAdapter(
            mutableListOf<TripTemplate>(),
            activity!!.applicationContext,
            this::openTripInfo
        )
        tripsRV.layoutManager = LinearLayoutManager(activity!!.applicationContext)
        tripsRV.adapter = adapter
        if (!homeRefreshLayout.isRefreshing) refreshTripList()
    }

    @SuppressLint("CheckResult")
    private fun refreshTripList() {
        adapter.clear()
        homeRefreshLayout.isRefreshing = true
        val output = mutableListOf<TripTemplate>()
        var finishedRefreshes = 0
        ApiClient.groupTripApi.getAll().background().subscribe({
            allGroupTrips = it.result
            synchronized(output) {
                output.addAll(it.result)
                adapter.setWithListContents(output)
                finishedRefreshes += 1
                if (finishedRefreshes == 2) finishRefresh()
            }
        }, {
            showToast(R.string.prompt_cannot_data)
            homeRefreshLayout.isRefreshing = false
        })
        ApiClient.tripApi.getAll().background().subscribe({
            synchronized(output) {
                output.addAll(it.result)
                adapter.setWithListContents(output)
                finishedRefreshes += 1
                if (finishedRefreshes == 2) finishRefresh()
            }
        }, {
            showToast(R.string.prompt_cannot_data)
            homeRefreshLayout.isRefreshing = false
        })
    }

    fun finishRefresh() {
        homeRefreshLayout.isRefreshing = false
        if (waitingToStart.compareAndSet(true, false)) {
            startTripWithGroupSelection()
        }
    }

    private fun openTripInfo(type:Int, id:Int) {
        if (type == TYPE_FUTURE) {
            baseActivity.openGroupTrip(id)
        } else {
            baseActivity.openPrivateTrip(id)
        }
    }

    private fun handleStartTripButton() {
        if (allGroupTrips == null) {
            waitingToStart.set(true)
            startTripBT.isEnabled = false
        } else {
            startTripWithGroupSelection()
        }
    }

    private fun startTripWithGroupSelection() {
        startTripBT.isEnabled = true
        waitingToStart.set(false)
        val upcomingGroupTrips = getUpcomingGroupTrips()
        if (upcomingGroupTrips.isEmpty()) {
            baseActivity.startTrip()
        } else {
            val itemsList = upcomingGroupTrips.map { gt -> gt.name }
            val customItems = listOf(getString(R.string.private_trip_option), getString(R.string.cancel_option))
            val itemsArray = (itemsList + customItems).toTypedArray()
            val idByIndex = upcomingGroupTrips.map { gt -> gt.id!! }
            val privateIndex = itemsArray.size - 2
            val cancelIndex = itemsArray.size - 1
        LovelyChoiceDialog(context)
            .setTopColorRes(R.color.colorAccent)
            .setTitle(resources.getString(R.string.select_group_trip_to_join))
            .setIcon(R.drawable.bike_icon)
            .setItems(itemsArray) { pos: Int, _: String ->
                if (pos == privateIndex) {
                    baseActivity.startTrip()
                } else if (pos != cancelIndex) {
                    baseActivity.startTrip(idByIndex[pos])
                }
            }.show()
        }
    }

    private fun getUpcomingGroupTrips(): List<GroupTrip> {
        if (allGroupTrips == null) return listOf()
        return allGroupTrips!!.filter { it.aboutToStart() }
    }

    private fun setListeners() {
        newtripBT.setOnClickListener {
            startActivity(Intent(activity!!.applicationContext, TripCreationActivity::class.java))
        }
        startTripBT.setOnClickListener { handleStartTripButton() }
        jointripBT.setOnClickListener {
            //todo
        }
        homeRefreshLayout.setOnRefreshListener { refreshTripList() }
    }
}