package pl.pwr.zpi.bcycle.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_register.*
import pl.pwr.zpi.bcycle.mobile.utils.content
import pl.pwr.zpi.bcycle.mobile.utils.showToast


class RegisterActivity : AppCompatActivity() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var allControls: List<View> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()


        galleryBt.setOnClickListener {
            loadPhotoFromGallery()
        }

        registerBt.isEnabled = false
        registerBt.setOnClickListener {
            if (isFormValid()) register() else showToast("Please, enter all the data!")
        }

        privacyCB.setOnCheckedChangeListener { _, isChecked -> registerBt.isEnabled = isChecked }

        allControls = listOf(namePT, emailPT, passwordPT, repeatPasswordPT, CameraBt, galleryBt, privacyCB, registerBt)
    }


    private fun loadPhotoFromGallery() {
        intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode== PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data !=null && data.data != null){

            mImageUri = data.data
            Picasso.get().load(mImageUri).into(avatarImV)
        }
    }


    // TODO: Implement actual form validation
    private fun isFormValid(): Boolean =
        namePT.content().isNotEmpty()
                && emailPT.content().isNotEmpty()
                && passwordPT.content().isNotEmpty()
                && passwordPT.content() == repeatPasswordPT.content()

    private fun register() {
        showSpinnerAndDisableControls()
        auth
            .createUserWithEmailAndPassword(emailPT.content(), passwordPT.content())
            .addOnSuccessListener { updateUserDetails(it.user!!) }
            .addOnFailureListener {
                hideSpinnerAndEnableControls()
                showToast("Failed to register: ${it.localizedMessage}")
            }
    }

    private fun updateUserDetails(user: FirebaseUser) {
        val avatar: Uri? = if(uploadImageToFirebase()) {
            Uri.parse("${FirebaseStorage.getInstance().reference.root}/${namePT.content()}.png")
        } else {
            defaultAvatarUri
        }
        val profile = UserProfileChangeRequest.Builder()
            .setPhotoUri(avatar)
            .setDisplayName(namePT.content())
            .build()

        user.updateProfile(profile)
            .addOnSuccessListener {
                showToast("You have been registered successfully")
                finish()
            }.addOnFailureListener {
                showToast("Failed to register: ${it.localizedMessage}")
                user.delete()
                hideSpinnerAndEnableControls()
            }
    }

    private fun uploadImageToFirebase() :Boolean {
        if(mImageUri!=null) {
             val imageRef = FirebaseStorage.getInstance().reference.child(namePT.content()+ ".png")
            imageRef.putFile(mImageUri!!)
            return true
        }
        return false
    }

    private fun showSpinnerAndDisableControls() {
        progressBar.visibility = View.VISIBLE
        allControls.forEach { v -> v.isEnabled = false }
    }

    private fun hideSpinnerAndEnableControls() {
        progressBar.visibility = View.GONE
        allControls.forEach { v -> v.isEnabled = true }
    }

    companion object {
        private var mImageUri: Uri? = null
        private final val PICK_IMAGE_REQUEST = 1
        private val defaultAvatarUri =
            Uri.parse("${FirebaseStorage.getInstance().reference.root}/default_avatar.png")
    }
}
