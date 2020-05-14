package pl.pwr.zpi.bcycle.mobile.ui.my_account

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_my_account.*
import pl.pwr.zpi.bcycle.mobile.R
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.utils.background
import kotlin.math.round
import kotlin.math.roundToLong

class MyAccountFragment : Fragment() {

    private lateinit var myAccountViewModel: MyAccountViewModel
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myAccountViewModel = ViewModelProvider(this).get(MyAccountViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_my_account, container, false)
        return root
    }

    @SuppressLint("CheckResult")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        progressBar.visibility = View.VISIBLE

        nameTV.text = auth.currentUser?.displayName;
        emailTV.text = auth.currentUser?.email;
        storage.getReferenceFromUrl(auth.currentUser?.photoUrl.toString()).downloadUrl
            .addOnSuccessListener { Picasso.get().load(it).into(avatarIV) }


        ApiClient.statsApi.getUserStats().background().subscribe({
            stats ->






            numberOfAllTripsCountTV.text = stats.result.tripCount.toString()
            numberOfGroupTripsCountTV.text = stats.result.groupTripTotalCount.toString()
            numberOfCreatedGroupTripsCountTV.text  = stats.result.groupTripHostingCount.toString()
            totalDistanceKmCountTV.text = (round( stats.result.totalKilometers * 100) / 100).toString().replace('.',',')
            totalTimeCountTV.text = (round(stats.result.totalTimeMinutes * 100) / 100).toString().replace('.',',')

            progressBar.visibility = View.GONE
        },
            {error->
                error.message
                Toast.makeText(activity,getString(R.string.changed_data_error), Toast.LENGTH_SHORT).show()
                progressBar.visibility=View.GONE;

        }
        )
    }


}