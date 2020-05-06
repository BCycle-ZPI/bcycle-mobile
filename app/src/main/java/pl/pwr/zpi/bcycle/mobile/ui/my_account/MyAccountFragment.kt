package pl.pwr.zpi.bcycle.mobile.ui.my_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_my_account.*
import pl.pwr.zpi.bcycle.mobile.R

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        nameTV.text = auth.currentUser?.displayName;
        emailTV.text = auth.currentUser?.email;
        storage.getReferenceFromUrl(auth.currentUser?.photoUrl.toString()).downloadUrl
            .addOnSuccessListener { Picasso.get().load(it).into(avatarIV) }
    }


}