package dev.anonymous.eilaji.doctor.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseControllerKotlin {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        @Volatile
        private var instance: FirebaseControllerKotlin? = null
        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: FirebaseControllerKotlin().also { instance = it }
            }

        private const val TAG = "FirebaseController"
    }



    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}