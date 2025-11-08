// package com.sukhayu.patient.notification

// import android.app.NotificationChannel
// import android.app.NotificationManager
// import android.content.Context
// import android.os.Build
// import androidx.core.app.NotificationCompat
// import com.sukhayu.patient.R

// object CallNotificationManager {

//     private const val CHANNEL_ID = "call_notifications"

//     fun showIncomingCall(context: Context, doctorName: String) {
//         createChannel(context)
//         val builder = NotificationCompat.Builder(context, CHANNEL_ID)
//             .setSmallIcon(R.drawable.ic_call)
//             .setContentTitle("Incoming Call")
//             .setContentText("Dr. $doctorName is calling...")
//             .setPriority(NotificationCompat.PRIORITY_HIGH)
//             .setAutoCancel(true)

//         val notificationManager =
//             context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//         notificationManager.notify(101, builder.build())
//     }

//     fun showCallEnded(context: Context) {
//         val builder = NotificationCompat.Builder(context, CHANNEL_ID)
//             .setSmallIcon(R.drawable.ic_call_end)
//             .setContentTitle("Call Ended")
//             .setContentText("Teleconsultation completed successfully.")
//             .setPriority(NotificationCompat.PRIORITY_DEFAULT)

//         val notificationManager =
//             context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//         notificationManager.notify(102, builder.build())
//     }

//     private fun createChannel(context: Context) {
//         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//             val channel = NotificationChannel(
//                 CHANNEL_ID,
//                 "Teleconsultation Alerts",
//                 NotificationManager.IMPORTANCE_HIGH
//             )
//             val manager =
//                 context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//             manager.createNotificationChannel(channel)
//         }
//     }
// }
