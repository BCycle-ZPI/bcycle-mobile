package pl.pwr.zpi.bcycle.mobile

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.nav_header_main.view.*
import pl.pwr.zpi.bcycle.mobile.api.ApiClient

class MainActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener {
            if (it.itemId == R.id.nav_logout) {
                auth.signOut()
                finish()
            }
            true
        }

        auth.currentUser?.run {
            this.getIdToken(false).addOnSuccessListener {
                ApiClient.currentToken = it.token!!
            }.addOnFailureListener {
                finish() // TODO: or refresh?
            }
            val header = navView.getHeaderView(0)
            header.currentUserName.text = displayName
            header.currentUserEmail.text = email
            storage.getReferenceFromUrl(this.photoUrl.toString()).downloadUrl
                .addOnSuccessListener{ Picasso.get().load(it).into(header.currentUserImage) }

        } ?: finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
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
}
