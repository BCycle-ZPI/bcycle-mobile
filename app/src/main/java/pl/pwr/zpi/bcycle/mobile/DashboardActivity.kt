package pl.pwr.zpi.bcycle.mobile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_dashboard.*
import pl.pwr.zpi.bcycle.mobile.adapters.TripAdapter
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.TripTemplate
import pl.pwr.zpi.bcycle.mobile.utils.background
import pl.pwr.zpi.bcycle.mobile.utils.showToastError

class DashboardActivity : BCycleNavigationDrawerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        setListeners()
        arrangeRecyclerViews()
    }

    @SuppressLint("CheckResult")
    private fun arrangeRecyclerViews() {
        val adapter = TripAdapter(mutableListOf<TripTemplate>(), this)
        tripsRV.layoutManager = LinearLayoutManager(this)
        tripsRV.adapter = adapter
        ApiClient.groupTripApi.getAll().background().subscribe({
            adapter.addAll(it.result)
            adapter.notifyDataSetChanged()
        }, {
            showToastError(R.string.prompt_cannot_data)
        })
        ApiClient.tripApi.getAll().background().subscribe({
            adapter.addAll(it.result)
            adapter.notifyDataSetChanged()
        }, {
            showToastError(R.string.prompt_cannot_data)
        })
    }

    private fun setListeners() {
        newtripBT.setOnClickListener {
            startActivity(Intent(this, TripCreationActivity::class.java))
        }
    }
}

