package com.sukhayu.patient.ui.teleconsult

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.databinding.ActivityConsentBinding

class ConsentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConsentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConsentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAccept.setOnClickListener {
            startActivity(Intent(this, VideoCallActivity::class.java))
            finish()
        }

        binding.btnDecline.setOnClickListener {
            finish()
        }
    }
}
