package pl.pwr.zpi.bcycle.mobile.ui.settings

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.fragment_settings.*
import pl.pwr.zpi.bcycle.mobile.R
import pl.pwr.zpi.bcycle.mobile.utils.content


class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel
    private var allControls: List<View> = listOf()
    val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var callback: OnDataChangedListener


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(activity is OnDataChangedListener)
            callback = activity as OnDataChangedListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancelBt.setOnClickListener() {
            cancel()
        }
        galleryBt.setOnClickListener(){
            loadPhotoFromGallery()
        }
        cameraBt.setOnClickListener(){
            loadPhotoFromCamera()
        }

        saveBt.setOnClickListener(){
            if(isDataEdited()) saveChanges()
            else Toast.makeText(activity,getString(R.string.nothing_to_edit_prompt),Toast.LENGTH_SHORT).show()
        }

        allControls = listOf(cameraBt,passwordPT,repeatPasswordPT,saveBt,namePT,galleryBt,cameraBt,cancelBt)
    }

    private fun saveChanges() {

        showSpinnerAndDisableControls()

        var newName : String? = null
        var newPassword : String?  = null
        var newAvatar : Uri? = null

        if(isNameEdited()){
            newName = namePT.content()
        }

        if(isPasswordEditedOk()){
            newPassword = passwordPT.content()
        }

        if(isImageEdited()) {
            newAvatar = mImageUri
        }

        val handler = Handler()
        handler.postDelayed(Runnable {
            changeData(newName,newPassword,newAvatar)
            // Actions to do after 3 seconds
        }, 3000)

    }

    fun changeData(name: String?, password: String?, imageUri : Uri?) {

        val profileBuilder = UserProfileChangeRequest.Builder()

        if(password!=null) {
            currentUser!!.updatePassword(password)

        }

        if(name!=null){
            profileBuilder.setDisplayName(name)
        }

        if(imageUri!=null) {
            uploadImageToFirebase(imageUri)
            val avatar: Uri? = if(uploadImageToFirebase(imageUri)) {
                Uri.parse("${FirebaseStorage.getInstance().reference.root}/${currentUser!!.uid}.png")
            } else {
                defaultAvatarUri
            }
            profileBuilder.setPhotoUri(avatar)
        }

        if(name!=null || imageUri!=null){
            val profile = profileBuilder.build()

            currentUser!!.updateProfile(profile).addOnCompleteListener{
                callback.onDataChanged()
                hideSpinnerAndEnableControls()
                activity?.onBackPressed()
                Toast.makeText(activity,getString(R.string.data_changed_prompt),Toast.LENGTH_SHORT).show()

            }.addOnFailureListener{
                onFailure()
            }
        } else {
            hideSpinnerAndEnableControls()
            activity?.onBackPressed()
            Toast.makeText(activity,getString(R.string.data_changed_prompt),Toast.LENGTH_SHORT).show()
        }

    }

    private fun onFailure(){

        hideSpinnerAndEnableControls()
        Toast.makeText(activity,getString(R.string.changed_data_error),Toast.LENGTH_SHORT).show()
    }

    private fun uploadImageToFirebase(mImageUri:Uri?) :Boolean {
        if(mImageUri!=null) {
            val imageRef = FirebaseStorage.getInstance().reference.child(currentUser!!.uid+".png")
            imageRef.putFile(mImageUri)
            return true
        }
        return false
    }

    private fun isDataEdited(): Boolean =
                isNameEdited()  || isImageEdited() || isPasswordEditedOk()

    private fun isNameEdited(): Boolean = namePT.content().isNotEmpty()

    private fun isImageEdited(): Boolean = mImageUri!=null

    private fun isPasswordEditedOk(): Boolean {

        var correct = true

        if(passwordPT.content().isEmpty() && repeatPasswordPT.content().isEmpty()){
            correct=false
        }
        else if (passwordPT.content().isNotEmpty()){

            if(repeatPasswordPT.content().isNotEmpty()){
                if(passwordPT.content().length <6 ){
                    passwordPT.error = getString(R.string.too_short_password_message)
                    correct = false
                }
                else if(passwordPT.content()!= repeatPasswordPT.content()) {
                    repeatPasswordPT.error = getString(R.string.different_password_message)
                    correct=false
                }
            } else {
                correct=false
                repeatPasswordPT.error = getString(R.string.empty_edit_text)
            }
        } else {
            correct=false
            passwordPT.error = getString(R.string.empty_edit_text)
        }

        return correct
    }


    private fun loadPhotoFromGallery() {

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) {
            if(context?.let { checkSelfPermission(it,Manifest.permission.READ_EXTERNAL_STORAGE) } == PackageManager.PERMISSION_DENIED) {
                val permission= arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permission, PERMISSION_CODE_GALLERY)
            } else {
                openGallery()
            }
        } else  {
            openGallery()
        }
    }

    private fun loadPhotoFromCamera() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if(context?.let { checkSelfPermission(it,Manifest.permission.CAMERA) } == PackageManager.PERMISSION_DENIED ||
                context?.let { checkSelfPermission(it,Manifest.permission.WRITE_EXTERNAL_STORAGE) } == PackageManager.PERMISSION_DENIED) {
                val permission= arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permission,PERMISSION_CODE_CAMERA)
            } else {
                openCamera()
            }
        } else  {
            openCamera()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private fun openCamera(){

        val values  = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, getString(R.string.new_avatar_info))
        values.put(MediaStore.Images.Media.DESCRIPTION, getString(R.string.camera_source_info))
        mImageUri =
            activity?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)
        startActivityForResult(cameraIntent,MAKE_IMAGE_REQUEST )
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode== PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data !=null && data.data != null){


            mImageUri = data.data



            context?.let {
                CropImage.activity(mImageUri)
                    .setAspectRatio(1,1)
                    .start(it,this)
            };

        }

        if(requestCode == MAKE_IMAGE_REQUEST && mImageUri !=null) {

            context?.let {
                CropImage.activity(mImageUri)
                    .setAspectRatio(1,1)
                    .start(it,this)
            };

        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {


            val result : CropImage.ActivityResult = CropImage.getActivityResult(data)

            if(resultCode == RESULT_OK) {

                mImageUri = result.uri

                Picasso.get().load(mImageUri).into(avatarImV)

            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when(requestCode) {
            PERMISSION_CODE_CAMERA -> if(grantResults.size>0  && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                openCamera()
            PERMISSION_CODE_GALLERY -> if(grantResults.size>0  && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                openGallery()
            else -> Toast.makeText(activity,getString(R.string.permission_denied_info),Toast.LENGTH_SHORT).show()

        }
    }

    private fun cancel() {

        showSpinnerAndDisableControls()

        val handler = Handler()
        handler.postDelayed(Runnable {
            hideSpinnerAndEnableControls()
            activity?.onBackPressed()
            Toast.makeText(activity,getString(R.string.prompt_cancel_change),Toast.LENGTH_SHORT).show()
            // Actions to do after 3 seconds
        }, 3000)


    }

    private fun showSpinnerAndDisableControls() {
        progressBar.visibility = View.VISIBLE
        allControls.forEach { v -> v.isEnabled = false }
    }

    private fun hideSpinnerAndEnableControls() {
        progressBar.visibility = View.GONE
        allControls.forEach { v -> v.isEnabled = true }
    }

    companion object
    {
        private val PERMISSION_CODE_CAMERA: Int = 1000;
        private val PERMISSION_CODE_GALLERY: Int = 1001;
        private var mImageUri: Uri? = null
        private  val PICK_IMAGE_REQUEST = 1
        private  val MAKE_IMAGE_REQUEST = 2
        private val defaultAvatarUri =
            Uri.parse("${FirebaseStorage.getInstance().reference.root}/default_avatar.png")
    }


    interface OnDataChangedListener {
        fun onDataChanged()
    }


}