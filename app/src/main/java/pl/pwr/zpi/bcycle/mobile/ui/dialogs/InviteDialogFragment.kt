package pl.pwr.zpi.bcycle.mobile.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import pl.pwr.zpi.bcycle.mobile.INTENT_EXTRA_INVITE_CODE
import pl.pwr.zpi.bcycle.mobile.INTENT_EXTRA_INVITE_TRIP_NAME
import pl.pwr.zpi.bcycle.mobile.R
import pl.pwr.zpi.bcycle.mobile.models.GroupTrip
import pl.pwr.zpi.bcycle.mobile.utils.showToast


class InviteDialogFragment : BCycleDialogFragment(R.layout.dialog_invite) {
    private var tripName = ""
    private var tripCode = ""

    override fun parseDataFromBundle(bundle: Bundle) {
        tripName = bundle.getString(INTENT_EXTRA_INVITE_TRIP_NAME)!!
        tripCode = bundle.getString(INTENT_EXTRA_INVITE_CODE)!!
        val shareCodeTV: TextView = findView(R.id.tripCodeTV)
        shareCodeTV.text = tripCode
    }

    override fun registerListeners() {
        findView<Button>(R.id.copyCodeBt).setOnClickListener { copyToClipboard() }
        findView<Button>(R.id.shareCodeBt).setOnClickListener { shareCode() }
        findView<Button>(R.id.doneBt).setOnClickListener { dismiss() }
    }

    private fun copyToClipboard() {
        val clipboard: ClipboardManager? =
            getSystemService(requireContext(), ClipboardManager::class.java)
        val clip = ClipData.newPlainText(
            getString(R.string.clipboard_invite_description), tripCode)
        clipboard?.primaryClip = clip
        showToast(R.string.copied_to_clipboard)
    }

    private fun shareCode() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_invite_text, tripName, tripCode))
        sendIntent.type = "text/plain"
        val shareIntent = Intent.createChooser(
            sendIntent, getString(R.string.share_invite_intent_title))
        startActivity(shareIntent)
    }

    companion object {
        fun prepareInviteDialog(trip: GroupTrip): Bundle {
            val b = Bundle()
            b.putString(INTENT_EXTRA_INVITE_TRIP_NAME, trip.name)
            b.putString(INTENT_EXTRA_INVITE_CODE, trip.tripCode)
            return b
        }
    }
}