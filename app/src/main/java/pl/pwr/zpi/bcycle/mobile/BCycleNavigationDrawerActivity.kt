package pl.pwr.zpi.bcycle.mobile

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.nav_header_main.view.*
import pl.pwr.zpi.bcycle.mobile.ui.settings.SettingsFragment
import pl.pwr.zpi.bcycle.mobile.utils.showToast

abstract class BCycleNavigationDrawerActivity: AppCompatActivity() {
    protected lateinit var appBarConfiguration: AppBarConfiguration
    protected lateinit var navController: NavController

    protected fun updateNavigationDrawerHeader() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            updateNavigationDrawerHeader(user, FirebaseStorage.getInstance())
        }
    }

    protected fun updateNavigationDrawerHeader(user: FirebaseUser) {
        updateNavigationDrawerHeader(user, FirebaseStorage.getInstance())
    }

    protected fun configureMainNavigationDrawer() {
        configureMainNavigationDrawer(FirebaseAuth.getInstance())
    }

    protected fun configureIndependentNavigationDrawer() {
        configureIndependentNavigationDrawer(FirebaseAuth.getInstance())
    }
    
    protected fun updateNavigationDrawerHeader(user: FirebaseUser, storage: FirebaseStorage) {
        val header = findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
        header.currentUserName.text = user.displayName
        header.currentUserEmail.text = user.email
        storage.getReferenceFromUrl(user.photoUrl.toString()).downloadUrl
            .addOnSuccessListener { Picasso.get().load(it).into(header.currentUserImage) }
            .addOnFailureListener {
                Picasso.get().load(defaultAvatarUri).into(header.currentUserImage)
            }
    }

    protected fun configureMainNavigationDrawer(auth: FirebaseAuth) {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(appDestinations, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener {
            if (it.itemId == R.id.nav_logout) {
                auth.signOut()
                finish()
            } else {
                navController.navigate(it.itemId)


            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }



    protected fun configureIndependentNavigationDrawer(auth: FirebaseAuth) {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        appBarConfiguration = AppBarConfiguration(appDestinations, drawerLayout)

        navView.setNavigationItemSelectedListener {
            if (it.itemId == R.id.nav_logout) {
                auth.signOut()
                finishAndRemoveTask()
            } else {
                // Start the main activity and run the item.
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.putExtra(INTENT_EXTRA_MAIN_NAV_ID, it.itemId)
                startActivity(intent)
                finish()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }



    companion object {
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appDestinations = setOf(
            R.id.nav_home, R.id.nav_join_group_trip, R.id.nav_create_group_trip,
            R.id.nav_my_account, R.id.nav_settings
        )
        private val defaultAvatarUri =
            Uri.parse("${FirebaseStorage.getInstance().reference.root}/default_avatar.png")
    }


}