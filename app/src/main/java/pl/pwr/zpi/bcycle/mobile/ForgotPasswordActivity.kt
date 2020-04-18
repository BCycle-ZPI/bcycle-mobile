package pl.pwr.zpi.bcycle.mobile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_forgot_password.*
import pl.pwr.zpi.bcycle.mobile.utils.content
import pl.pwr.zpi.bcycle.mobile.utils.showToast

class ForgotPasswordActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var allControls: List<View> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        supportActionBar?.hide()
        allControls = listOf(emailET,sendEmailBt)

        sendEmailBt.setOnClickListener() {
            if(isNotEmailEmpty()) sendEmail()
        }
    }

    private fun isNotEmailEmpty() : Boolean {
        if(emailET.content().isEmpty()) {
            emailET.error = getString(R.string.empty_edit_text)
        }

        return  emailET.content().isNotEmpty()
    }

    private fun sendEmail() {

        showSpinnerAndDisableControls()
        auth.sendPasswordResetEmail(emailET.content())
            .addOnSuccessListener {
                showToast(getString(R.string.email_sent_message))
                startActivity(Intent(this,LoginActivity::class.java))
                finish()

            }.addOnFailureListener(){
                showToast("${getString(R.string.failed_to_sign_in)}  ${it.localizedMessage}")
                hideSpinnerAndEnableControls()
            }

    }

    private fun showSpinnerAndDisableControls() {
        progressBar.visibility = View.VISIBLE
        allControls.forEach { v -> v.isEnabled = false }
    }

    private fun hideSpinnerAndEnableControls() {
        progressBar.visibility = View.GONE
        allControls.forEach { v -> v.isEnabled = true }
    }
}
