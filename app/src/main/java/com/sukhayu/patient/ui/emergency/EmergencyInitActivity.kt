// package com.sukhayu.patient.ui.emergency

// import android.Manifest
// import android.content.Intent
// import android.content.pm.PackageManager
// import android.os.Bundle
// import androidx.appcompat.app.AppCompatActivity
// import androidx.core.app.ActivityCompat
// import androidx.core.content.ContextCompat
// import androidx.lifecycle.lifecycleScope
// import com.sukhayu.patient.databinding.ActivityEmergencyInitBinding
// import com.sukhayu.utils.VoiceInputHelper
// import kotlinx.coroutines.delay
// import kotlinx.coroutines.launch

// class EmergencyInitActivity : AppCompatActivity() {

//     private lateinit var binding: ActivityEmergencyInitBinding
//     private var countdown = 5
//     private lateinit var voiceHelper: VoiceInputHelper

//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         binding = ActivityEmergencyInitBinding.inflate(layoutInflater)
//         setContentView(binding.root)

//         requestAudioPermission()
//         voiceHelper = VoiceInputHelper(this)
//         VoiceInputHelper.attachToAllEditTexts(this)

//         lifecycleScope.launch {
//             while (countdown > 0) {
//                 binding.tvCountdown.text = countdown.toString()
//                 delay(1000)
//                 countdown--
//             }
//             startActivity(Intent(this@EmergencyInitActivity, EmergencyVCActivity::class.java))
//             finish()
//         }
//     }

//     private fun requestAudioPermission() {
//         if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
//             != PackageManager.PERMISSION_GRANTED) {
//             ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
//         }
//     }

//     override fun onDestroy() {
//         super.onDestroy()
//         voiceHelper.destroy()
//     }
// }
