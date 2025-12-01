package com.sukhayu.patient

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.databinding.ActivityMainBinding
import com.sukhayu.patient.ui.consultation.VideoCallActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.fabVideoCall?.setOnClickListener {
            startQuickVideoCall()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_video_call -> {
                startQuickVideoCall()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startQuickVideoCall() {
        val patientId = "patient_${System.currentTimeMillis()}"
        val intent = Intent(this, VideoCallActivity::class.java).apply {
            putExtra("patientId", patientId)
            putExtra("doctorId", "doctor_quick")
            putExtra("doctorName", "Quick Consultation")
        }
        startActivity(intent)
    }
}