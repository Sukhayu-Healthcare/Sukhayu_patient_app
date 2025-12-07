package com.yourpackage

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_notice_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoticeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_detail)

        val noticeId = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_NOTICE_ID)
        if (noticeId == null) {
            Toast.makeText(this, "Notice ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val authToken = TokenManager.getToken(this)
        if (authToken == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        ApiClient.retrofit.getNoticeDetail("Bearer $authToken", noticeId)
            .enqueue(object : Callback<NoticeDetailResponse> {
                override fun onResponse(call: Call<NoticeDetailResponse>, response: Response<NoticeDetailResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val notice = response.body()!!
                        notice_title.text = notice.title
                        notice_body.text = notice.body
                        // ...populate other views...
                    } else {
                        Toast.makeText(this@NoticeDetailActivity, "Failed to load notice", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<NoticeDetailResponse>, t: Throwable) {
                    Toast.makeText(this@NoticeDetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
