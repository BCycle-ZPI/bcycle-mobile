package pl.pwr.zpi.bcycle.mobile.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_home.*
import pl.pwr.zpi.bcycle.mobile.*
import pl.pwr.zpi.bcycle.mobile.adapters.TYPE_FUTURE
import pl.pwr.zpi.bcycle.mobile.adapters.TripAdapter
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.TripTemplate
import pl.pwr.zpi.bcycle.mobile.utils.background
import pl.pwr.zpi.bcycle.mobile.utils.showToast

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var adapter: TripAdapter<TripTemplate>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val startTrip: Button = root.findViewById(R.id.startTripBT)
        startTrip.setOnClickListener {
            startActivity(Intent(context, RecordTripActivity::class.java))
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setListeners()
        arrangeRecyclerViews()
    }

    override fun onResume() {
        super.onResume()
        if (this::adapter.isInitialized) refreshTripList()
    }

    private fun arrangeRecyclerViews() {
        adapter = TripAdapter(
            mutableListOf<TripTemplate>(),
            activity!!.applicationContext,
            this::openTripInfo
        )
        tripsRV.layoutManager = LinearLayoutManager(activity!!.applicationContext)
        tripsRV.adapter = adapter
        refreshTripList()
    }

    @SuppressLint("CheckResult")
    private fun refreshTripList() {
        adapter.clear()
        homeRefreshLayout.isRefreshing = true
        var finishedRefreshes = 0
        ApiClient.groupTripApi.getAll().background().subscribe({
            allGroupTrips = it.result
            adapter.addAll(it.result)
            finishedRefreshes += 1
            if (finishedRefreshes == 2) homeRefreshLayout.isRefreshing = false
        }, {
            showToast(R.string.prompt_cannot_data)
            homeRefreshLayout.isRefreshing = false
        })
        ApiClient.tripApi.getAll().background().subscribe({
            adapter.addAll(it.result)
            finishedRefreshes += 1
            if (finishedRefreshes == 2) homeRefreshLayout.isRefreshing = false
        }, {
            showToast(R.string.prompt_cannot_data)
            homeRefreshLayout.isRefreshing = false
        })
    }

    private fun openTripInfo(type:Int, id:Int) {
        val baseActivity = activity as BCycleBaseActivity
        if (type == TYPE_FUTURE) {
            baseActivity.openGroupTrip(id)
        } else {
            baseActivity.openPrivateTrip(id)
        }
    }

    private fun setListeners() {
        newtripBT.setOnClickListener {
            startActivity(Intent(activity!!.applicationContext, TripCreationActivity::class.java))
        }
        startTripBT.setOnClickListener { //todo
        }
        jointripBT.setOnClickListener {
            //todo
        }
        homeRefreshLayout.setOnRefreshListener { refreshTripList() }
    }
}