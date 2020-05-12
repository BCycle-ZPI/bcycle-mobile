package pl.pwr.zpi.bcycle.mobile.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_dashboard.*
import pl.pwr.zpi.bcycle.mobile.R
import pl.pwr.zpi.bcycle.mobile.RecordTripActivity
import pl.pwr.zpi.bcycle.mobile.TripCreationActivity
import pl.pwr.zpi.bcycle.mobile.adapters.TripAdapter
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.TripTemplate
import pl.pwr.zpi.bcycle.mobile.utils.background
import pl.pwr.zpi.bcycle.mobile.utils.showToast
import pl.pwr.zpi.bcycle.mobile.utils.showToastError

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

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

    @SuppressLint("CheckResult")
    private fun arrangeRecyclerViews() {
        val adapter = TripAdapter(mutableListOf<TripTemplate>(), activity!!.applicationContext)
        tripsRV.layoutManager = LinearLayoutManager(activity!!.applicationContext)
        tripsRV.adapter = adapter
        ApiClient.groupTripApi.getAll().background().subscribe({
            adapter.addAll(it.result)
            adapter.notifyDataSetChanged()
        }, {
            showToast(R.string.prompt_cannot_data)
        })
        ApiClient.tripApi.getAll().background().subscribe({
            adapter.addAll(it.result)
            adapter.notifyDataSetChanged()
        }, {
            showToast(R.string.prompt_cannot_data)
        })
    }

    private fun setListeners() {
        newtripBT.setOnClickListener {
            startActivity(Intent(activity!!.applicationContext, TripCreationActivity::class.java))
        }
    }

}