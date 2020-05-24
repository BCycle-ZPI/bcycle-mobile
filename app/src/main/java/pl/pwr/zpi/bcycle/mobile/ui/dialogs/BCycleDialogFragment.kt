package pl.pwr.zpi.bcycle.mobile.ui.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import pl.pwr.zpi.bcycle.mobile.R

abstract class BCycleDialogFragment(private var dialogLayout: Int): DialogFragment() {
    private lateinit var dialogView: View

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        // Get the layout inflater
        val inflater = requireActivity().layoutInflater

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        dialogView = inflater.inflate(dialogLayout, null)
        parseDataFromBundle(requireArguments())
        registerListeners()
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_rounded)
        return dialog
    }

    // Synthetic view references do not work in dialog fragments
    protected fun <T : View?> findView(id: Int): T = dialogView.findViewById<T>(id)

    protected abstract fun registerListeners()

    protected abstract fun parseDataFromBundle(bundle: Bundle)
}
