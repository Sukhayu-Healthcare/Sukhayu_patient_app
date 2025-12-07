package com.yourpackage

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "notice_channel"
        const val CHANNEL_NAME = "Notice Updates"
        const val BROADCAST_ACTION = "com.yourpackage.NOTICE_PUSH"
        const val EXTRA_NOTICE_ID = "notice_id"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        val authToken = TokenManager.getToken(this)
        if (authToken != null) {
            ApiClient.retrofit.registerFcmToken(
                "Bearer $authToken",
                FcmTokenRequest(token)
            ).enqueue(object : retrofit2.Callback<GenericResponse> {
                override fun onResponse(call: retrofit2.Call<GenericResponse>, response: retrofit2.Response<GenericResponse>) {
                    Log.d("FCM", "Token registered: ${response.isSuccessful}")
                }
                override fun onFailure(call: retrofit2.Call<GenericResponse>, t: Throwable) {
                    Log.e("FCM", "Token registration failed", t)
                }
            })
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val noticeId = remoteMessage.data[EXTRA_NOTICE_ID]
        val title = remoteMessage.notification?.title ?: "New Notice"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "You have a new notice."

        // Show notification
        showNotification(title, body, noticeId)

        // Broadcast in-app event for Dashboard refresh
        val intent = Intent(BROADCAST_ACTION)
        if (noticeId != null) intent.putExtra(EXTRA_NOTICE_ID, noticeId)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun showNotification(title: String, body: String, noticeId: String?) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, NoticeDetailActivity::class.java)
        if (noticeId != null) intent.putExtra(EXTRA_NOTICE_ID, noticeId)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(noticeId?.hashCode() ?: 0, notification)
    }
}
