package pl.pwr.zpi.bcycle.mobile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import pl.pwr.zpi.bcycle.mobile.utils.content
import pl.pwr.zpi.bcycle.mobile.utils.showToast

class LoginActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()

        if(auth.currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
        }

        loginBt.setOnClickListener {
            if (isFormFilled()) signIn()
        }

        registerBt.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun isFormFilled(): Boolean =
        isNotEmailEmpty() && isNotPasswordEmpty()

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

        return  email.content().isNotEmpty()
    }

    private fun signIn() {
        auth.signInWithEmailAndPassword(email.content(), password.content())
            .addOnSuccessListener {
                startActivity(Intent(this, MainActivity::class.java))
            }.addOnFailureListener {
                showToast("${getString(R.string.failed_to_sign_in)} + ${it.localizedMessage}")
            }
    }
}
