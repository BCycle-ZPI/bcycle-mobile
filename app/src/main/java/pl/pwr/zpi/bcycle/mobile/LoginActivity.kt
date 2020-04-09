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
            if (isFormFilled()) signIn() else showToast("Enter both email and password")
        }

        registerBt.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun isFormFilled(): Boolean =
        email.content().isNotEmpty() && password.content().isNotEmpty()

    private fun signIn() {
        auth.signInWithEmailAndPassword(email.content(), password.content())
            .addOnSuccessListener {
                startActivity(Intent(this, MainActivity::class.java))
            }.addOnFailureListener {
                showToast("Failed to sign in: ${it.localizedMessage}")
            }
    }
}
