package pl.pwr.zpi.bcycle.mobile.adapters

import android.content.Context
import android.widget.ImageView
import com.squareup.picasso.Picasso

import io.brotherjing.galleryview.GalleryAdapter

class UrlGalleryAdapter(
    context: Context?,
    private val data: List<String>
) :
    GalleryAdapter(context) {
    override fun getInitPicIndex(): Int {
        return 0
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun fillViewAtPosition(position: Int, imageView: ImageView?) {
        val url = data[position]
        Picasso.get().cancelRequest(imageView!!)
        Picasso.get().load(url).into(imageView)
    }
}