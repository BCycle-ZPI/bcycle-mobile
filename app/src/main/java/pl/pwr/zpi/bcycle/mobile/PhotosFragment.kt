package pl.pwr.zpi.bcycle.mobile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_photos.*
import pl.pwr.zpi.bcycle.mobile.adapters.UrlGalleryAdapter

class PhotosFragment(private val con: Context, private val photos:List<String>, private val callback: OnPhotosWindowClosedCallback) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gallery.adapter = UrlGalleryAdapter(con, photos)
        photosoffBT.setOnClickListener {
            callback.onPhotosWindowClosed()
            parentFragmentManager.beginTransaction().remove(this).commit()
        }
    }
}
