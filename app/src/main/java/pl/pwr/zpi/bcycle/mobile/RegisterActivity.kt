package pl.pwr.zpi.bcycle.mobile

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import pl.pwr.zpi.bcycle.mobile.utils.content
import pl.pwr.zpi.bcycle.mobile.utils.showToast

class RegisterActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()

        registerBt.isEnabled = false
        registerBt.setOnClickListener {
            if (isFormValid()) register() else showToast("Register form invalid")
        }

        privacyCB.setOnCheckedChangeListener { _, isChecked -> registerBt.isEnabled = isChecked }
    }

    // TODO: Implement actual form validation
    private fun isFormValid(): Boolean =
        namePT.content().isNotEmpty()
                && emailPT.content().isNotEmpty()
                && passwordPT.content().isNotEmpty()
                && passwordPT.content() == repeatPasswordPT.content()

    private fun register() = auth
        .createUserWithEmailAndPassword(emailPT.content(), passwordPT.content())
        .addOnSuccessListener { updateUserDetails(it.user!!) }
        .addOnFailureListener { showToast("Failed to register: ${it.localizedMessage}") }

    private fun updateUserDetails(user: FirebaseUser) {
        // TODO: Pick image from device's storage and upload to firebase storage
        val profile = UserProfileChangeRequest.Builder()
            .setPhotoUri(defaultAvatarUri)
            .setDisplayName(namePT.content())
            .build()

        user.updateProfile(profile)
            .addOnSuccessListener {
                showToast("You have been registered successfully")
                finish()
            }.addOnFailureListener {
                showToast("Failed to register: ${it.localizedMessage}")
                user.delete()
            }
    }

    companion object {
        private val defaultAvatarUri =
            Uri.parse("${FirebaseStorage.getInstance().reference.root}/default_avatar.png")
    }
}
