package com.sukhayu.patient.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.SaveFcmTokenRequest
import com.sukhayu.patient.data.remote.SaveFcmTokenResponse
import com.sukhayu.patient.notification.CallNotificationManager
import com.sukhayu.patient.ui.asha.dashboard.AshaDashboardActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMessagingService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "========================================")
        Log.d(TAG, "🔔 NEW FCM TOKEN RECEIVED FROM FIREBASE")
        Log.d(TAG, "FCM Token: $token")
        Log.d(TAG, "========================================")

        // Save token to SharedPreferences
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
        Log.d(TAG, "✅ FCM token saved to SharedPreferences")

        // Send token to backend if user is logged in
        val authToken = prefs.getString("token", null)
        if (!authToken.isNullOrEmpty()) {
            Log.d(TAG, "📤 Sending FCM token to backend...")
            sendFcmTokenToBackend(authToken, token)
        } else {
            Log.d(TAG, "⏳ No auth token found. Token will be sent during next login.")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "========================================")
        Log.d(TAG, "📨 MESSAGE RECEIVED FROM FCM")
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Data: ${remoteMessage.data}")
        Log.d(TAG, "Title: ${remoteMessage.notification?.title}")
        Log.d(TAG, "Body: ${remoteMessage.notification?.body}")
        Log.d(TAG, "========================================")

        val title = remoteMessage.notification?.title ?: "New Notification"
        val body = remoteMessage.notification?.body ?: "You have a message"

        // 🔥 If backend sends a call notification
        if (remoteMessage.data["type"] == "call") {
            val doctorName = remoteMessage.data["doctor_name"] ?: "Doctor"
            Log.d(TAG, "☎️ INCOMING CALL from doctor: $doctorName")
            CallNotificationManager.showIncomingCall(this, doctorName)
            return
        }

        // 🔔 For normal ASHA notifications
        Log.d(TAG, "📣 Showing standard notification - Title: $title, Body: $body")
        showStandardNotification(title, body)
    }

    private fun showStandardNotification(title: String, body: String) {
        val channelId = "asha_notifications"

        val intent = Intent(this, AshaDashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "ASHA Worker Notices",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun sendFcmTokenToBackend(authToken: String, fcmToken: String) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "📤 SENDING FCM TOKEN FROM SERVICE TO BACKEND")
        Log.d(TAG, "Auth Token: $authToken")
        Log.d(TAG, "FCM Token: $fcmToken")
        Log.d(TAG, "========================================")
        
        val request = SaveFcmTokenRequest(fcmToken)
        
        try {
            ApiClient.retrofit.saveFcmToken("Bearer $authToken", request)
                .enqueue(object : Callback<SaveFcmTokenResponse> {
                    override fun onResponse(
                        call: Call<SaveFcmTokenResponse>,
                        response: Response<SaveFcmTokenResponse>
                    ) {
                        Log.d(TAG, "========================================")
                        Log.d(TAG, "📡 BACKEND RESPONSE RECEIVED (SERVICE)")
                        Log.d(TAG, "Response Code: ${response.code()}")
                        Log.d(TAG, "Is Successful: ${response.isSuccessful}")
                        Log.d(TAG, "Response Body: ${response.body()}")
                        Log.d(TAG, "========================================")
                        
                        if (response.isSuccessful) {
                            Log.d(TAG, "========================================")
                            Log.d(TAG, "✅ FCM TOKEN SENT TO BACKEND SUCCESSFULLY (SERVICE)")
                            Log.d(TAG, "Response: ${response.body()?.message}")
                            Log.d(TAG, "FCM Token: $fcmToken")
                            Log.d(TAG, "========================================")
                        } else {
                            Log.e(TAG, "========================================")
                            Log.e(TAG, "❌ BACKEND RETURNED ERROR (SERVICE)")
                            Log.e(TAG, "Status Code: ${response.code()}")
                            Log.e(TAG, "Error Body: ${response.errorBody()?.string()}")
                            Log.e(TAG, "========================================")
                        }
                    }

                    override fun onFailure(call: Call<SaveFcmTokenResponse>, t: Throwable) {
                        Log.e(TAG, "========================================")
                        Log.e(TAG, "❌ NETWORK ERROR SENDING FCM TOKEN (SERVICE)")
                        Log.e(TAG, "Error Message: ${t.message}")
                        Log.e(TAG, "Error Type: ${t.javaClass.simpleName}")
                        Log.e(TAG, "========================================")
                        t.printStackTrace()
                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "========================================")
            Log.e(TAG, "❌ EXCEPTION WHILE CREATING REQUEST (SERVICE)")
            Log.e(TAG, "Exception: ${e.message}")
            Log.e(TAG, "========================================")
            e.printStackTrace()
        }
    }
}
