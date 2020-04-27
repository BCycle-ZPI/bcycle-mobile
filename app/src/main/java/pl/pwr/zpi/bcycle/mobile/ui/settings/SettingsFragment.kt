package pl.pwr.zpi.bcycle.mobile.ui.settings

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.progressBar
import pl.pwr.zpi.bcycle.mobile.MainActivity
import pl.pwr.zpi.bcycle.mobile.R
import pl.pwr.zpi.bcycle.mobile.RegisterActivity
import pl.pwr.zpi.bcycle.mobile.utils.showToast

class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel
    private var allControls: List<View> = listOf()


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

        allControls = listOf(emialPT,cameraBt,passwordPT,repeatPasswordPT,saveBt,namePT,galleryBt,cameraBt)
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

            Toast.makeText(activity,"picked",Toast.LENGTH_SHORT).show()

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
            Toast.makeText(activity,"in",Toast.LENGTH_SHORT).show()

            val result : CropImage.ActivityResult = CropImage.getActivityResult(data)

            if(resultCode == RESULT_OK) {

                Toast.makeText(activity,"good",Toast.LENGTH_SHORT).show()

                mImageUri = result.uri

                Picasso.get().load(mImageUri).into(avatarImV)

            } else {
                Toast.makeText(activity,"bad",Toast.LENGTH_SHORT).show()
            }

        }else {
            Toast.makeText(activity,"notin",Toast.LENGTH_SHORT).show()
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
        activity?.onBackPressed()
        hideSpinnerAndEnableControls()
        Toast.makeText(activity,getString(R.string.prompt_cancel_change),Toast.LENGTH_SHORT).show()
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
    }

}