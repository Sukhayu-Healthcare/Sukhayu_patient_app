package com.example.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging

class CreateDriveActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_drive)

        // Call this method after login or app start
        registerFcmTokenIfNeeded()
    }

    private fun registerFcmTokenIfNeeded() {
        val authToken = TokenManager.getToken(this)
        if (authToken != null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    ApiClient.retrofit.registerFcmToken(
                        "Bearer $authToken",
                        FcmTokenRequest(token)
                    ).enqueue(object : retrofit2.Callback<GenericResponse> {
                        override fun onResponse(call: retrofit2.Call<GenericResponse>, response: retrofit2.Response<GenericResponse>) {
                            // Optionally log success
                        }
                        override fun onFailure(call: retrofit2.Call<GenericResponse>, t: Throwable) {
                            // Optionally log failure
                        }
                    })
                }
            }
        }
    }
}