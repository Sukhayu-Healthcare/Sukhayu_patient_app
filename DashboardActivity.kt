package com.yourpackage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {

    private val noticeAdapter = NoticeAdapter() // Assume this exists

    private val noticeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshNotices()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        recycler_view.adapter = noticeAdapter
        // ...existing code...
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            noticeReceiver,
            IntentFilter(MyFirebaseMessagingService.BROADCAST_ACTION)
        )
        refreshNotices()
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(noticeReceiver)
    }

    private fun refreshNotices() {
        val authToken = TokenManager.getToken(this)
        if (authToken == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show()
            return
        }
        ApiClient.retrofit.getNotices("Bearer $authToken")
            .enqueue(object : Callback<List<Notice>> {
                override fun onResponse(call: Call<List<Notice>>, response: Response<List<Notice>>) {
                    if (response.isSuccessful && response.body() != null) {
                        noticeAdapter.submitList(response.body())
                    } else {
                        Toast.makeText(this@DashboardActivity, "Failed to load notices", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<Notice>>, t: Throwable) {
                    Toast.makeText(this@DashboardActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}