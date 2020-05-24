package pl.pwr.zpi.bcycle.mobile.ui.dialogs

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import pl.pwr.zpi.bcycle.mobile.*
import pl.pwr.zpi.bcycle.mobile.api.ApiClient
import pl.pwr.zpi.bcycle.mobile.models.Trip
import pl.pwr.zpi.bcycle.mobile.utils.background
import pl.pwr.zpi.bcycle.mobile.utils.showToast


class ShareDialogFragment : BCycleDialogFragment(R.layout.dialog_share) {
    private var tripId = -1
    private var sharingUrl: String? = null
    private var photoCount = 0
    private var tripIsGroupTrip = false
    private lateinit var listener: ShareDialogListener

    interface ShareDialogListener {
        fun setSharingUrlFromDialog(url: String?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as ShareDialogListener
    }

    override fun parseDataFromBundle(bundle: Bundle) {
        tripId = bundle.getInt(INTENT_EXTRA_SHARE_ID)
        sharingUrl = bundle.getString(INTENT_EXTRA_SHARE_URL)
        photoCount = bundle.getInt(INTENT_EXTRA_SHARE_PHOTO_COUNT)
        tripIsGroupTrip = bundle.getBoolean(INTENT_EXTRA_SHARE_ISGROUP)
        val shareDescTV: TextView = findView(R.id.shareDescTV)
        val textWithPhotos: Int
        val textWithoutPhotos: Int
        if (tripIsGroupTrip) {
            textWithPhotos = R.plurals.sharing_explanation_with_photos_group
            textWithoutPhotos = R.string.sharing_explanation_without_photos_group
        } else {
            textWithPhotos = R.plurals.sharing_explanation_with_photos
            textWithoutPhotos = R.string.sharing_explanation_without_photos
        }
            if (photoCount > 0) {
                shareDescTV.text = resources.getQuantityString(textWithPhotos, photoCount, photoCount)
            } else {
                shareDescTV.setText(textWithoutPhotos)
            }
        setUrl(sharingUrl)
    }

    override fun registerListeners() {
        val shareSwitch: Switch = findView(R.id.shareSwitch)
        shareSwitch.setOnClickListener {
            shareSwitch.isEnabled = false
            findView<ProgressBar>(R.id.shareSpinner).visibility = View.VISIBLE
            findView<TextView>(R.id.shareLinkTV).setText(R.string.please_wait)
            if (sharingUrl == null) enableSharing() else disableSharing()
        }
        findView<Button>(R.id.copyLinkBt).setOnClickListener { copyToClipboard() }
        findView<Button>(R.id.shareLinkBt).setOnClickListener { shareLink() }
        findView<Button>(R.id.doneBt).setOnClickListener { dismiss() }
    }

    @SuppressLint("CheckResult")
    private fun enableSharing() {
        val request = ApiClient.shareApi.startSharing(tripId)
        request.background().subscribe(
            { url -> setUrl(url.result) },
            { error -> showError(error) }
        )
    }

    @SuppressLint("CheckResult")
    private fun disableSharing() {
        val request = ApiClient.shareApi.stopSharing(tripId)
        request.background().subscribe(
            { setUrl(null) },
            { error -> showError(error) }
        )
    }

    private fun copyToClipboard() {
        if (sharingUrl == null) return
        val clipboard: ClipboardManager? =
            getSystemService(requireContext(), ClipboardManager::class.java)
        val clip = ClipData.newPlainText(
            getString(R.string.clipboard_share_description), sharingUrl)
        clipboard?.primaryClip = clip
        showToast(R.string.copied_to_clipboard)
    }

    private fun shareLink() {
        if (sharingUrl == null) return
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, sharingUrl)
        sendIntent.type = "text/url"
        val shareIntent = Intent.createChooser(
            sendIntent, getString(R.string.share_intent_title))
        startActivity(shareIntent)
    }

    private fun setUrl(url: String?) {
        sharingUrl = url
        val shareSwitch: Switch = findView(R.id.shareSwitch)
        val shareLinkBt: Button = findView(R.id.shareLinkBt)
        val copyLinkBt: Button = findView(R.id.copyLinkBt)
        val shareLinkTV: TextView = findView(R.id.shareLinkTV)
        val shareSpinner: ProgressBar = findView(R.id.shareSpinner)
        if (sharingUrl == null) {
            shareSwitch.isChecked = false
            shareLinkBt.isEnabled = false
            copyLinkBt.isEnabled = false
            shareLinkTV.isEnabled = false
            shareLinkTV.setText(R.string.not_sharing)
        } else {
            shareSwitch.isChecked = true
            shareLinkBt.isEnabled = true
            copyLinkBt.isEnabled = true
            shareLinkTV.isEnabled = true
            shareLinkTV.text = sharingUrl
        }
        hideError()
        listener.setSharingUrlFromDialog(url)
        shareSwitch.isEnabled = true
        shareSpinner.visibility = View.GONE
    }

    private fun showError(error: Throwable) {
        val shareSwitch: Switch = findView(R.id.shareSwitch)
        val shareErrorTV: TextView = findView(R.id.shareErrorTV)
        shareSwitch.isEnabled = true
        shareErrorTV.visibility = View.VISIBLE
        shareErrorTV.text = error.localizedMessage
    }

    private fun hideError() {
        findView<TextView>(R.id.shareErrorTV).visibility = View.GONE
    }

    companion object {
        fun prepareShareDialog(trip: Trip): Bundle {
            val b = Bundle()
            b.putBoolean(INTENT_EXTRA_SHARE_ISGROUP, trip.groupTripId != null)
            b.putInt(INTENT_EXTRA_SHARE_ID, trip.id!!)
            b.putInt(INTENT_EXTRA_SHARE_PHOTO_COUNT, trip.photos.size)
            b.putString(INTENT_EXTRA_SHARE_URL, trip.sharingUrl)
            return b
        }
    }
}