package pl.pwr.zpi.bcycle.mobile

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
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
         cameraBt.setOnClickListener{
             loadPhotoFromCamera()
         }

        registerBt.isEnabled = false
        registerBt.setOnClickListener {
            if (isFormValid()) register()
        }

        privacyCB.setOnCheckedChangeListener { _, isChecked -> registerBt.isEnabled = isChecked }

        allControls = listOf(namePT, emailTV, passwordPT, repeatPasswordPT, cameraBt, galleryBt, privacyCB, registerBt)
    }

    private fun loadPhotoFromCamera() {

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permission= arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permission,PERMISSION_CODE_CAMERA)
            } else {
                openCamera()
            }
        } else  {
            openCamera()
        }
    }

    private fun openCamera() {
        val values  = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, getString(R.string.new_avatar_info))
        values.put(MediaStore.Images.Media.DESCRIPTION, getString(R.string.camera_source_info))
        mImageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)
        startActivityForResult(cameraIntent,MAKE_IMAGE_REQUEST )

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when(requestCode) {
            PERMISSION_CODE_CAMERA -> if(grantResults.size>0  && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                                    openCamera()
            PERMISSION_CODE_GALLERY -> if(grantResults.size>0  && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                                    openGallery()
            else -> showToast(getString(R.string.permission_denied_info))

        }
    }

    private fun loadPhotoFromGallery() {

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permission= arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permission, PERMISSION_CODE_GALLERY)
            } else {
                openGallery()
            }
        } else  {
            openGallery()
        }

    }

    private fun openGallery() {
        intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode== PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data !=null && data.data != null){

            mImageUri = data.data

                CropImage.activity(mImageUri)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        if(requestCode == MAKE_IMAGE_REQUEST && mImageUri !=null) {

            CropImage.activity(mImageUri)
                .setAspectRatio(1,1)
                .start(this);

        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            val result : CropImage.ActivityResult = CropImage.getActivityResult(data)

            if(resultCode == Activity.RESULT_OK) {

                mImageUri = result.uri

                Picasso.get().load(mImageUri).into(avatarImV)

            }

        }
    }

    private fun isFormValid(): Boolean =
     allDataFilled() && isPasswordOk() && isPasswordIdentical() && accountNotExists()

    private fun allDataFilled(): Boolean {
        if(namePT.content().isEmpty()) {
            namePT.error = getString(R.string.empty_edit_text)
        }
        if(emailTV.content().isEmpty()) {
            emailTV.error = getString(R.string.empty_edit_text)
        }
        if(passwordPT.content().isEmpty()) {
            passwordPT.error = getString(R.string.empty_edit_text)
        }
        if(repeatPasswordPT.content().isEmpty()){
            repeatPasswordPT.error = getString(R.string.empty_edit_text)
        }

        return namePT.content().isNotEmpty()
                && emailTV.content().isNotEmpty()
                && passwordPT.content().isNotEmpty()
                && repeatPasswordPT.content().isNotEmpty()
    }


    private fun accountNotExists() :Boolean {
        return !auth.isSignInWithEmailLink(emailTV.content())
    }

    private fun isPasswordOk() : Boolean {
        if(passwordPT.content().length <6 ){
            passwordPT.error = getString(R.string.too_short_password_message)
        }
        return passwordPT.content().length >5
    }

    private fun isPasswordIdentical() : Boolean {
        if(passwordPT.content()!= repeatPasswordPT.content()) {
            repeatPasswordPT.error = getString(R.string.different_password_message)
        }

        return passwordPT.content()== repeatPasswordPT.content()
    }
    private fun register() {
        showSpinnerAndDisableControls()
        auth
            .createUserWithEmailAndPassword(emailTV.content(), passwordPT.content())
            .addOnSuccessListener { updateUserDetails(it.user!!) }
            .addOnFailureListener {

                hideSpinnerAndEnableControls()
                if (it.message.equals(getString(R.string.emial_exists_message))) {
                    emailTV.error = getString(R.string.emial_exists_message)
                }

                if (it.message.equals(getString(R.string.badly_formated_email_message))) {
                    emailTV.error = getString(R.string.badly_formated_email_message)
                }
            }
    }


    private fun updateUserDetails(user: FirebaseUser) {
        val avatar: Uri? = if(uploadImageToFirebase()) {
            Uri.parse("${FirebaseStorage.getInstance().reference.root}/${auth.currentUser!!.uid}.png")
        } else {
            defaultAvatarUri
        }

        val profile = UserProfileChangeRequest.Builder()
            .setPhotoUri(avatar)
            .setDisplayName(namePT.content())
            .build()

        user.updateProfile(profile)
            .addOnSuccessListener {
                showToast(getString(R.string.register_success_message))
                finish()
            }.addOnFailureListener {
                showToast("${getString(R.string.failed_to_register)} ${it.localizedMessage}")
                user.delete()
                hideSpinnerAndEnableControls()
            }
    }

    private fun uploadImageToFirebase() :Boolean {
        if(mImageUri!=null) {
             val imageRef = FirebaseStorage.getInstance().reference.child(auth.currentUser!!.uid+".png")
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
        private val PERMISSION_CODE_CAMERA: Int = 1000;
        private val PERMISSION_CODE_GALLERY: Int = 1001;
        private var mImageUri: Uri? = null
        private  val PICK_IMAGE_REQUEST = 1
        private  val MAKE_IMAGE_REQUEST = 2
        private val defaultAvatarUri =
            Uri.parse("${FirebaseStorage.getInstance().reference.root}/default_avatar.png")
    }
}
