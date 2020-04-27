package pl.pwr.zpi.bcycle.mobile.api

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object ApiTokenManager {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    var token: String = ""
        private set

    fun updateToken() = auth.currentUser?.let { updateToken(it) }

    fun updateToken(firebaseUser: FirebaseUser) =
        firebaseUser.getIdToken(true)
            .addOnSuccessListener {
                this.token = it.token!!
            }
}