package pl.pwr.zpi.bcycle.mobile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import pl.pwr.zpi.bcycle.mobile.adapters.TYPE_FUTURE
import pl.pwr.zpi.bcycle.mobile.api.ApiTokenManager
import pl.pwr.zpi.bcycle.mobile.ui.settings.SettingsFragment
import pl.pwr.zpi.bcycle.mobile.ui.settings.SettingsViewModel
import pl.pwr.zpi.bcycle.mobile.utils.showToast

class MainActivity : BCycleNavigationDrawerActivity(),SettingsFragment.OnDataChangedListener {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        configureMainNavigationDrawer(auth)

        auth.currentUser?.run {
            ApiTokenManager.updateToken(this)
                .addOnFailureListener {
                    showToast(getString(R.string.token_update_failed))
                }
            updateNavigationDrawerHeader(this, storage)
        } ?: openLoginScreen()

        if (intent.extras != null && intent.extras!!.containsKey(INTENT_EXTRA_MAIN_NAV_ID)) {
            navController.navigate(intent.extras!!.getInt(INTENT_EXTRA_MAIN_NAV_ID))
        }
    }

    private fun openLoginScreen() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onResume() {
        super.onResume()
        checkPlayServicesAvailability()
    }

    /** Check Google Play Services availability, and prompt the user to install it if needed. */
    private fun checkPlayServicesAvailability() {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val isAvailable = googleApiAvailability.isGooglePlayServicesAvailable(applicationContext)
        if (isAvailable != ConnectionResult.SUCCESS) {
            // request code currently not used
            googleApiAvailability.getErrorDialog(
                this, isAvailable, REQUEST_CODE_AFTER_GOOGLE_PLAY
            ) { finish() }?.show()

        }
    }

    override fun onDataChanged() {
        auth.currentUser?.let { updateNavigationDrawerHeader(it, storage) }
    }
}
