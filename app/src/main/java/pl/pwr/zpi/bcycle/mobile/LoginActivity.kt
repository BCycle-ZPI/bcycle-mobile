package pl.pwr.zpi.bcycle.mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import pl.pwr.zpi.bcycle.mobile.utils.content
import pl.pwr.zpi.bcycle.mobile.utils.showToast

class LoginActivity : AppCompatActivity() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var allControls: List<View> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()
        allControls = listOf(email, password, loginBt, registerBt)

        foregtPasswordBt.setOnClickListener() {
            startActivity(Intent(this,ForgotPasswordActivity::class.java))
        }

        loginBt.setOnClickListener {
            if (isFormFilled()) signIn()
        }

        registerBt.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun isFormFilled(): Boolean {
        isNotPasswordEmpty()
        isNotEmailEmpty()

        return isNotPasswordEmpty() && isNotEmailEmpty()

    }



    private fun isNotEmailEmpty() : Boolean {
        if(email.content().isEmpty()) {
            email.error = getString(R.string.empty_edit_text)
        }

        return  email.content().isNotEmpty()
    }

    private fun isNotPasswordEmpty() : Boolean {
        if(password.content().isEmpty()) {
            password.error = getString(R.string.empty_edit_text)
        }

        return  password.content().isNotEmpty()
    }

    private fun signIn() {
        showSpinnerAndDisableControls()
        auth.signInWithEmailAndPassword(email.content(), password.content())
            .addOnSuccessListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }.addOnFailureListener {
                showToast(getString(R.string.failed_to_sign_in, it.localizedMessage))
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
